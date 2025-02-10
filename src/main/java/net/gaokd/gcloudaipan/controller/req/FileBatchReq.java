package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: FileBatchReq
 * @Author: gkd
 * @date: 2025/2/10 15:26
 * @Version: V1.0
 */
@Data
public class FileBatchReq {

    @Schema(description = "移动文件的ID列表", required = true)
    private List<Long> fileIds;

    @Schema(description = "目标父文件ID", required = true)
    private Long targetParentId;

    @Schema(description = "账号ID", required = true)
    private Long accountId;
}
