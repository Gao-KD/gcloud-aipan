package net.gaokd.gcloudaipan.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;
import net.gaokd.gcloudaipan.mapper.AccountFileMapper;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import net.gaokd.gcloudaipan.model.FileDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import org.springframework.stereotype.Service;

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

    @Override
    public void createFolder(FolderCreateReq folderCreateReq) {
    }
}
