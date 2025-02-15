package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.FolderTreeNodeDTO;

import java.util.List;

/**
 * @ClassName: AccountFileService
 * @Author: gkd
 * @date: 2025/2/6 00:27
 * @Version: V1.0
 */
public interface AccountFileService {
    //创建文件夹
    Long createFolder(FolderCreateReq folderCreateReq);

    //获取文件列表
    List<AccountFileDTO> listFile(Long accountId, Long parentId);

    //重命名文件
    void renameFile(FolderUpdateReq req);

    //文件树接口
    List<FolderTreeNodeDTO> folderTree(Long accountId);

    //文件树接口v2
    List<FolderTreeNodeDTO> folderTreeV2(Long accountId);

    //小文件上传接口
    void fileUpload(FileUploadReq req);

    //批量移动文件
    void moveBatch(FileBatchReq req);

    //批量删除文件
    void delBatch(FileDelBatchReq req);

    //批量复制文件
    void copyBatch(FileBatchReq req);

    //文件秒传
    Boolean secondUpload(FileSecondUpLoadReq req);

    //校验并更新存储空间
    boolean checkAndUpdateCapacity(Long accountId,Long fileSize);

    //保存文件关系和账号文件关系到数据库
    void saveFileAndAccountFile(FileUploadReq req, String storeFileObjectKey);
}
