package net.gaokd.gcloudaipan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @ClassName: ShareSimpleDTO
 * @Author: gkd
 * @date: 2025/3/14 15:58
 * @Version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShareSimpleDTO",description = "分享简单链接信息")
@Accessors(chain = true)
public class ShareSimpleDTO {

    @Schema(description = "分享id")
    private Long id;

    @Schema(description = "分享名称")
    private String shareName;

    @Schema(description = "分享类型（no_code没有提取码 ,need_code有提取码）")
    private String shareType;

    @Schema(description = "分享类型（0 永久有效；1: 7天有效；2: 30天有效）")
    private Integer shareDayType;

    @Schema(description = "分享有效天数（永久有效为0）")
    private Integer shareDay;

    @Schema(description = "分享结束时间")
    private Date shareEndTime;

    @Schema(description = "分享链接地址")
    private String shareUrl;

    @Schema(description = "分享令牌，不需要校验码的时候才会有")
    private String token;

    @Schema(description = "分享着信息")
    private ShareAccountDTO shareAccountDTO;

}
