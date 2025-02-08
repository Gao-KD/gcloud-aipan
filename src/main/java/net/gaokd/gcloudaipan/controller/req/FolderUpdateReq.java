package net.gaokd.gcloudaipan.controller.req;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @ClassName: FolderUpdateReq
 * @Author: gkd
 * @date: 2025/2/8 17:24
 * @Version: V1.0
 */
@Data
public class FolderUpdateReq {

    /**
     * 账号id
     */
    @Schema(description = "账号ID", example = "123456789", required = true)
    private Long accountId;

    /**
     * 文件id
     */
    @Schema(description = "文件ID", example = "987654321", required = true)
    private Long fileId;

    /**
     * 新文件名
     */
    @Schema(description = "新文件名", example = "new_folder_name", required = true)
    private String newFileName;
}
