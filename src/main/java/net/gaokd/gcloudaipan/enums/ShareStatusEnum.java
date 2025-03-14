package net.gaokd.gcloudaipan.enums;

import lombok.Getter;

/**
 * @ClassName: ShareStatusEnum
 * @Author: gkd
 * @date: 2025/3/14 11:36
 * @Version: V1.0
 */
@Getter
public enum ShareStatusEnum {

    /**
     * 已使用
     */
    USED,

    /**
     * 已过期
     */
    EXPIRED,

    /**
     * 已取消
     */
    CANCELED,

    /**
     * 未使用
     */
    NOT_USED,

    /**
     * 已删除
     */
    DELETED
}
