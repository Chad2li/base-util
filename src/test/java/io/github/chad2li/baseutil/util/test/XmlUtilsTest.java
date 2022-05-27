package io.github.chad2li.baseutil.util.test;

import io.github.chad2li.baseutil.util.StringUtils;
import com.google.gson.reflect.TypeToken;
import lombok.Data;

import java.lang.reflect.*;
import java.util.*;

public class XmlUtilsTest {
    public static void main(String[] args) throws Exception {
//        field();
//        typeToken();
        type();
//        array();
    }

    public static void array()throws Exception{
        Field f = XmlModel.class.getDeclaredField("arr");
        Type type = f.getGenericType();
        System.out.println("type instanceof Array: " + (((Class) type).isArray()));
        System.out.println("type == String[].class: " + (type == String[].class));
        System.out.println("type instanceof ParameterizedType: " + (type instanceof ParameterizedType));
        System.out.println("type instanceof Class: " + (type instanceof Class));
        System.out.println("type componentType: " + ((Class<?>) type).getComponentType());
    }

    public static void type() throws Exception{
        List<String> list = new ArrayList<String>(0);
        Class cls = list.getClass();
        Type type = (Type) cls;
        System.out.println("generictype=====================");
        System.out.println("type instanceof Class: " + (type instanceof Class));
        System.out.println("type == ArrayList.class: " + (type == ArrayList.class));
        System.out.println("type instanceof List: " + (type instanceof List));
        System.out.println("List.class.isInstance: " + (List.class.isAssignableFrom(cls)));
        cls = XmlModel.class;
        Field field = cls.getDeclaredField("list");
        type = field.getGenericType();
        System.out.println("fieldtype==================");
        System.out.println("type instanceof Class: " + (type instanceof Class));
        System.out.println("type instanceof ParameterizedType: " + (type instanceof ParameterizedType));
        System.out.println("type instanceof List:" + (type instanceof ArrayList));
        if (type instanceof ParameterizedType) {
            Type sub = ((ParameterizedType) type).getRawType();
            System.out.println("type raw: " + sub);
            System.out.println("rawtype instanceof List: " + (sub == List.class));
            System.out.println("List.class.isAssignableFrom rawtype: " + (List.class.isAssignableFrom((Class)sub)));
            sub = ((ParameterizedType) type).getActualTypeArguments()[0];
            System.out.println("type generic: " + sub);
        }

        field = cls.getDeclaredField("a");
        type = field.getGenericType();
        System.out.println("simpletype===================");
        System.out.println("type instanceof Class: " + (type instanceof Class));
        System.out.println("type == int.class:" + (type == int.class));
        System.out.println("int.class.isAssignableFrom: " + (int.class.isAssignableFrom((Class)type)));
        System.out.println("type instanceof ParameterizedType: " + (type instanceof ParameterizedType));

    }

    public static void typeToken() {
        Type type = new TypeToken<List<String>>() {}.getType();
        if (type instanceof ParameterizedType) {
            Type t1 = ((ParameterizedType) type).getRawType();
            // list
            System.out.println((Class) t1);
            Type t2 = ((ParameterizedType) type).getActualTypeArguments()[0];
            // string
            System.out.println((Class) t2);
            if (String.class == t2) {
                System.out.println("type is String");
            }
        }
    }

    public static void field() throws Exception {
        Class cls = XmlModel.class;
        XmlUtilsTest x = new XmlUtilsTest();
        XmlModel m = x.new XmlModel();
        XmlModel obj = (XmlModel) cls.getConstructor(XmlUtilsTest.class).newInstance(x);
        Field[] fs = cls.getDeclaredFields();
        System.out.println("field: " + fs.length);
        Object fobj = null;
        for (Field f : fs) {
            f.setAccessible(true);
            System.out.print(f.getName() + ": " + f.getType());
            Type type = f.getGenericType();
            if (type instanceof ParameterizedType) {
                Type[] ts = ((ParameterizedType) type).getActualTypeArguments();
                if (ts.length > 0)
                    System.out.println(": " + ts[0]);
            }
            System.out.println();
        }

        System.out.println(StringUtils.toStr(obj));
    }

    @Data
    public class XmlModel {
        private int a = 1;
        private Integer a2 = 2;
        private byte b = 2;
        private short c = 3;
        private long d = 4;
        private double e = 5;
        private float f = 6;
        private String g = "7";
        private boolean h = true;
        private String[] arr = {"a", "b", "c"};
        private List<String> list;
        private ArrayList<String> list2;
        private Set<String> set;
        private HashSet<String> set2;
        private Map<String, String> map;
        private HashMap<String, String> map2;

        public XmlModel() {
        }
    }
}
