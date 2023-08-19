package io.github.chad2li.baseutil.util;


import org.springframework.lang.Nullable;

import java.math.BigDecimal;

/**
 * 数字工具类
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 15:51
 */
public class NumberUtils {

    /**
     * 是否 > 0
     *
     * @param number number
     * @return true: number > 0；false: number为null或 <= 0
     * @author chad
     * @since 1 by chad at 2023/8/19
     */
    public static boolean isPositive(@Nullable Integer number) {
        if (null == number) {
            return false;
        }
        return number > 0;
    }

    /**
     * @author chad
     * @see NumberUtils#isPositive(Integer)
     * @since 1 by chad at 2023/8/19
     */
    public static boolean isPositive(@Nullable Long number) {
        if (null == number) {
            return false;
        }
        return number > 0L;
    }

    /**
     * @author chad
     * @see NumberUtils#isPositive(Integer)
     * @since 1 by chad at 2023/8/19
     */
    public static boolean isPositive(@Nullable BigDecimal number) {
        if (null == number) {
            return false;
        }
        return BigDecimal.ZERO.compareTo(number) < 0;
    }

    /**
     * @author chad
     * @see NumberUtils#isPositive(Integer)
     * @since 1 by chad at 2023/8/19
     */
    public static boolean isPositive(@Nullable String number) {
        if (null == number) {
            return false;
        }
        // 转 big decimal
        return isPositive(new BigDecimal(number));
    }

    private NumberUtils() {
        // do nothing
    }
}
