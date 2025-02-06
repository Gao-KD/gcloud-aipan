package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.AccountLoginReq;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: AccountService
 * @Author: gkd
 * @date: 2025/2/4 16:24
 * @Version: V1.0
 */
public interface AccountService {
    //注册
    void register(AccountRegisterReq req);

    //上传头像
    String uploadAvatar(MultipartFile file);

    //登录
    AccountDTO login(AccountLoginReq req);
}
