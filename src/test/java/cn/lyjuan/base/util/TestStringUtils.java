package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

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

        Assert.assertEquals("null", objToStr);
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
        obj.birthday = new Date();
        String objToStr = StringUtils.toStr(obj);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday="+DateUtils.format(obj.birthday, "yyyy-MM-dd HH:mm:ss") + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrForMultClsExtends()
    {
        TestToStrChild obj = new TestToStrChild();
        obj.name = "张三";
        obj.age = 18;
        obj.birthday = new Date();
        String objToStr = StringUtils.toStr(obj, null, TestToStrParent.class);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday="+DateUtils.format(obj.birthday, "yyyy-MM-dd HH:mm:ss")
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

    public static class TestToStrParent
    {
        public String name;
    }

    public static class TestToStrChild extends TestToStrParent
    {
        public int age;
        public Date birthday;
    }

    public static class TestToStrInner
    {
        public TestToStrParent base;

        public int age;
    }
}
