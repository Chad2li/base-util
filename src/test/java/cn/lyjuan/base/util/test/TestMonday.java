package cn.lyjuan.base.util.test;

import cn.lyjuan.base.util.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by chad on 2017/1/10.
 */
public class TestMonday
{
    public static void main(String[] args)
    {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        cal.setTime(DateUtils.parse("2017-01-10", "yyyy-MM-dd"));

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        Date monday = cal.getTime();

        System.out.println(DateUtils.format(monday, "yyyy-MM-dd"));
    }
}
