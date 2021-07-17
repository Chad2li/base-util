package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

public class ReflectUtilsTest {
    @Test
    public void setValue() {
        User user = new User();

        ReflectUtils.setValue(user, "name", "张三");
        Assert.assertEquals("张三", user.getName());

        user.setName("李四");
        ReflectUtils.setValue(user, "name", null);
        Assert.assertNull(user.getName());
    }

    @Test
    public void getValueNoThrow() throws Exception {
        User user = new User("张三", 18, 183);

        String name = (String) ReflectUtils.getValueNoThrow(user, "name");
        Assert.assertEquals("张三", name);

        Integer age = (Integer) ReflectUtils.getValueNoThrow(user, "age");
        Assert.assertEquals(Integer.valueOf(18), age);

        int height = (int) ReflectUtils.getValueNoThrow(user, "height");
        Assert.assertEquals(183, height);
    }

    @Test
    public void testGenericityType() {
        Class cls = ReflectUtils.getGenericityClass(ExtendClass.class, 0);
        Assert.assertEquals(cls, User.class);

        cls = ReflectUtils.getGenericityClass(ExtendMultClass.class, 0);
        Assert.assertEquals(cls, User.class);

        cls = ReflectUtils.getGenericityClass(ExtendMultClass.class, 1);
        Assert.assertEquals(cls, Address.class);
    }

    @Test
    public void membersToMap() {
        // --- base
        User user = new User();
        user.setName("张三");
        user.setHeight(195);
        user.setAge(18);

        Map<String, Object> map = ReflectUtils.membersToMap(user);

        Assert.assertEquals(user.getName(), map.get("name"));
        Assert.assertEquals(user.getAge(), map.get("age"));
        Assert.assertEquals(user.getHeight(), map.get("height"));


        // --- extends
        ExtendUser extend = new ExtendUser();
        List<Address> addrs = new ArrayList<>();
        addrs.add(new Address("浙江", "杭州"));
        addrs.add(new Address("安徽", "宣城"));

        extend.setName("李四");
        extend.setAge(18);
        extend.setHeight(195);
        extend.setAddrs(addrs);

        map = ReflectUtils.membersToMap(extend);

        Assert.assertEquals(extend.getName(), map.get("name"));
        Assert.assertEquals(extend.getAddrs(), map.get("addrs"));
    }

    @Test
    public void field() {
        // exist
        Field f = ReflectUtils.field(ExtendUser.class, "name");
        Assert.assertNotNull(f);

        // not exist
        f = ReflectUtils.field(ExtendUser.class, "abc");
        Assert.assertNull(f);
    }


    @Test
    public void method() {
        // exist
        Method m = ReflectUtils.method(ExtendUser.class, "setName", String.class);
        Assert.assertNotNull(m);

        // not exist
        m = ReflectUtils.method(ExtendUser.class, "abc");
        Assert.assertNull(m);
    }

    @Test
    public void isBaseClass() {
        int intv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(intv));
        Integer intv2 = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(intv2));
        byte bv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(bv));
        Byte bv2 = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(bv2));
        short sv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(sv));
        Short sv2 = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(sv2));
        float fv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(fv));
        Float fv2 = 1F;
        Assert.assertTrue(ReflectUtils.isBaseClass(fv2));
        double dv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(dv));
        Double dv2 = 1D;
        Assert.assertTrue(ReflectUtils.isBaseClass(dv2));
        String strv = "a";
        Assert.assertTrue(ReflectUtils.isBaseClass(strv));
        boolean boov = false;
        Assert.assertTrue(ReflectUtils.isBaseClass(boov));
        boolean boov2 = true;
        Assert.assertTrue(ReflectUtils.isBaseClass(boov2));
        long lv = 1;
        Assert.assertTrue(ReflectUtils.isBaseClass(lv));
        Long lv2 = 1L;
        Assert.assertTrue(ReflectUtils.isBaseClass(lv2));
        LocalDateTime ldtv = LocalDateTime.now();
        Assert.assertTrue(ReflectUtils.isBaseClass(ldtv));
        LocalDate ldv = LocalDate.now();
        Assert.assertTrue(ReflectUtils.isBaseClass(ldv));
        LocalTime ltv = LocalTime.now();
        Assert.assertTrue(ReflectUtils.isBaseClass(ltv));
        Date datev = new Date();
        Assert.assertTrue(ReflectUtils.isBaseClass(datev));
    }

    private void checkIsBaseClass(Object obj) {
        Assert.assertTrue(ReflectUtils.isBaseClass(obj));
    }

    public class ExtendUser extends User {
        private List<Address> addrs;

        public List<Address> getAddrs() {
            return addrs;
        }

        public void setAddrs(List<Address> addrs) {
            this.addrs = addrs;
        }

        @Override
        public String toString() {
            return "ExtendUser{" +
                    "addrs=" + addrs +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    ", height=" + height +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ExtendUser that = (ExtendUser) o;
            return Objects.equals(getAddrs(), that.getAddrs());
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), getAddrs());
        }
    }

    public static class ExtendClass extends BaseGenericity<User> {
        @Override
        public String toString() {
            return "BaseGenericity{" +
                    "obj=" + obj +
                    '}';
        }
    }

    public static class BaseGenericity<T> {
        public T obj;
    }

    public static class ExtendMultClass extends BaseMultGenericity<User, Address> {

    }

    public static class BaseMultGenericity<U, A> {
        public U u;

        public List<A> as;
    }

    public class Address {
        public String province;

        public String city;

        public Address() {
        }

        public Address(String province, String city) {
            this.province = province;
            this.city = city;
        }
    }

    public static class User {
        protected String name;

        protected Integer age;

        protected int height;

        public User() {
        }

        public User(String name, Integer age, int height) {
            this.name = name;
            this.age = age;
            this.height = height;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return getHeight() == user.getHeight() &&
                    Objects.equals(getName(), user.getName()) &&
                    Objects.equals(getAge(), user.getAge());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getName(), getAge(), getHeight());
        }
    }
}