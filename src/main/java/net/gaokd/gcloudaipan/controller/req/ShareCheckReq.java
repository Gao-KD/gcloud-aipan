package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName: ShareCheckReq
 * @Author: gkd
 * @date: 2025/3/17 10:34
 * @Version: V1.0
 */
@Data
@Schema(description = "分享校验请求参数")
public class ShareCheckReq {

    @Schema(description = "分享id", required = true)
    public Long shareId;

    @Schema(description = "分享提取码", required = true)
    public String shareCode;
}
