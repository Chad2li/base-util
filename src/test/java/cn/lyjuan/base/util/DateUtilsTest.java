package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

/**
 * Created by chad on 2017/1/9.
 */
public class DateUtilsTest
{
    @Test
    public void monday()
    {
        String pattern = "yyyy-MM-dd";

        // 周一为 2017-01-09
        String expect = "2017-01-09";
        Date   now    = DateUtils.parse("2017-01-12", pattern);
        Date   monday = DateUtils.monday(now, Locale.CHINA);
        Assert.assertEquals(expect, DateUtils.format(monday, pattern));

        expect = "2017-01-08";
        now    = DateUtils.parse("2017-01-12", pattern);
        monday = DateUtils.monday(now, Locale.US);
        Assert.assertEquals(expect, DateUtils.format(monday, pattern));

        // 临界
        expect = "2017-01-09";
        now = DateUtils.parse(expect, pattern);
        monday = DateUtils.monday(now, Locale.CHINA);
        Assert.assertEquals(expect, DateUtils.format(monday, pattern));

        // 临界
        expect = "2017-01-30";
        now = DateUtils.parse("2017-02-02", pattern);
        monday = DateUtils.monday(now, Locale.CHINA);
        Assert.assertEquals(expect, DateUtils.format(monday, pattern));
    }

    public void firstDayOfMonth()
    {

    }
}