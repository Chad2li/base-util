package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by chad on 2016/7/12.
 */
public class TestStringUtils {
    @Test
    public void testToStrForObject() {
        Object obj = new Object();
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(obj.toString(), objToStr);
    }

    @Test
    public void testToStrForNull() {
        Object obj = null;
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals("", objToStr);
    }

    @Test
    public void testToStrForString() {
        String obj = "abc";
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(obj, objToStr);
    }

    @Test
    public void testToStrForInt() {
        int obj = 1;
        String objToStr = StringUtils.toStr(obj);

        Assert.assertEquals(String.valueOf(obj), objToStr);
    }

    @Test
    public void testToStrForSingleCls() {
        TestToStrParent obj = new TestToStrParent();
        obj.name = "张三";
        String objToStr = StringUtils.toStr(obj);

        String result = TestToStrParent.class.getSimpleName() + "{name=" + obj.name + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrForMultCls() {
        TestToStrChild obj = new TestToStrChild();
        obj.name = "张三";
        obj.age = 18;
        obj.birthday = LocalDate.now();
        String objToStr = StringUtils.toStr(obj);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday=" + DateUtils.format(obj.birthday, "yyyy-MM-dd") + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrForMultClsExtends() {
        TestToStrChild obj = new TestToStrChild();
        obj.name = "张三";
        obj.age = 18;
        obj.birthday = LocalDate.now();
        String objToStr = StringUtils.toStr(obj, null, TestToStrParent.class);

        String result = "TestToStrChild{age=" + obj.age
                + ", birthday=" + DateUtils.format(obj.birthday, "yyyy-MM-dd")
                + " Parent_TestToStrParent{name=" + obj.name + "}}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrInnerCls() {
        TestToStrInner obj = new TestToStrInner();
        obj.base = new TestToStrParent();
        obj.base.name = "张三";
        obj.age = 18;

        String objToStr = StringUtils.toStr(obj);
        String result = "TestToStrInner{base=TestToStrParent{name=" + obj.base.name + "}, age=" + obj.age + "}";

        Assert.assertEquals(result, objToStr);
    }

    @Test
    public void testToStrMap() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", "18");

        String result = StringUtils.toStr(map);
        String expect = "[name=张三,age=18]";

        Assert.assertEquals(expect, result);
    }

    @Test
    public void testToStrList() {
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

    @Test
    public void toStr() {
        Object obj = new byte[]{1, 2, 3};
        String str = StringUtils.toStr(obj);
        Assert.assertEquals("[1,2,3]", str);
    }

    @Test
    public void baseTypeArray() {
        List list = null;
        Object obj = null;

        byte b = 1;
        obj = b;

        // byte
        obj = new byte[]{1, 2, 3};
        byte[] b4 = (byte[]) obj;
        list = Arrays.asList(b4);
        arrayTypeCompare(byte.class, Byte.class, obj);
        byte[] b1 = (byte[]) obj;
        obj = new Byte[]{1, 2, 3};
        arrayTypeCompare(byte.class, Byte.class, obj);
        Byte[] b2 = (Byte[]) obj;
        list = Arrays.asList(new byte[]{1, 2, 3});
        // long
        arrayTypeCompare(long.class, Long.class, new long[]{1, 2, 3});
        arrayTypeCompare(long.class, Long.class, new Long[]{1L, 2L, 3L});
        list = Arrays.asList(new long[]{1, 2, 3});
        // double
        arrayTypeCompare(double.class, Double.class, new double[]{1, 2, 3});
        arrayTypeCompare(double.class, Double.class, new Double[]{1D, 2D, 3D});
        list = Arrays.asList(new double[]{1, 2, 3});
        // float
        arrayTypeCompare(float.class, Float.class, new float[]{1, 2, 3});
        arrayTypeCompare(float.class, Float.class, new Float[]{1F, 2F, 3F});
        list = Arrays.asList(new float[]{1, 2, 3});
        // short
        arrayTypeCompare(short.class, Short.class, new short[]{1, 2, 3});
        arrayTypeCompare(short.class, Short.class, new Short[]{1, 2, 3});
        list = Arrays.asList(new short[]{1, 2, 3});
        // char
        arrayTypeCompare(char.class, Character.class, new char[]{'a', 'b', 'c'});
        arrayTypeCompare(char.class, Character.class, new Character[]{new Character('a'), new Character('b'), new Character('c')});
        list = Arrays.asList(new char[]{'a', 'b', 'c'});
        // boolean
        arrayTypeCompare(boolean.class, Boolean.class, new boolean[]{true, false});
        arrayTypeCompare(boolean.class, Boolean.class, new Boolean[]{true, false});
        list = Arrays.asList(new boolean[]{true, false});

    }

    private void arrayTypeCompare(Class<?> expectBase, Class<?> expectPkg, Object obj) {
        Class<?> type = obj.getClass().getComponentType();
        System.out.println("\n\n============= "+type.getName()+" ============");
        System.out.println("type ==> " + type.getName());
        System.out.println("is base ==> " + (expectBase == type));
        System.out.println("is pkg ==> " + (expectPkg == type));
    }

    public static class TestToStrParent {
        public String name;
    }

    public static class TestToStrChild extends TestToStrParent {
        public int age;
        public LocalDate birthday;
    }

    public static class TestToStrInner {
        public TestToStrParent base;

        public int age;
    }
}
