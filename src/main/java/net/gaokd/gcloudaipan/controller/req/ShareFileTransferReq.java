package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: ShareFileTransferReq
 * @Author: gkd
 * @date: 2025/3/26 23:06
 * @Version: V1.0
 */
@Data
public class ShareFileTransferReq {

    @Schema(description = "用户ID")
    private Long accountId;

    @Schema(description = "父文件id")
    private Long parentId;

    @Schema(description = "分享ID")
    private Long shareId;

    @Schema(description = "转存文件id列表")
    private List<Long> fileIds;
}
