package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: ShareCancelReq
 * @Author: gkd
 * @date: 2025/3/14 14:12
 * @Version: V1.0
 */
@Data
public class ShareCancelReq {

    @Schema(description = "账号ID", required = false)
    private Long accountId;

    @Schema(description = "分享ID列表", required = true)
    private List<Long> shareIds;


}
