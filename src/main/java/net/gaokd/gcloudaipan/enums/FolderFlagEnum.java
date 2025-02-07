package net.gaokd.gcloudaipan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: FolderFlagEnum
 * @Author: gkd
 * @date: 2025/2/7 16:21
 * @Version: V1.0
 */
@AllArgsConstructor
@Getter
public enum FolderFlagEnum {
    /**
     * 非文件夹
     */
    NO(0),
    /**
     * 文件夹
     */
    YES(1);

    private final Integer code;
}
