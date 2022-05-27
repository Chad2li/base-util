package io.github.chad2li.baseutil.util;

import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by chad on 2016/8/12.
 */
public class JsonUtilsTest {
    public static class UserBean {
        private String name;

        private int age;

        private LocalDate date;

        private LocalDateTime time;

        public UserBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public UserBean(String name, int age) {
            this.name = name;
            this.age = age;
            this.date = LocalDate.parse(DATE, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.time = LocalDateTime.parse(TIME, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Override
        public String toString() {
            return "UserBean{" +
                    "name='" + name + '\'' +
                    ",age=" + age +
                    ",date=" + date +
                    ",time=" + time +
                    '}';
        }
    }

    private static String DATE = "1991-10-22";
    private static String TIME = "1991-10-22 14:58:59";

    @Test
    public void to() {
        String strVal = "abc";
        int intVal = 1;
        byte byteVal = 2;
        boolean boolVal = true;
        float floatVal = 3.1F;
        double doubleVal = 4.1;
        char charVal = 'a';
        long longVal = 5;
        short shortVal = 6;
        Assert.assertEquals(strVal, JsonUtils.to(strVal));
        Assert.assertEquals(String.valueOf(intVal), JsonUtils.to(intVal));
        Assert.assertEquals(String.valueOf(byteVal), JsonUtils.to(byteVal));
        Assert.assertEquals(String.valueOf(boolVal), JsonUtils.to(boolVal));
        Assert.assertEquals(String.valueOf(floatVal), JsonUtils.to(floatVal));
        Assert.assertEquals(String.valueOf(doubleVal), JsonUtils.to(doubleVal));
        Assert.assertEquals(String.valueOf(charVal), JsonUtils.to(charVal));
        Assert.assertEquals(String.valueOf(longVal), JsonUtils.to(longVal));
        Assert.assertEquals(String.valueOf(shortVal), JsonUtils.to(shortVal));

        LocalDateTime now = LocalDateTime.now();
        Assert.assertEquals(DateUtils.format(now, DateUtils.FMT_DATE_TIME), JsonUtils.to(now));

        UserBean user = new UserBean("张三", 19);
        String expect = "{\"name\":\"" + user.getName() + "\",\"age\":" + user.getAge() + ",\"date\":\"" + DATE + "\",\"time\":\"" + TIME + "\"}";
        String json = JsonUtils.to(user);

        Assert.assertEquals(expect, json);
    }

    @Test
    public void from_class_str() {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\"" + except.getName() + "\",\"age\":" + except.getAge() + ",\"date\":\"" + DATE + "\",\"time\":\"" + TIME + "\"}";

        UserBean user = JsonUtils.from(UserBean.class, json);
        System.out.println(StringUtils.toStr(user));

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }

    @Test
    public void from_type_json() {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\"" + except.getName() + "\",\"age\":" + except.getAge() + ",\"date\":\"" + DATE + "\",\"time\":\"" + TIME + "\"}";

        UserBean user = JsonUtils.from(new TypeToken<UserBean>() {
        }.getType(), json);

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }

    @Test
    public void simple_type() {
        String val = "";
        // String start with /
        val = "/abc";
        String val2 = JsonUtils.from(String.class, val);
        Assert.assertEquals(val, val2);
        // int
        val = "1";
        Integer intVal = JsonUtils.from(Integer.class, val);
        Assert.assertEquals(1, (int) intVal);

        val = "2021-01-01 11:22:33";
        LocalDateTime now = JsonUtils.from(LocalDateTime.class, val);
        Assert.assertEquals(val, DateUtils.format(now, "yyyy-MM-dd HH:mm:ss"));

        val = "true";
        Boolean booleanVal = JsonUtils.from(Boolean.class, val);
        Assert.assertTrue(booleanVal);
        val = "false";
        booleanVal = JsonUtils.from(Boolean.class, val);
        Assert.assertFalse(booleanVal);

        val = "1";
        Long longVal = JsonUtils.from(Long.class, val);
        Assert.assertEquals(1, (long) longVal);

        val = "1.0";
        Double doubleVal = JsonUtils.from(Double.class, val);
        Assert.assertEquals(1.0, (double) doubleVal, 0);

        val = "a";
        Character charVal = JsonUtils.from(Character.class, val);
        Assert.assertEquals('a', (char) charVal);

        val = "1";
        Short shortVal = JsonUtils.from(Short.class, val);
        Assert.assertEquals(1, (short) shortVal);

        val = "1.1";
        Float floatVal = JsonUtils.from(Float.class, val);
        Assert.assertEquals(1.1, (float) floatVal, 0.0001);
    }
}