package net.gaokd.gcloudaipan.controller.req;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: AccountRegisterReq
 * @Author: gkd
 * @date: 2025/2/4 16:28
 * @Version: V1.0
 */
@Data
@Builder
public class AccountRegisterReq {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像地址
     */
    private String avatarUrl;
}
