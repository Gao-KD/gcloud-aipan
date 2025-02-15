package net.gaokd.gcloudaipan.dto;

import com.amazonaws.services.s3.model.PartSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.gaokd.gcloudaipan.model.FileChunkDO;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileChunkInitDTO
 * @Author: gkd
 * @date: 2025/2/15 16:46
 * @Version: V1.0
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class FileChunkDTO {

    public FileChunkDTO(FileChunkDO fileChunkDO) {
        SpringBeanUtil.copyProperties(fileChunkDO, this);
    }

    private Long id;

    @Schema(description = "文件唯一标识（md5）")
    private String identifier;

    @Schema(description = "分片上传ID")
    private String uploadId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "所属桶名")
    private String bucketName;

    @Schema(description = "文件的key")
    private String objectKey;

    @Schema(description = "总文件大小（byte）")
    private Long totalSize;

    @Schema(description = "每个分片大小（byte）")
    private Long chunkSize;

    @Schema(description = "分片数量")
    private Integer chunkNum;

    @Schema(description = "用户ID")
    private Long accountId;

    @Schema(description = "是否完成上传")
    private Boolean finished;

    @Schema(description = "存在的分片数量")
    private List<PartSummary> exitPartList = new ArrayList<>();
}
