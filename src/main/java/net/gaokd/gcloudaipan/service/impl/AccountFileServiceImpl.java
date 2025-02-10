package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.component.StoreEngine;
import net.gaokd.gcloudaipan.config.MinioConfig;
import net.gaokd.gcloudaipan.controller.req.FileUploadReq;
import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;
import net.gaokd.gcloudaipan.controller.req.FolderUpdateReq;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.FolderTreeNodeDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.enums.FileTypeEnum;
import net.gaokd.gcloudaipan.enums.FolderFlagEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountFileMapper;
import net.gaokd.gcloudaipan.mapper.FileMapper;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import net.gaokd.gcloudaipan.model.FileDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.FileService;
import net.gaokd.gcloudaipan.util.CommonUtil;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName: AccountFileServiceImpl
 * @Author: gkd
 * @date: 2025/2/6 00:27
 * @Version: V1.0
 */
@Service
@Slf4j
public class AccountFileServiceImpl implements AccountFileService {

    @Resource
    private AccountFileMapper accountFileMapper;

    @Resource
    private StoreEngine fileStoreEngine;

    @Resource
    private MinioConfig minioConfig;

    @Resource
    private FileMapper fileMapper;
    @Autowired
    private FileService fileService;

    /**
     * 查询文件列表
     *
     * @param accountId
     * @param parentId
     * @return
     */
    @Override
    public List<AccountFileDTO> listFile(Long accountId, Long parentId) {
        List<AccountFileDO> accountFileDOList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountId)
                .eq(AccountFileDO::getParentId, parentId)
                .orderByDesc(AccountFileDO::getIsDir, AccountFileDO::getGmtCreate));
        return SpringBeanUtil.copyProperties(accountFileDOList, AccountFileDTO.class);
    }

    /**
     * 重命名文件
     * 1、校验文件是否存子啊
     * 2、新旧文件名是否重复
     * 3、同层文件名称不能一样
     *
     * @param req
     */
    @Override
    public void renameFile(FolderUpdateReq req) {
        AccountFileDO accountFileDO = accountFileMapper.selectOne(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getId, req.getFileId())
                .eq(AccountFileDO::getAccountId, req.getAccountId()));
        if (accountFileDO == null) {
            log.error("文件不存在:{}", req);
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        } else {
            //新旧文件名称不能一样
            if (Objects.equals(accountFileDO.getFileName(), req.getNewFileName())) {
                log.error("文件名称重复,{}", req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            }
            //同层文件名称不能一样
            Long count = accountFileMapper.selectCount(new LambdaQueryWrapper<AccountFileDO>()
                    .eq(AccountFileDO::getAccountId, req.getAccountId())
                    .eq(AccountFileDO::getParentId, accountFileDO.getParentId())
                    .eq(AccountFileDO::getFileName, req.getNewFileName()));
            if (count > 0) {
                log.error("文件名称重复,{}", req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            } else {
                accountFileDO.setFileName(req.getNewFileName());
                accountFileMapper.updateById(accountFileDO);
            }
        }

    }

    /**
     * 文件树接口（非递归方式）
     * 1、查询用户全部文件夹
     * 2、拼装文件树
     * 通过从子节点开始添加到父节点
     *
     * @param accountId
     * @return
     */
    @Override
    public List<FolderTreeNodeDTO> folderTree(Long accountId) {
        List<AccountFileDO> folderList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountId)
                .eq(AccountFileDO::getIsDir, FolderFlagEnum.YES.getCode()));
        if (folderList.isEmpty()) {
            return List.of();
        }
        //数据源
        //构建一个map接口，key为文件夹ID，value为文件对象FolderTreeNodeDTO
        Map<Long, FolderTreeNodeDTO> folderTreeNodeDTOMap = folderList.stream().collect(Collectors.toMap(
                AccountFileDO::getId, accountFileDO ->
                        FolderTreeNodeDTO.builder()
                                .id(accountFileDO.getId())
                                .parentId(accountFileDO.getParentId())
                                .label(accountFileDO.getFileName())
                                .children(new ArrayList<>())
                                .build()

        ));
        //构建文件树，遍历数据源，为每个文件夹找到其子文件夹
        for (FolderTreeNodeDTO node : folderTreeNodeDTOMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && folderTreeNodeDTOMap.containsKey(parentId)) {
                //父节点
                FolderTreeNodeDTO folderTreeNodeDTO = folderTreeNodeDTOMap.get(parentId);
                folderTreeNodeDTO.getChildren().add(node);
            }
        }

        //过滤出根节点，即parentId是0
        List<FolderTreeNodeDTO> rootFolderList = folderTreeNodeDTOMap.values().stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
        return rootFolderList;
    }

    @Override
    public List<FolderTreeNodeDTO> folderTreeV2(Long accountId) {
        List<AccountFileDO> folderList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountId)
                .eq(AccountFileDO::getIsDir, FolderFlagEnum.YES.getCode()));
        if (folderList.isEmpty()) {
            return List.of();
        }
        //数据源
        List<FolderTreeNodeDTO> folderTreeNodeDTOS = folderList.stream().map(file -> {
            return FolderTreeNodeDTO.builder()
                    .id(file.getId())
                    .parentId(file.getParentId())
                    .label(file.getFileName())
                    .children(new ArrayList<>())
                    .build();
        }).toList();

        //根据父文件id分组，构建文件树，key为当前文件夹id，value为对应子文件夹列表
        Map<Long, List<FolderTreeNodeDTO>> parentIdMap = folderTreeNodeDTOS.stream()
                .collect(Collectors.groupingBy(FolderTreeNodeDTO::getParentId));
        for (FolderTreeNodeDTO node : folderTreeNodeDTOS) {
            List<FolderTreeNodeDTO> children = parentIdMap.get(node.getId());
            if (!CollectionUtil.isEmpty(children)) {
                node.getChildren().addAll(children);
            }
        }
        //过滤出根节点,即parentId=0的
        return folderTreeNodeDTOS.stream().filter(node -> node.getParentId() == 0L).toList();
    }

    /**
     * 小文件上传
     * 1、保存文件到存储引擎
     * 2、保存文件关系
     * 3、保存文件账号关系
     *
     * @param req
     */
    @Override
    public void fileUpload(FileUploadReq req) {
        //上传到存储引擎
        String storeFileObjectKey = storeFile(req);
        saveFileAndAccountFile(req,storeFileObjectKey);
    }

    /**
     * 保存文件关系和账号文件关系到数据库
     */
    public void saveFileAndAccountFile(FileUploadReq req, String storeFileObjectKey) {
        //保存文件
        FileDO fileDO = saveFile(req,storeFileObjectKey);
        //保存文件和账号关系
        AccountFileDTO accountFileDTO = AccountFileDTO.builder()
                .fileId(fileDO.getId())
                .fileSize(fileDO.getFileSize())
                .fileSuffix(fileDO.getFileSuffix())
                .fileName(fileDO.getFileName())
                //通过枚举类获取文件类型
                .fileType(FileTypeEnum.fromExtension(fileDO.getFileSuffix()).name())
                .accountId(req.getAccountId())
                .parentId(req.getParentId())
                .isDir(FolderFlagEnum.NO.getCode())
                .build();
        saveAccountFile(accountFileDTO);
    }

    /**
     * 保存文件关系到数据库
     * @param req
     * @param storeFileObjectKey
     * @return
     */
    private FileDO saveFile(FileUploadReq req, String storeFileObjectKey) {
        FileDO fileDO = new FileDO();
        fileDO.setAccountId(req.getAccountId());
        fileDO.setFileName(req.getFileName());
        fileDO.setFileSuffix(CommonUtil.getFileSuffix(req.getFile().getOriginalFilename()));
        fileDO.setFileSize(req.getFile() != null ? req.getFile().getSize() : req.getFileSize());
        fileDO.setIdentifier(req.getIdentifier());
        fileDO.setObjectKey(storeFileObjectKey);
        fileMapper.insert(fileDO);
        return fileDO;
    }

    /**
     * 存储文件返回唯一标识
     *
     * @param req
     * @return
     */
    private String storeFile(FileUploadReq req) {
        String objectKey = CommonUtil.getFilePath(req.getFile().getOriginalFilename());
        fileStoreEngine.upload(minioConfig.getBucketName(), objectKey, req.getFile());
        return objectKey;
    }

    //再写一个文件树接口V3，多数据情况下采用递归的方式遍历

    /**
     * 创建文件夹
     *
     * @param req
     */
    @Override
    public Long createFolder(FolderCreateReq req) {
        AccountFileDTO accountFileDTO = AccountFileDTO.builder()
                .accountId(req.getAccountId())
                .parentId(req.getParentId())
                .fileName(req.getFolderName())
                .isDir(FolderFlagEnum.YES.getCode())
                .build();
        return saveAccountFile(accountFileDTO);
    }

    /**
     * 处理用户和文件的关系，存储文件和文件夹都是可以的
     *
     * @param accountFileDTO
     * @return
     */
    private Long saveAccountFile(AccountFileDTO accountFileDTO) {
        //查询父文件夹是否存在
        checkParentFileId(accountFileDTO);

        AccountFileDO accountFileDO = SpringBeanUtil.copyProperties(accountFileDTO, AccountFileDO.class);

        //查询文件夹名是否存在
        processFileNameDuplicate(accountFileDO);
        //保存相关文件
        accountFileMapper.insert(accountFileDO);
        return accountFileDO.getId();
    }

    /**
     * 查询父文件夹是否存在
     *
     * @param accountFileDTO
     */
    private void checkParentFileId(AccountFileDTO accountFileDTO) {
        if (accountFileDTO.getParentId() != 0) {
            AccountFileDO accountFileDO = accountFileMapper.selectOne(new LambdaQueryWrapper<AccountFileDO>()
                    .eq(AccountFileDO::getAccountId, accountFileDTO.getAccountId())
                    .eq(AccountFileDO::getId, accountFileDTO.getParentId()));
            if (accountFileDO == null) {
                throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
            }
        }
    }

    /**
     * 处理文件是否重复
     * 文件夹重复和文件名重复处理规则不一样
     *
     * @param accountFileDO
     */
    private void processFileNameDuplicate(AccountFileDO accountFileDO) {
        Long count = accountFileMapper.selectCount(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountFileDO.getAccountId())
                .eq(AccountFileDO::getParentId, accountFileDO.getParentId())
                .eq(AccountFileDO::getFileName, accountFileDO.getFileName())
                .eq(AccountFileDO::getIsDir, accountFileDO.getIsDir()));
        if (count > 0) {
            //处理重复文件夹
            if (accountFileDO.getIsDir().equals(FolderFlagEnum.YES.getCode())) {
                accountFileDO.setFileName(accountFileDO.getFileName() + "_" + System.currentTimeMillis());
            } else {
                //处理重复文件名，提取文件扩展名
                String[] split = accountFileDO.getFileName().split("\\.");
                accountFileDO.setFileName(split[0] + "_" + System.currentTimeMillis() + split[1]);
            }
        }
    }
}
