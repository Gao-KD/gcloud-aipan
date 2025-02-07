package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "用户注册请求对象")
public class AccountRegisterReq {

    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true, example = "user123")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true, example = "password123")
    private String password;

    /**
     * 手机号
     */
    @Schema(description = "手机号", required = true, example = "13800138000")
    private String phone;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "http://example.com/avatar.jpg")
    private String avatarUrl;
}
