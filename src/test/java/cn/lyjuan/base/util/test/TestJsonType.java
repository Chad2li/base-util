package cn.lyjuan.base.util.test;

import cn.lyjuan.base.util.JsonUtilsTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by chad on 2016/8/12.
 */
public class TestJsonType
{
    public static void main(String[] args)
    {
        printType(JsonUtilsTest.UserBean.class);
    }

    public static <T> void printType(T c)
    {
        LocalDate date = LocalDate.parse("1991-10-12", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println(date);

        LocalDateTime time = LocalDateTime.parse("1991-10-12 14:58:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
