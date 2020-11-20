package cn.lyjuan.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.*;

/**
 * 实现线程安全的格式化时间
 *
 * @author chad
 * @version 2.0    2014-12-18
 */
public class DateUtils {
    public static final Map<String, DateTimeFormatter> DTFS = new HashMap<>();

    public static final ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();

    public static LocalDate parseDate(String date, String pattern) {
        return LocalDate.parse(date, getDTF(pattern));
    }
    /**
     * 解析日期
     *
     * @param time
     * @param pattern
     * @return
     */
    public static LocalDateTime parseTime(String time, String pattern) {
        return LocalDateTime.parse(time, getDTF(pattern));
    }

    /**
     * 缓存DateTimeFormatter，以免相同的pattern生成多个
     *
     * @param pattern
     * @return
     */
    private static DateTimeFormatter getDTF(String pattern) {
        DateTimeFormatter dtf = DTFS.get(pattern);
        if (null != dtf) return dtf;

        synchronized (DTFS) {
            if (null != (dtf = DTFS.get(pattern)))
                return dtf;
            dtf = DateTimeFormatter.ofPattern(pattern);
            DTFS.put(pattern, dtf);
        }

        return dtf;
    }

    public static String format(LocalDate date, String pattern) {
        return date.format(getDTF(pattern));
    }

    public static String format(LocalDateTime time, String pattern) {
        return time.format(getDTF(pattern));
    }

    /**
     * 13位毫秒级时间戳转时间
     * @param date
     * @return
     */
    public static LocalDateTime long2Time(long date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
    }

    /**
     * 时间转13位毫秒级时间戳
     * @param time
     * @return
     */
    public static long time2long(LocalDateTime time) {
        return time.toInstant(zoneOffset).toEpochMilli();
    }

    /**
     * @param date
     * @param add  毫秒
     * @return
     */
    public static LocalDateTime addLong(LocalDateTime date, long add) {
        return date.plus(add, ChronoUnit.MILLIS);
    }

    /**
     * 本周一
     *
     * @return
     */
    public static LocalDate monday() {
        return monday(LocalDate.now());
    }

    /**
     * 给定日期的周一
     *
     * @param now
     * @return
     */
    public static LocalDate monday(LocalDate now) {
        return now.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1);
    }


    /**
     * 给定日期月的第一天
     *
     * @param date
     * @return
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 当前月第一天
     *
     * @return
     */
    public static LocalDate firstDayOfMonth() {
        return firstDayOfMonth(LocalDate.now());
    }

}
