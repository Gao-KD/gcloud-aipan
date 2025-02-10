package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: FileUploadReq
 * @Author: gkd
 * @date: 2025/2/10 14:12
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
public class FileUploadReq {

    @Schema(description = "文件名", required = true)
    private String fileName;

    @Schema(description = "标识符", required = true)
    private String identifier;

    @Schema(description = "账户ID", required = true)
    private Long accountId;

    @Schema(description = "父目录ID", required = true)
    private Long parentId;

    @Schema(description = "文件大小", required = true)
    private Long fileSize;

    @Schema(description = "上传的文件", required = true)
    private MultipartFile file;
}
