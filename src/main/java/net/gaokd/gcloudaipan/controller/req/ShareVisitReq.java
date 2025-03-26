package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: ShareVisitReq
 * @Author: gkd
 * @date: 2025/3/14 15:09
 * @Version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareVisitReq {

    @Schema(description = "分享ID", required = false)
    private Long accountId;

    @Schema(description = "分享文件ID列表", required = true)
    private List<Long> fileIds;

}
