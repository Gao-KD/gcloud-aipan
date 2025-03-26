package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.component.StoreEngine;
import net.gaokd.gcloudaipan.config.MinioConfig;
import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.FolderTreeNodeDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.enums.FileTypeEnum;
import net.gaokd.gcloudaipan.enums.FolderFlagEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountFileMapper;
import net.gaokd.gcloudaipan.mapper.FileMapper;
import net.gaokd.gcloudaipan.mapper.StorageMapper;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import net.gaokd.gcloudaipan.model.FileDO;
import net.gaokd.gcloudaipan.model.StorageDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.util.CommonUtil;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Resource
    private StorageMapper storageMapper;


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
     * 校验用户存储空间
     * 1、保存文件到存储引擎
     * 2、保存文件关系
     * 3、保存文件账号关系
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fileUpload(FileUploadReq req) {
        boolean storageEnough = checkAndUpdateCapacity(req.getAccountId(), req.getFile().getSize());
        if (storageEnough) {
            //上传到存储引擎
            String storeFileObjectKey = storeFile(req);
            //保存文件和账号关系
            saveFileAndAccountFile(req, storeFileObjectKey);
        } else {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

    }

    /**
     * 批量移动文件
     * 1、校验文件列表id是否合法
     * 2、校验目标文件夹id是否合法
     * 3、批量移动处理文件重命名
     * 4、更新
     *
     * @param req
     */
    @Override
    public void moveBatch(FileBatchReq req) {
        //检查被移动的文件ID是否合法
        List<AccountFileDO> accountFileDOList = checkFileIdIllegal(req.getFileIds(), req.getAccountId());

        //检查目标文件ID是否合法，包括子文件夹
        checkTargetParentIdLegal(req);

        //批量转移文件到目标文件夹
        accountFileDOList.forEach(accountFileDO -> accountFileDO.setParentId(req.getTargetParentId()));

        //批量移动，处理文件重命名
        accountFileDOList.forEach(this::processFileNameDuplicate);

        //更新文件或者文件夹的parent_id为目标文件夹的ID
        for (AccountFileDO accountFileDO : accountFileDOList) {
            if (accountFileMapper.updateById(accountFileDO) < 0) {
                log.error("文件移动失败");
                throw new BizException(BizCodeEnum.FILE_UPDATE_BATCH_ERROR);
            }
        }

    }

    /**
     * * 步骤一：检查是否满足：1、文件ID数量是否合法，2、文件是否属于当前用户
     * * 步骤二：判断文件是否是文件夹，文件夹的话需要递归获取里面子文件ID，然后进行批量删除
     * * 步骤三：需要更新账号存储空间使用情况
     * * 步骤四：批量删除账号映射文件，考虑回收站如何设计
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatch(FileDelBatchReq req) {
        //步骤一：检查是否满足：1、文件ID数量是否合法，2、文件是否属于当前用户
        List<AccountFileDO> accountFileDOList = checkFileIdIllegal(req.getFileIds(), req.getAccountId());

        List<AccountFileDO> storageAccountFileDOList = new ArrayList<>();

        //步骤二：判断文件是否是文件夹，文件夹的话需要递归获取里面子文件ID，然后进行批量删除
        findAllAccountFileDOWithIterative(storageAccountFileDOList, accountFileDOList, false);
        //拿到全部文件ID列表
        List<Long> allFileIdList = storageAccountFileDOList.stream().filter(file -> !Objects.equals(file.getParentId(), 0L)).map(AccountFileDO::getId).toList();

        //步骤三：需要更新账号存储空间使用情况,可以加个分布式锁，redisson
        long allFileSize = storageAccountFileDOList.stream()
                .filter(file -> Objects.equals(file.getIsDir(), FolderFlagEnum.NO.getCode()))
                .mapToLong(AccountFileDO::getFileSize)
                .sum();
        //校验并更新存储空间
        StorageDO storageDO = storageMapper.selectOne(new LambdaQueryWrapper<StorageDO>()
                .eq(StorageDO::getAccountId, req.getAccountId()));
        storageDO.setUsedSize(storageDO.getUsedSize() - allFileSize);
        storageMapper.updateById(storageDO);
        //步骤四：批量删除账号映射文件，考虑回收站如何设计
        accountFileMapper.deleteBatchIds(allFileIdList);
    }

    /**
     * * 检查被转移的文件ID是否合法
     * * 检查目标文件夹ID是否合法
     * * 执行拷贝，递归查找【差异点，ID是全新的】
     * * 计算存储空间大小，检查是否足够【差异点，空间需要检查】
     * * 存储相关记录
     *
     * @param req
     */
    @Override
    public void copyBatch(FileBatchReq req) {
        //步骤一：检查被转移的文件ID是否合法
        List<AccountFileDO> accountFileDOList = checkFileIdIllegal(req.getFileIds(), req.getAccountId());
        //步骤二：检查目标文件夹ID是否合法
        checkTargetParentIdLegal(req);
        //步骤三：执行拷贝，递归查找【差异点，ID是全新的】
        List<AccountFileDO> newAccountFileDOList = findBatchCopyFileWithRecur(accountFileDOList, req.getTargetParentId());

        long allFileSize = newAccountFileDOList.stream()
                .filter(file -> Objects.equals(file.getIsDir(), FolderFlagEnum.NO.getCode()))
                .mapToLong(AccountFileDO::getFileSize)
                .sum();
        //步骤四：计算存储空间大小，检查是否足够【差异点，空间需要检查】
        if (!checkAndUpdateCapacity(req.getAccountId(), allFileSize)) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }
        ;
        //存储相关记录
        accountFileMapper.insertFileBatch(newAccountFileDOList);

    }

    /**
     * 文件秒传
     * 1、检查文件是否存在
     * 2、检查空间是否足够
     * 3、建立关系
     *
     * @param req
     * @return
     */
    @Override
    public Boolean secondUpload(FileSecondUpLoadReq req) {
        //检查文件是否存在
        FileDO fileDO = fileMapper.selectOne(new LambdaQueryWrapper<FileDO>()
                .eq(FileDO::getIdentifier, req.getIdentifier()));
        //检查空间是否足够
        if (fileDO != null && checkAndUpdateCapacity(req.getAccountId(), fileDO.getFileSize())) {
            //处理文件秒传
            AccountFileDTO accountFileDTO = AccountFileDTO.builder()
                    .accountId(req.getAccountId())
                    .fileId(fileDO.getId())
                    .parentId(req.getParentId())
                    .fileSuffix(fileDO.getFileSuffix())
                    .fileName(req.getFileName())
                    .fileType(FileTypeEnum.fromExtension(fileDO.getFileSuffix()).name())
                    .fileSize(fileDO.getFileSize())
                    .del(false)
                    .isDir(FolderFlagEnum.NO.getCode())
                    .build();
            //保存关联文件关系，里面有做相关检查
            saveAccountFile(accountFileDTO);
            return true;
        }
        return false;
    }

    /**
     * 执行拷贝，递归查找【差异点，ID是全新的】
     *
     * @param accountFileDOList
     * @param targetParentId
     * @return
     */
    @Override
    public List<AccountFileDO> findBatchCopyFileWithRecur(List<AccountFileDO> accountFileDOList, Long targetParentId) {
        List<AccountFileDO> newAccountFileDOList = new ArrayList<>();
        accountFileDOList.forEach(accountFileDO -> doCopyChildRecord(newAccountFileDOList, accountFileDO, targetParentId));
        return newAccountFileDOList;
    }

    /**
     * 递归拷贝
     *
     * @param newAccountFileDOList
     * @param accountFileDO
     * @param targetParentId
     */
    private void doCopyChildRecord(List<AccountFileDO> newAccountFileDOList, AccountFileDO accountFileDO, Long targetParentId) {
        //保存旧的ID，方便查找子文件夹
        Long oldAccountFileId = accountFileDO.getId();
        //创建新纪录
        accountFileDO.setId(IdUtil.getSnowflakeNextId());
        accountFileDO.setParentId(targetParentId);
        accountFileDO.setGmtModified(null);
        accountFileDO.setGmtCreate(null);


        //处理重复文件夹
        processFileNameDuplicate(accountFileDO);
        //纳入容器存储
        newAccountFileDOList.add(accountFileDO);

        if (Objects.equals(accountFileDO.getIsDir(), FolderFlagEnum.YES.getCode())) {
            //继续获取子文件夹列表
            List<AccountFileDO> childAccountFileDOList = findChildAccountFile(accountFileDO.getAccountId(), oldAccountFileId);
            if (CollectionUtil.isEmpty(childAccountFileDOList)) {
                return;
            }
            //递归处理
            childAccountFileDOList.forEach(childAccountFileDO -> doCopyChildRecord(newAccountFileDOList, childAccountFileDO, accountFileDO.getId()));
        }
    }

    /**
     * 查找文件记录，只查询下一级
     *
     * @param accountId
     * @param oldAccountFileId
     * @return
     */
    private List<AccountFileDO> findChildAccountFile(Long accountId, Long oldAccountFileId) {
        return accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getParentId, oldAccountFileId)
                .eq(AccountFileDO::getAccountId, accountId));
    }

    /**
     * 判断并更新账号存储空间使用情况
     *
     * @param accountId
     * @param fileSize
     */
    public boolean checkAndUpdateCapacity(Long accountId, Long fileSize) {
        StorageDO storageDO = storageMapper.selectOne(new LambdaQueryWrapper<StorageDO>()
                .eq(StorageDO::getAccountId, accountId));
        Long totalStorageSize = storageDO.getTotalSize();
        if (storageDO.getUsedSize() + fileSize <= totalStorageSize) {
            storageDO.setUsedSize(storageDO.getUsedSize() + fileSize);
            storageMapper.updateById(storageDO);
            return true;
        } else {
            return false;
        }
    }


    /**
     * 检查目标文件id是否合法，包括子文件夹
     * 1、目标文件ID不能是文件
     * 2、要操作的文件列表不能包含目标文件ID
     *
     * @param req
     */
    private void checkTargetParentIdLegal(FileBatchReq req) {
        //1、目标文件ID不能是文件
        AccountFileDO parentFileDO = accountFileMapper.selectOne(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getId, req.getTargetParentId())
                .eq(AccountFileDO::getIsDir, FolderFlagEnum.YES.getCode())
                .eq(AccountFileDO::getAccountId, req.getAccountId()));
        if (parentFileDO == null) {
            log.error("文件批量移动不合法，目标id:{}", req.getTargetParentId());
            throw new BizException(BizCodeEnum.FILE_UPDATE_BATCH_ERROR);
        }
        /**
         * 2、要操作的文件列表不能包含目标文件ID(***)
         * 2.1、查询批量操作的文件和文件夹，递归处理
         * 2.2、判断是否在里面
         */
        //2.1、查询批量操作的文件和文件夹
        List<AccountFileDO> prepareAccountFileDOList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .in(AccountFileDO::getId, req.getFileIds())
                .eq(AccountFileDO::getAccountId, req.getAccountId()));

        //2.2递归判断目标文件id是否在操作的目录下面
        //定义一个容器存储全部文件夹，包括子文件夹
        List<AccountFileDO> allAccountFileDOList = new ArrayList<>();

        findAllAccountFileDOWithRecur(allAccountFileDOList, prepareAccountFileDOList, false);

        //判断全部文件夹是否存在目标文件id
        if (allAccountFileDOList.stream().anyMatch(accountFileDO -> Objects.equals(accountFileDO.getId(), req.getTargetParentId()))) {
            log.error("文件批量移动不合法，目标id:{}", req.getTargetParentId());
            throw new BizException(BizCodeEnum.FILE_UPDATE_BATCH_ERROR);
        }
    }

    /**
     * 采用迭代方式批量查询所有子文件（和子文件夹）
     *
     * @param allAccountFileDOList     用于存储所有遍历到的文件/文件夹
     * @param prepareAccountFileDOList 初始操作的文件列表（可能包含文件和文件夹）
     * @param onlyFolder               如果为true，则只存储文件夹，否则全部存储
     */
    private void findAllAccountFileDOWithIterative(List<AccountFileDO> allAccountFileDOList, List<AccountFileDO> prepareAccountFileDOList, boolean onlyFolder) {
        if (prepareAccountFileDOList == null || prepareAccountFileDOList.isEmpty()) {
            return;
        }
        // 使用队列保存需要查询子节点的文件夹ID
        Queue<Long> queue = new LinkedList<>();
        // 将初始列表中的记录加入结果集，并将文件夹的ID加入队列（因为只有文件夹才可能有子文件）
        for (AccountFileDO file : prepareAccountFileDOList) {
            if (!onlyFolder || FolderFlagEnum.YES.getCode().equals(file.getIsDir())) {
                allAccountFileDOList.add(file);
            }
            if (FolderFlagEnum.YES.getCode().equals(file.getIsDir())) {
                queue.offer(file.getId());
            }
        }

        // 当队列非空时，批量查询当前层的所有子节点
        while (!queue.isEmpty()) {
            List<Long> parentIds = new ArrayList<>();
            while (!queue.isEmpty()) {
                parentIds.add(queue.poll());
            }
            // 批量查询所有 parentId 在当前层的子节点，减少数据库调用次数
            List<AccountFileDO> children = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                    .in(AccountFileDO::getParentId, parentIds));
            for (AccountFileDO child : children) {
                if (!onlyFolder || FolderFlagEnum.YES.getCode().equals(child.getIsDir())) {
                    allAccountFileDOList.add(child);
                }
                // 如果子节点是文件夹，则加入队列，用于继续查询它的子节点
                if (FolderFlagEnum.YES.getCode().equals(child.getIsDir())) {
                    queue.offer(child.getId());
                }
            }
        }
    }

    /**
     * 目标文件id是否在操作的文件目录下
     *
     * @param allAccountFileDOList
     * @param prepareAccountFileDOList
     * @param onlyFolder
     */
    @Override
    public void findAllAccountFileDOWithRecur(List<AccountFileDO> allAccountFileDOList, List<AccountFileDO> prepareAccountFileDOList, boolean onlyFolder) {
        for (AccountFileDO prepare : prepareAccountFileDOList) {
            if (Objects.equals(prepare.getIsDir(), FolderFlagEnum.YES.getCode())) {
                List<AccountFileDO> children = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                        .eq(AccountFileDO::getParentId, prepare.getId()));

                findAllAccountFileDOWithRecur(allAccountFileDOList, children, onlyFolder);
            }

            //如果onlyFolder是true，只存储文件夹到allAccountFileDOList，否则都存储到allAccountFileDOList
            if (!onlyFolder || Objects.equals(prepare.getIsDir(), FolderFlagEnum.YES.getCode())) {
                allAccountFileDOList.add(prepare);
            }
        }
    }


    /**
     * 校验文件列表id是否合法
     *
     * @param fileIds
     * @param accountId
     */
    @Override
    public List<AccountFileDO> checkFileIdIllegal(List<Long> fileIds, Long accountId) {
        //更加严谨的操作是fileIds要去重
        List<AccountFileDO> accountFileDOList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .in(AccountFileDO::getId, fileIds)
                .eq(AccountFileDO::getAccountId, accountId));
        if (accountFileDOList.size() == fileIds.size()) {
            return accountFileDOList;
        } else {
            log.error("文件ID数量不合法,ids:{}", accountFileDOList.size());
            throw new BizException(BizCodeEnum.FILE_UPDATE_BATCH_ERROR);
        }
    }

    /**
     * 保存文件关系和账号文件关系到数据库
     */
    public void saveFileAndAccountFile(FileUploadReq req, String storeFileObjectKey) {
        //保存文件
        FileDO fileDO = saveFile(req, storeFileObjectKey);
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
     *
     * @param req
     * @param storeFileObjectKey
     * @return
     */
    private FileDO saveFile(FileUploadReq req, String storeFileObjectKey) {
        FileDO fileDO = new FileDO();
        fileDO.setAccountId(req.getAccountId());
        fileDO.setFileName(req.getFileName());
        fileDO.setFileSuffix(CommonUtil.getFileSuffix(req.getFileName()));
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
     * @param accountFileDO 文件信息对象
     */
    private void processFileNameDuplicate(AccountFileDO accountFileDO) {
        if (accountFileDO == null || accountFileDO.getFileName() == null) {
            return;
        }

        Long count = accountFileMapper.selectCount(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountFileDO.getAccountId())
                .eq(AccountFileDO::getParentId, accountFileDO.getParentId())
                .eq(AccountFileDO::getFileName, accountFileDO.getFileName())
                .eq(AccountFileDO::getIsDir, accountFileDO.getIsDir()));

        if (count > 0) {
            String originalFileName = accountFileDO.getFileName();
            String timeStamp = "_" + System.currentTimeMillis();
            // 如果是文件夹，则直接拼接时间戳
            if (FolderFlagEnum.YES.getCode().equals(accountFileDO.getIsDir())) {
                accountFileDO.setFileName(originalFileName + timeStamp);
            } else {
                // 对文件名进行处理，保留扩展名（如果存在）
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex == -1) {
                    // 文件没有扩展名
                    accountFileDO.setFileName(originalFileName + timeStamp);
                } else {
                    String baseName = originalFileName.substring(0, dotIndex);
                    String extension = originalFileName.substring(dotIndex); // 包含点
                    accountFileDO.setFileName(baseName + timeStamp + extension);
                }
            }
        }
    }

}
