package net.gaokd.gcloudaipan.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: ShareCreateReq
 * @Author: gkd
 * @date: 2025/3/14 13:39
 * @Version: V1.0
 */
@Data
public class ShareCreateReq {

    @Schema(description = "分享名称",required = true)
    private String shareName;

    @Schema(description = "是否需要提取码",required = true)
    private String shareType;

    @Schema(description = "分享有效天数，0-永久，1-7天，2-30天",required = true)
    private Integer shareDayType;

    @Schema(description = "分享文件列表",required = true)
    private List<Long> fileIds;

    @Schema(description = "账号ID",required = false)
    private Long accountId;

}
