package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;

/**
 * @ClassName: AccountFileService
 * @Author: gkd
 * @date: 2025/2/6 00:27
 * @Version: V1.0
 */
public interface AccountFileService {
    //创建文件夹
    void createFolder(FolderCreateReq folderCreateReq);
}
