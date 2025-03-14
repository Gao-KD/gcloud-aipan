package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: FileChunkMergeReq
 * @Author: gkd
 * @date: 2025/2/15 17:46
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
public class FileChunkMergeReq {

    @Schema(description = "账户ID", required = false)
    private Long accountId;

    @Schema(description = "文件标识符", required = true)
    private String identifier;

    @Schema(description = "父文件id", required = true)
    private Long parentId;
}
