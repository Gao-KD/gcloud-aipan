package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.core.date.DateUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.component.StoreEngine;
import net.gaokd.gcloudaipan.config.MinioConfig;
import net.gaokd.gcloudaipan.controller.req.FileChunkInitReq;
import net.gaokd.gcloudaipan.controller.req.FileChunkMergeReq;
import net.gaokd.gcloudaipan.controller.req.FileUploadReq;
import net.gaokd.gcloudaipan.dto.FileChunkDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.FileChunkMapper;
import net.gaokd.gcloudaipan.mapper.StorageMapper;
import net.gaokd.gcloudaipan.model.FileChunkDO;
import net.gaokd.gcloudaipan.model.StorageDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.FileChunkService;
import net.gaokd.gcloudaipan.util.CommonUtil;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;

/**
 * @ClassName: FileChunkServiceImpl
 * @Author: gkd
 * @date: 2025/2/15 16:56
 * @Version: V1.0
 */
@Service
@Slf4j
public class FileChunkServiceImpl implements FileChunkService {

    @Resource
    private StoreEngine fileStoreEngine;

    @Resource
    private StorageMapper storageMapper;

    @Resource
    private FileChunkMapper fileChunkMapper;

    @Resource
    private MinioConfig minioConfig;

    @Resource
    private AccountFileService accountFileService;

    /**
     * 1-初始化文件分片上传
     * * 检查存储空间是否够( 合并文件的时候进行校验更新存储空间)
     * * 根据文件名推断内容类型
     * * 初始化分片上传,获取上传ID
     * * 创建上传任务实体并设置相关属性
     * * 将任务插入数据库，构建并返回任务信息DTO
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileChunkDTO initFileChunkTask(FileChunkInitReq req) {
        //检查存储空间是否够( 合并文件的时候进行校验更新存储空间)
        StorageDO storageDO = storageMapper.selectOne(new LambdaQueryWrapper<StorageDO>()
                .eq(StorageDO::getAccountId, req.getAccountId()));
        if (storageDO.getUsedSize() + req.getTotalSize() > storageDO.getTotalSize()) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

        //根据文件名推断内容类型
        String contentType = MediaTypeFactory.getMediaType(req.getFileName()).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();

        String objectKey = CommonUtil.getFilePath(req.getFileName());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        //初始化分片上传,获取上传ID
        InitiateMultipartUploadResult uploadResult = fileStoreEngine.initMultipartUploadTask(minioConfig.getBucketName(), objectKey, objectMetadata);
        String uploadId = uploadResult.getUploadId();

        int chunkNum = (int) Math.ceil(req.getTotalSize() * 1.0 / req.getChunkSize());
        FileChunkDO fileChunkDO = new FileChunkDO()
                .setAccountId(req.getAccountId())
                .setIdentifier(req.getIdentifier())
                .setFileName(req.getFileName())
                .setBucketName(minioConfig.getBucketName())
                .setObjectKey(objectKey)
                .setTotalSize(req.getTotalSize())
                .setUploadId(uploadId)
                .setChunkSize(req.getChunkSize())
                .setChunkNum(chunkNum);
        //保存到数据库
        fileChunkMapper.insert(fileChunkDO);
        return new FileChunkDTO(fileChunkDO)
                .setFinished(false)
                .setExitPartList(new ArrayList<>());
    }

    /**
     * 获取临时文件上传地址
     *
     * @param accountId
     * @param identifier
     * @param partNumber
     * @return
     */
    @Override
    public String genPreSignUploadUrl(Long accountId, String identifier, Integer partNumber) {
        FileChunkDO fileChunkDO = fileChunkMapper.selectOne(new LambdaQueryWrapper<FileChunkDO>()
                .eq(FileChunkDO::getAccountId, accountId)
                .eq(FileChunkDO::getIdentifier, identifier));
        if (fileChunkDO == null) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }
        Map<String, Object> param = new HashMap<>();
        param.put("uploadId", fileChunkDO.getUploadId());
        param.put("partNumber", String.valueOf(partNumber));
        long PRE_SIGN_URL_EXPIRE = minioConfig.getPRE_SINGLE_URL_EXPIRE_TIME();
        Date expiration = DateUtil.offsetMillisecond(new Date(), (int) PRE_SIGN_URL_EXPIRE);
        URL preSignedUrl = fileStoreEngine.genePreSignedUrl(minioConfig.getBucketName(), fileChunkDO.getObjectKey(), HttpMethod.PUT, expiration, param);
        return preSignedUrl.toString();
    }

    /**
     * *  获取任务和分片列表，检查是否足够合并
     * *  检查存储空间和更新
     * *  合并分片
     * *  判断合并分片是否成功
     * *  存储文件和关联信息到数据库
     * *  根据唯一标识符删除相关分片信息
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeFileChunk(FileChunkMergeReq req) {

        //获取任务和分片列表，检查是否足够合并
        FileChunkDO task = fileChunkMapper.selectOne(new LambdaQueryWrapper<FileChunkDO>()
                .eq(FileChunkDO::getAccountId, req.getAccountId())
                .eq(FileChunkDO::getIdentifier, req.getIdentifier()));
        if (task == null) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }

        //查看分片数量
        PartListing partListing = fileStoreEngine.listMultipart(minioConfig.getBucketName(), task.getObjectKey(), task.getUploadId());
        List<PartSummary> parts = partListing.getParts();
        if (parts.size() != task.getChunkNum()) {
            throw new BizException(BizCodeEnum.FILE_CHUNK_NOT_ENOUGH);
        };

        long realFileTotalSize = parts.stream().mapToLong(PartSummary::getSize).sum();
        //检查存储空间和更新
        boolean enough = accountFileService.checkAndUpdateCapacity(req.getAccountId(), realFileTotalSize);
        if (!enough) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }


        //合并分片
        List<PartETag> partETagList = parts.stream().map(partSummary -> new PartETag(partSummary.getPartNumber(), partSummary.getETag())).toList();
        CompleteMultipartUploadResult result = fileStoreEngine.mergeChunks(minioConfig.getBucketName(), task.getObjectKey(), task.getUploadId(), partETagList);
        //判断是否合并成功
        if (result.getETag() != null) {
            FileUploadReq fileUploadReq = new FileUploadReq();
            fileUploadReq.setAccountId(req.getAccountId())
                    .setFileName(task.getFileName())
                    .setIdentifier(task.getIdentifier())
                    .setParentId(req.getParentId())
                    .setFileSize(realFileTotalSize)
                    .setFile(null);

            //存储文件和关联信息到数据库
            accountFileService.saveFileAndAccountFile(fileUploadReq, task.getObjectKey());

            //删除相关任务记录,留存也可
            fileChunkMapper.deleteById(task.getId());

            log.info("合并成功");
        }
    }

    /**
     * 获取分片上传进度
     *
     * @param accountId
     * @param identifier
     */
    @Override
    public FileChunkDTO getFileChunkUploadProgress(Long accountId, String identifier) {
        FileChunkDO task = fileChunkMapper.selectOne(new LambdaQueryWrapper<FileChunkDO>()
                .eq(FileChunkDO::getAccountId, accountId)
                .eq(FileChunkDO::getIdentifier, identifier));
        if (task == null || !identifier.equals(task.getIdentifier())) {
            return null;
        }
        FileChunkDTO result = new FileChunkDTO(task);

        //判断文件是否存在
        boolean doesObjectExist = fileStoreEngine.doesObjectExist(task.getBucketName(), task.getObjectKey());
        if (!doesObjectExist) {
            // 不存在，表示未上传完，返回已上传的分片
            PartListing partListing = fileStoreEngine.listMultipart(task.getBucketName(), task.getObjectKey(), task.getUploadId());
            if (task.getChunkNum() == partListing.getParts().size()) {
                //已经存在，合并
                result.setFinished(true).setExitPartList(partListing.getParts());
            } else {
                result.setFinished(false).setExitPartList(partListing.getParts());
            }
        }
        return result;
    }
}
