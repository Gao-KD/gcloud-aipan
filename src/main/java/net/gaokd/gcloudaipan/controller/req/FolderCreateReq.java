package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: FolderCreateReq
 * @Author: gkd
 * @date: 2025/2/6 00:19
 * @Version: V1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件夹创建请求对象")
public class FolderCreateReq {

    /**
     * 文件夹名称
     */
    @Schema(description = "文件夹名称", required = true, example = "MyFolder")
    private String folderName;

    /**
     * 上级文件夹id
     */
    @Schema(description = "上级文件夹ID", example = "12345")
    private Long parentId;

    /**
     * 用户id
     */
    @Schema(description = "用户ID", required = false, example = "67890")
    private Long accountId;
}
