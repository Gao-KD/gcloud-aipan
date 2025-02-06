package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.component.StoreEngine;
import net.gaokd.gcloudaipan.config.AccountConfig;
import net.gaokd.gcloudaipan.config.MinioConfig;
import net.gaokd.gcloudaipan.controller.req.AccountLoginReq;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.enums.AccountRoleEnum;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountMapper;
import net.gaokd.gcloudaipan.mapper.StorageMapper;
import net.gaokd.gcloudaipan.model.AccountDO;
import net.gaokd.gcloudaipan.model.StorageDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.AccountService;
import net.gaokd.gcloudaipan.util.CommonUtil;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static net.gaokd.gcloudaipan.config.AccountConfig.*;

/**
 * @ClassName: AccountServiceImpl
 * @Author: gkd
 * @date: 2025/2/4 16:24
 * @Version: V1.0
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {


    @Resource
    private AccountMapper accountMapper;

    @Resource
    private StoreEngine fileStoreEngine;

    @Resource
    private MinioConfig minioConfig;

    @Resource
    private StorageMapper storageMapper;

    @Resource
    private AccountFileService accountFileService;

    /**
     * 1、查询手机号是否重复
     * 2、加密密码
     * 3、插入数据库
     * 4、其他相关初始化
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AccountRegisterReq req) {
        //1、查询手机号是否重复
        List<AccountDO> accountDOList = accountMapper.selectList(new QueryWrapper<AccountDO>()
                .eq("phone", req.getPhone()));
        if (!accountDOList.isEmpty()) {
            throw new BizException(BizCodeEnum.ACCOUNT_REPEAT);
        }
        // 2、加密密码
        String encryptedPassword = encryptPassword(req.getPassword());

        // 3、插入数据库
        AccountDO accountDO = new AccountDO();
        accountDO.setRole(AccountRoleEnum.COMMON.name());
        accountDO.setPhone(req.getPhone());
        accountDO.setPassword(encryptedPassword);
        accountDO.setGmtModified(new Date());
        accountMapper.insert(accountDO);

        // 4、其他相关初始化 todo
        //初始化大小
        StorageDO storageDO = new StorageDO();
        storageDO.setAccountId(accountDO.getId());
        storageDO.setUsedSize(0L);
        storageDO.setTotalSize(DEFAULT_STORAGE_SIZE);
        storageMapper.insert(storageDO);

        //初始化根目录
        FolderCreateReq folderCreateReq = FolderCreateReq.builder()
                .accountId(accountDO.getId())
                .parentId(ROOT_PARENT_ID)
                .folderName(ROOT_FOLDER_NAME)
                .build();
        accountFileService.createFolder(folderCreateReq);

    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String objectKey = CommonUtil.getFilePath(file.getOriginalFilename());
        fileStoreEngine.upload(minioConfig.getAvatarBucketName(), objectKey, file);
        return minioConfig.getEndpoint() + "/" + minioConfig.getAvatarBucketName() + "/" + objectKey;
    }

    @Override
    public AccountDTO login(AccountLoginReq req) {
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getUsername, req.getUsername()));
        if (accountDO == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
        if (!Objects.equals(accountDO.getPassword(), encryptPassword(req.getPassword()))) {
            throw new BizException(BizCodeEnum.ACCOUNT_PWD_ERROR);
        }
        return SpringBeanUtil.copyProperties(accountDO, AccountDTO.class);
    }

    /**
     * 加密密码
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // 使用BCrypt加密密码
        return BCrypt.hashpw(password, AccountConfig.ACCOUNT_SALT);
    }

}
