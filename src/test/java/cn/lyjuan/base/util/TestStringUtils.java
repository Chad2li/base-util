package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by chad on 2016/7/12.
 */
public class TestStringUtils
{
    @Test
    public void testToStrForObject()
    {
        Object obj = new Object();
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(obj.toString(), objToStr);
    }

    @Test
    public void testToStrForNull()
    {
        Object obj = null;
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals("", objToStr);
    }

    @Test
    public void testToStrForString()
    {
        String obj = "abc";
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(obj, objToStr);
    }

    @Test
    public void testToStrForInt()
    {
        int obj = 1;
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(String.valueOf(obj), objToStr);
    }

    @Test
    public void testToStrForSingleCls()
    {
        TestToStrParent obj = new TestToStrParent();
        obj.name = "张三";
        String objToStr = StringUtils.toStr(obj);

        String result = TestToStrParent.class.getSimpleName() + "{name=" + obj.name + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrForMultCls()
    {
        TestToStrChild obj = new TestToStrChild();
        obj.name = "张三";
        obj.age = 18;
        obj.birthday = LocalDate.now();
        String objToStr = StringUtils.toStr(obj);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday="+DateUtils.format(obj.birthday, "yyyy-MM-dd") + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrForMultClsExtends()
    {
        TestToStrChild obj = new TestToStrChild();
        obj.name = "张三";
        obj.age = 18;
        obj.birthday = LocalDate.now();
        String objToStr = StringUtils.toStr(obj, null, TestToStrParent.class);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday="+DateUtils.format(obj.birthday, "yyyy-MM-dd")
                +" Parent_TestToStrParent{name="+obj.name+"}}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrInnerCls()
    {
        TestToStrInner obj = new TestToStrInner();
        obj.base = new TestToStrParent();
        obj.base.name = "张三";
        obj.age = 18;

        String objToStr = StringUtils.toStr(obj);
        String result = "TestToStrInner{base=TestToStrParent{name="+obj.base.name+"}, age="+obj.age+"}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", "18");

        String result = StringUtils.toStr(map);
        String expect = "[name=张三,age=18]";

        Assert.assertEquals(expect, result);
    }

    @Test
    public void testToStrList()
    {
        List<String> list = new ArrayList<>();

        list.add("1");
        list.add("2");
        list.add("3");

        String result = StringUtils.toStr(list);
        String expect = "[1,2,3]";

        Assert.assertEquals(expect, result);
    }

    @Test
    public void isNullArray() {
        String abc = "123";
        Assert.assertFalse(StringUtils.isNull(abc));

        abc = null;
        Assert.assertTrue(StringUtils.isNull(abc));

        byte[] def = null;
        Assert.assertTrue(StringUtils.isNullArray(def));
        def = new byte[]{1, 2};
        Assert.assertFalse(StringUtils.isNull(def));
        def = new byte[]{};
        Assert.assertTrue(StringUtils.isNull(def));
    }

    public static class TestToStrParent
    {
        public String name;
    }

    public static class TestToStrChild extends TestToStrParent
    {
        public int age;
        public LocalDate birthday;
    }

    public static class TestToStrInner
    {
        public TestToStrParent base;

        public int age;
    }
}
