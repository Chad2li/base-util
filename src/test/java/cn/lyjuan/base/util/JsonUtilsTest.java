package cn.lyjuan.base.util;

import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chad on 2016/8/12.
 */
public class JsonUtilsTest
{
    public static class UserBean
    {
        private String name;

        private int age;

        public UserBean() {}
        public UserBean(String name, int age) {this.name = name;this.age = age;}
        public String getName(){ return name;}
        public void setName(String name) {this.name = name;}
        public int getAge() {return age;}
        public void setAge(int age) {this.age = age;}
        @Override
        public String toString() {return "UserBean{" + "name='" + name + '\'' + ", age=" + age + '}';}
    }

    @Test
    public void to()
    {
        UserBean user = new UserBean("张三", 19);
        String expect = "{\"name\":\""+user.getName()+"\",\"age\":"+user.getAge()+"}";
        String json = JsonUtils.to(user);

        Assert.assertEquals(expect, json);
    }

    @Test
    public void from_class_str()
    {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\""+except.getName()+"\",\"age\":"+except.getAge()+"}";

        UserBean user = JsonUtils.from(UserBean.class, json);

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }

    @Test
    public void from_type_json()
    {
        UserBean except = new UserBean("张三", 19);
        String json = "{\"name\":\""+except.getName()+"\",\"age\":"+except.getAge()+"}";

        UserBean user = JsonUtils.from(new TypeToken<UserBean>(){}.getType(), json);

        Assert.assertEquals(StringUtils.toStr(except), StringUtils.toStr(user));
    }
}