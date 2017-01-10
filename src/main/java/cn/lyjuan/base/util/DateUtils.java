package cn.lyjuan.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 实现线程安全的格式化时间
 *
 * @author chad
 * @version 2.0    2014-12-18
 */
public class DateUtils
{
    public static final Map<String, SimpleDateFormat> SDFS = new HashMap<String, SimpleDateFormat>();

    /**
     * 获取今日时间
     *
     * @return
     */
    public static Date getTodayDate()
    {
        String today = DateUtils.format(new Date(), "yyyy-MM-dd");
        return DateUtils.parse(today, "yyyy-MM-dd");
    }

    /**
     * 将时间字符串根据Java时间格式解析为 Date 对象
     *
     * @param date    时间字符串
     * @param pattern 时间字符串格式
     * @return
     */
    public static Date parse(String date, String pattern)
    {
        synchronized (SDFS)
        {
            SimpleDateFormat sdf = getSDF(pattern);

            try
            {
                return sdf.parse(date);
            } catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将 Date 对象根据时间格式转化为时间字符串
     *
     * @param date    时间
     * @param pattern 时间字符串格式
     * @return
     */
    public static String format(Date date, String pattern)
    {
        synchronized (SDFS)
        {
            SimpleDateFormat sdf = getSDF(pattern);

            return sdf.format(date);
        }
    }

    /**
     * 将 13 位毫秒级时间根据时间字符串格式转化为时间字符串
     *
     * @param date    13 位毫秒级时间
     * @param pattern 时间字符串格式
     * @return
     */
    public static String format(long date, String pattern)
    {
        synchronized (SDFS)
        {
            SimpleDateFormat sdf = getSDF(pattern);

            return sdf.format(date);
        }
    }

    /**
     * 增加时间
     *
     * @param date 操作时间
     * @param add  时间增加值，为负表示减
     * @return
     */
    public static Date addLong(Date date, long add)
    {
        return new Date(date.getTime() + add);
    }

    /**
     * 获取本周第一天日期
     * 以星期日为一周的n第一天
     *
     * @param now 如果为空，则为当时时间
     * @return
     */
    public static Date monday(Date now, Locale locale)
    {
        now = null == now ? new Date() : now;
        locale = null == locale ? Locale.CHINA : locale;

        Calendar cal    = Calendar.getInstance();
        Date     monday = null;
        synchronized (SDFS)
        {
            cal.setTime(now);
            if (locale == Locale.CHINA)
            {
                cal.setFirstDayOfWeek(Calendar.MONDAY);
            } else
            {
                cal.setFirstDayOfWeek(Calendar.SUNDAY);
            }
            // 设置为每周第一天
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            clearTime(cal);// 清空时间

            monday = cal.getTime();
        }

        return monday;
    }

    private static void clearTime(Calendar cal)
    {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
    }

    /**
     * 本月第一天
     *
     * @param now 如果为空，则为当时时间
     * @return
     */
    public static Date firstDayOfMonth(Date now)
    {
        now = null == now ? new Date() : now;
        Calendar cal = Calendar.getInstance();
        Date firstDayOfMonth = null;

        synchronized (SDFS)
        {
            cal.setTime(now);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            clearTime(cal);// 清空时间

            firstDayOfMonth = cal.getTime();
        }

        return firstDayOfMonth;
    }

    /**
     * 私有方法，内部有锁权限时，可根据时间字符串格式获取或生成相应的时间格式工具
     *
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSDF(String pattern)
    {
        SimpleDateFormat sdf = SDFS.get(pattern);

        if (null != sdf) return sdf;

        sdf = new SimpleDateFormat(pattern);

        SDFS.put(pattern, sdf);

        return sdf;
    }
}
