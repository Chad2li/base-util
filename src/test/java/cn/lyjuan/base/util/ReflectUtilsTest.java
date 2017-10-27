package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ReflectUtilsTest
{
    @Test
    public void setValue()
    {
        User user = new User();

        ReflectUtils.setValue(user, "name", "张三");
        Assert.assertEquals("张三", user.getName());

        user.setName("李四");
        ReflectUtils.setValue(user, "name", null);
        Assert.assertNull(user.getName());
    }

    @Test
    public void getValueNoThrow() throws Exception
    {
        User user = new User("张三", 18, 183);

        String name = (String) ReflectUtils.getValueNoThrow(user, "name");
        Assert.assertEquals("张三", name);

        Integer age = (Integer) ReflectUtils.getValueNoThrow(user, "age");
        Assert.assertEquals(Integer.valueOf(18), age);

        int height = (int) ReflectUtils.getValueNoThrow(user, "height");
        Assert.assertEquals(183, height);
    }

    @Test
    public void testGenericityType()
    {
        Class cls = ReflectUtils.getGenericityClass(ExtendClass.class, 0);
        Assert.assertEquals(cls, User.class);

        cls = ReflectUtils.getGenericityClass(ExtendMultClass.class, 0);
        Assert.assertEquals(cls, User.class);

        cls = ReflectUtils.getGenericityClass(ExtendMultClass.class, 1);
        Assert.assertEquals(cls, Address.class);
    }

    public static class ExtendClass<User> extends BaseGenericity<User>
    {
        @Override
        public String toString()
        {
            return "BaseGenericity{" +
                    "obj=" + obj +
                    '}';
        }
    }

    public static class BaseGenericity<T>
    {
        public T obj;
    }

    public static class ExtendMultClass<User, Address> extends BaseMultGenericity<User, Address>
    {

    }

    public static class BaseMultGenericity<U, A>
    {
        public U u;

        public List<A> as;
    }

    public class Address
    {
        public String province;

        public String city;
    }

    public class User
    {
        private String name;

        private Integer age;

        private int height;

        public User()
        {
        }

        public User(String name, Integer age, int height)
        {
            this.name = name;
            this.age = age;
            this.height = height;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public int getHeight()
        {
            return height;
        }

        public void setHeight(int height)
        {
            this.height = height;
        }
    }
}