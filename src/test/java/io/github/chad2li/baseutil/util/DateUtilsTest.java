package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * Created by chad on 2017/1/9.
 */

public class DateUtilsTest {
    String date = "1991-10-12";
    String dateMonday = "1991-10-07";
    String dateFirstDayOfMonth = "1991-10-01";
    String datePattern = "yyyy-MM-dd";
    String time = "1991-10-13 14:58:59";
    String time1 = "1991-10-13 14:59:59";
    String timePattern = "yyyy-MM-dd HH:mm:ss";

    @Test
    public void testParseDate() {
        LocalDate ld = DateUtils.parseDate(date, datePattern);
        String the = DateUtils.format(ld, datePattern);
        Assert.assertEquals(the, date);

    }

    @Test
    public void testParseDateTime() {
        LocalDateTime ldt = DateUtils.parseDateTime(time, timePattern);
        String the = DateUtils.format(ldt, timePattern);
        Assert.assertEquals(the, time);
    }

    @Test
    public void parseTime() {
        LocalTime time = LocalTime.now();
        String str = DateUtils.format(time, DateUtils.FMT_TIME);
        time = DateUtils.parseTime(str, DateUtils.FMT_TIME);
    }

    @Test
    public void testLong2Time() {
        LocalDateTime ldt = DateUtils.parseDateTime(time, timePattern);
        long first = DateUtils.time2long(ldt);
        LocalDateTime ldt2 = DateUtils.long2Time(first);

        Assert.assertEquals(time, DateUtils.format(ldt2, timePattern));
    }

    @Test
    public void testAddLong() {
        LocalDateTime ldt = DateUtils.parseDateTime(time, timePattern);
        ldt = DateUtils.addLong(ldt, 60 * 1000);//加10分钟

        Assert.assertEquals(time1, DateUtils.format(ldt, timePattern));
    }

    @Test
    public void testMonday() {
        LocalDate ld = DateUtils.parseDate(date, datePattern);
        ld = DateUtils.monday(ld);

        Assert.assertEquals(dateMonday, DateUtils.format(ld, datePattern));
    }

    @Test
    public void testFirstDayOfMonth() {
        LocalDate ld = DateUtils.parseDate(date, datePattern);
        ld = DateUtils.firstDayOfMonth(ld);

        Assert.assertEquals(dateFirstDayOfMonth, DateUtils.format(ld, datePattern));
    }

    @Test
    public void duration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = DateUtils.addLong(now, 5000);

        long dura = DateUtils.duration(now, end);
        Assert.assertEquals(5000, dura);
    }

    @Test
    public void isGt() {
        Temporal date1;
        Temporal date2;
        // -- 大于
        // 1.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(-1);
        Assert.assertTrue(DateUtils.isGt(date1, date2));
        // 1.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(-1, ChronoUnit.DAYS);
        Assert.assertTrue(DateUtils.isGt(date1, date2));
        // 1.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(-1, ChronoUnit.SECONDS);
        Assert.assertTrue(DateUtils.isGt(date1, date2));
        // -- 相等
        // 2.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(0);
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        // 2.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(0, ChronoUnit.DAYS);
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        // 2.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(0, ChronoUnit.SECONDS);
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        // -- 小于
        // 2.4 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(1);
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        // 2.5 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(1, ChronoUnit.DAYS);
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        // 2.5 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(1, ChronoUnit.SECONDS);
        Assert.assertFalse(DateUtils.isGt(date1, date2));

        // 3.1 空
        date1 = null;
        date2 = LocalDateTime.now();
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        date1 = LocalDateTime.now();
        date2 = null;
        Assert.assertFalse(DateUtils.isGt(date1, date2));
        date1 = null;
        date2 = null;
        Assert.assertFalse(DateUtils.isGt(date1, date2));
    }

    @Test
    public void isGte() {
        Temporal date1;
        Temporal date2;
        // -- 大于
        // 1.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(-1);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // 1.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(-1, ChronoUnit.DAYS);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // 1.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(-1, ChronoUnit.SECONDS);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // -- 相等
        // 2.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(0);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // 2.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(0, ChronoUnit.DAYS);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // 2.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(0, ChronoUnit.SECONDS);
        Assert.assertTrue(DateUtils.isGte(date1, date2));
        // -- 小于
        // 2.4 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(1);
        Assert.assertFalse(DateUtils.isGte(date1, date2));
        // 2.5 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(1, ChronoUnit.DAYS);
        Assert.assertFalse(DateUtils.isGte(date1, date2));
        // 2.5 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(1, ChronoUnit.SECONDS);
        Assert.assertFalse(DateUtils.isGte(date1, date2));

        // 3.1 空
        date1 = null;
        date2 = LocalDateTime.now();
        Assert.assertFalse(DateUtils.isGte(date1, date2));
        date1 = LocalDateTime.now();
        date2 = null;
        Assert.assertFalse(DateUtils.isGte(date1, date2));
        date1 = null;
        date2 = null;
        Assert.assertFalse(DateUtils.isGte(date1, date2));
    }

    @Test
    public void isEquals() {
        Temporal date1;
        Temporal date2;
        // -- 大于
        // 1.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(-1);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        // 1.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(-1, ChronoUnit.DAYS);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        // 1.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(-1, ChronoUnit.SECONDS);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        // -- 相等
        // 2.1 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(0);
        Assert.assertTrue(DateUtils.isEquals(date1, date2));
        // 2.2 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(0, ChronoUnit.DAYS);
        Assert.assertTrue(DateUtils.isEquals(date1, date2));
        // 2.3 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(0, ChronoUnit.SECONDS);
        Assert.assertTrue(DateUtils.isEquals(date1, date2));
        // -- 小于
        // 2.4 ldt
        date1 = LocalDateTime.now();
        date2 = ((LocalDateTime) date1).plusSeconds(1);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        // 2.5 ldt
        date1 = LocalDate.now();
        date2 = date1.plus(1, ChronoUnit.DAYS);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        // 2.5 ldt
        date1 = LocalTime.now();
        date2 = date1.plus(1, ChronoUnit.SECONDS);
        Assert.assertFalse(DateUtils.isEquals(date1, date2));

        // 3.1 空
        date1 = null;
        date2 = LocalDateTime.now();
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        date1 = LocalDateTime.now();
        date2 = null;
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
        date1 = null;
        date2 = null;
        Assert.assertFalse(DateUtils.isEquals(date1, date2));
    }
}