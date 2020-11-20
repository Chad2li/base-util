package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by chad on 2017/1/9.
 */

public class DateUtilsTest
{
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
    public void testParseTime() {
        LocalDateTime ldt = DateUtils.parseTime(time, timePattern);
        String the = DateUtils.format(ldt, timePattern);
        Assert.assertEquals(the, time);
    }

    @Test
    public void testLong2Time() {
        LocalDateTime ldt = DateUtils.parseTime(time, timePattern);
        long first = DateUtils.time2long(ldt);
        LocalDateTime ldt2 = DateUtils.long2Time(first);

        Assert.assertEquals(time, DateUtils.format(ldt2, timePattern));
    }

    @Test
    public void testAddLong() {
        LocalDateTime ldt = DateUtils.parseTime(time, timePattern);
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
}