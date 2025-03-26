package net.gaokd.gcloudaipan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @ClassName: ShareDayTypeEnum
 * @Author: gkd
 * @date: 2025/3/14 11:38
 * @Version: V1.0
 */
@Getter
@AllArgsConstructor
public enum ShareDayTypeEnum {

    /**
     * 永久有效
     */
    PERMANENT(0, 0),

    /**
     * 7天有效
     */
    SEVEN_DAYS(1, 7),

    /**
     * 30天有效
     */
    THIRTY_DAYS(2, 30),

    /**
     * 90天有效
     */
    NINETY_DAYS(3, 90),

    /**
     * 180天有效
     */
    ONE_EIGHTY_DAYS(4, 180),

    /**
     * 365天有效
     */
    THREE_HUNDRED_FIFTY_FIVE_DAYS(5, 365);

    private final Integer dayType;

    private final Integer days;

    /**
     * 根据类型获取对应的天数
     *
     * @param dayType
     * @return
     */
    public static Integer fromDayType(Integer dayType) {
        for (ShareDayTypeEnum value : ShareDayTypeEnum.values()) {
            if (Objects.equals(value.getDayType(), dayType)) {
                return value.getDays();
            }
        }
        return null;
    }
}
