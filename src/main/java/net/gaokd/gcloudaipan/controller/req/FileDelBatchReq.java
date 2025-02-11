package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * @ClassName: FileDelBatchReq
 * @Author: gkd
 * @date: 2025/2/11 11:35
 * @Version: V1.0
 */
@Data
public class FileDelBatchReq {

    /**
     * 批量删除文件id
     */
    @Schema(description = "批量删除的文件ID列表", required = true, example = "[1, 2, 3]")
    private List<Long> fileIds;

    /**
     * 账号id
     */
    @Schema(description = "账号ID", required = false, example = "123456")
    private Long accountId;
}
