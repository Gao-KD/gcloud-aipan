package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: AccountLoginReq
 * @Author: gkd
 * @date: 2025/2/6 00:52
 * @Version: V1.0
 */
@Data
@Builder
@Schema(description = "用户登录请求对象")
public class AccountLoginReq {
    /**
     * 手机号
     */
    @Schema(description = "手机号", required = true, example = "user123")
    private String phone;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true, example = "password123")
    private String password;
}
