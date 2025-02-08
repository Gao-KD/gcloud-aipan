package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;
import net.gaokd.gcloudaipan.controller.req.FolderUpdateReq;
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
}
