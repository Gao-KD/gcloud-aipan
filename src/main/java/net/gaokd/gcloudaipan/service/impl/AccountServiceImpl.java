package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.config.AccountConfig;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.enums.AccountRoleEnum;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountMapper;
import net.gaokd.gcloudaipan.model.AccountDO;
import net.gaokd.gcloudaipan.service.AccountService;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    /**
     * 1、查询手机号是否重复
     * 2、加密密码
     * 3、插入数据库
     * 4、其他相关初始化
     * @param req
     * @return
     */
    @Override
    public void register(AccountRegisterReq req) {
        //1、查询手机号是否重复
        List<AccountDO> accountDOList = accountMapper.selectList(new QueryWrapper<AccountDO>()
                .eq("phone", req.getPhone()));
        if (!accountDOList.isEmpty()){
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
    }

    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // 使用BCrypt加密密码
        return BCrypt.hashpw(password, AccountConfig.ACCOUNT_SALT);
    }

}
