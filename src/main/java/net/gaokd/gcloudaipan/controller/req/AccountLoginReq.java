package net.gaokd.gcloudaipan.controller.req;

import lombok.Data;

/**
 * @ClassName: AccountLoginReq
 * @Author: gkd
 * @date: 2025/2/6 00:52
 * @Version: V1.0
 */
@Data
public class AccountLoginReq {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
