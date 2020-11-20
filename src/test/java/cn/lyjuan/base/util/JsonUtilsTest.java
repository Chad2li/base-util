package cn.lyjuan.base.util;

import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by chad on 2016/8/12.
 */
public class JsonUtilsTest
{
    public static class UserBean
    {
        private String name;

        private int age;

        private LocalDate date;

        private LocalDateTime time;

        public UserBean() {}

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
    public void to()
    {
        UserBean user = new UserBean("张三", 19);
        String expect = "{\"name\":\""+user.getName()+"\",\"age\":"+user.getAge()+",\"date\":\""+DATE+"\",\"time\":\""+TIME+"\"}";
        String json = JsonUtils.to(user);

        Assert.assertEquals(expect, json);
    }

    @Test
    public void from_class_str()
    {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\""+except.getName()+"\",\"age\":"+except.getAge()+",\"date\":\""+DATE+"\",\"time\":\""+TIME+"\"}";

        UserBean user = JsonUtils.from(UserBean.class, json);
        System.out.println(StringUtils.toStr(user));

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }

    @Test
    public void from_type_json()
    {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\""+except.getName()+"\",\"age\":"+except.getAge()+",\"date\":\""+DATE+"\",\"time\":\""+TIME+"\"}";

        UserBean user = JsonUtils.from(new TypeToken<UserBean>(){}.getType(), json);

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }
}