package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: FileChunkInitReq
 * @Author: gkd
 * @date: 2025/2/15 16:40
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
public class FileChunkInitReq {

    @Schema(description = "账户ID", required = false)
    private Long accountId;

    @Schema(description = "文件名", required = true)
    private String fileName;

    @Schema(description = "文件标识符", required = true)
    private String identifier;

    @Schema(description = "文件总大小（字节）", required = true)
    private Long totalSize;

    @Schema(description = "每个分片的大小（字节）", required = true)
    private Long chunkSize;
}
