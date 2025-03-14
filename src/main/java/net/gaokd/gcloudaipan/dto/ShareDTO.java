package net.gaokd.gcloudaipan.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户分享表
 * </p>
 *
 * @author gkd,
 * @since 2025-01-20
 */
@Getter
@Setter
@TableName("share")
@Schema(name = "ShareDO", description = "用户分享表")
public class ShareDTO implements Serializable {

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

    @Schema(description = "分享提取码")
    private String shareCode;

    @Schema(description = "分享状态  used正常, expired已失效,  canceled取消")
    private String shareStatus;

    @Schema(description = "分享创建人")
    private Long accountId;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "修改时间")
    private Date gmtModified;
}
