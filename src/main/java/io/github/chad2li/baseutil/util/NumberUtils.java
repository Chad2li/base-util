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

    /**
     * 判断 number1 > number2
     *
     * @param number1 number1
     * @param number2 number2
     * @return 如果number1或number2有一个为null返回false
     * @author chad
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGt(@Nullable Long number1, @Nullable Long number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1 > number2;
    }

    /**
     * 判断 number1 >= number2
     *
     * @param number1 number1
     * @param number2 number2
     * @return 如果number1或number2有一个为null返回false
     * @author chad
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGte(@Nullable Long number1, @Nullable Long number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1 >= number2;
    }

    /**
     * @author chad
     * @see NumberUtils#isGt(Long, Long)
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGt(@Nullable Integer number1, @Nullable Integer number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1 > number2;
    }

    /**
     * @author chad
     * @see NumberUtils#isGte(Long, Long)
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGte(@Nullable Integer number1, @Nullable Integer number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1 >= number2;
    }

    /**
     * @author chad
     * @see NumberUtils#isGt(Long, Long)
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGt(@Nullable BigDecimal number1, @Nullable BigDecimal number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1.compareTo(number2) > 0;
    }

    /**
     * @author chad
     * @see NumberUtils#isGte(Long, Long)
     * @since 1 by chad at 2023/8/20
     */
    public static boolean isGte(@Nullable BigDecimal number1, @Nullable BigDecimal number2) {
        if (null == number1 || null == number2) {
            return false;
        }

        return number1.compareTo(number2) >= 0;
    }

    /**
     * 是否相等
     *
     * @param long1          long 1
     * @param long2          long 2
     * @param bothNullResult 如果都为null，返回该值
     * @return true相等或都为null时bothNullResult为true
     * @author chad
     * @since 1 by chad at 2024/8/17
     */
    public static boolean isEq(@Nullable Long long1, @Nullable Long long2, boolean bothNullResult) {
        if (null == long1 && null == long2) {
            // 全为null，返回指定的结果
            return bothNullResult;
        }
        if (null == long1) {
            return false;
        }
        return long1.equals(long2);
    }

    private NumberUtils() {
        // do nothing
    }
}
