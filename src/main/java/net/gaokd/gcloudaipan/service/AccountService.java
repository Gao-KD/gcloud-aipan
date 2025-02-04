package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.util.JsonData;

/**
 * @ClassName: AccountService
 * @Author: gkd
 * @date: 2025/2/4 16:24
 * @Version: V1.0
 */
public interface AccountService {
    //注册
    void register(AccountRegisterReq req);
}
