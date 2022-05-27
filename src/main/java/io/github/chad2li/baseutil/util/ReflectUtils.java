package io.github.chad2li.baseutil.util;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Created by ly on 2014/12/23.
 */
public class ReflectUtils {
    /**
     * 解析类中有 Getter 和 Setter 方法的属性
     *
     * @param clazz 需要解析的类
     * @param clazz 递归解析的最上级的类（该类的属性不解析）
     * @return
     */
    public static Set<String> parseMember(Class<?> clazz) {
        Set<String> members = new HashSet<String>();
        Field[] fs = clazz.getDeclaredFields();
        boolean hasGetMethod = false;// 是否有get方法
        boolean hasSetMethod = false;// 是否有set方法
        String getMethodName = null;// get方法名
        String setMethodName = null;// set方法名
        for (Field f : fs) {
            if (skipField(f)) continue;

            getMethodName = genMemberGetSetName(f.getName(), true);
            setMethodName = genMemberGetSetName(f.getName(), false);

            hasGetMethod = hasMethod(clazz, getMethodName);
            hasSetMethod = hasMethod(clazz, setMethodName, f.getType());

            if (hasGetMethod && hasSetMethod)
                members.add(f.getName());
        }

        return members;
    }

    /**
     * 判断类是否有该属性，并且只获取属性有Getter和Setter方法的属性
     *
     * @param clazz  类名
     * @param method 方法名
     * @param types  方法参数值
     * @return true有该方法，false无该方法
     */
    public static boolean hasMethod(Class<?> clazz, String method, Class<?>... types) {
        try {
            Method m = method(clazz, method, types);

            return null != m;

        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 通过 Get 方法获取属性值
     *
     * @param obj        对象
     * @param memberName 属性名
     * @return
     * @throws NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getValue(Object obj, String memberName) {
        // getter 方法名
        memberName = genMemberGetSetName(memberName, true);

        Method getter = null;
        Boolean isAcc = null;
        try {
            getter = obj.getClass().getMethod(memberName);
            isAcc = getter.isAccessible();// 改变访问控制
            getter.setAccessible(true);

            return getter.invoke(obj);
        } catch (NoSuchMethodException e) {
            // do not have this method
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // this method invoke error
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // this method parameters error
            throw new RuntimeException(e);
        } finally {
            if (null != getter && null != isAcc)
                getter.setAccessible(isAcc);
        }
    }

    public static <T> Object getValueNoThrow(T obj, String memeberName) {
        return getValueNoThrow(obj, (Class<? super T>) obj.getClass(), memeberName);
    }

    /**
     * 获取方法，如果获取失败，返回 null替代报错
     *
     * @param obj
     * @param fromClass  属性所属的类
     * @param memberName
     * @return
     */
    public static <T> Object getValueNoThrow(T obj, Class<? super T> fromClass, String memberName) {
        Method getter = null;
        Boolean isAccMethod = null;
        Field field = null;
        Boolean isAccAttribute = null;

        try {
            field = fromClass.getDeclaredField(memberName);

            // getter 方法名
            String getName = genMemberGetSetName(memberName, true);

            boolean hasGet = hasMethod(obj.getClass(), memberName, field.getType());

            if (hasGet) {
                getter = fromClass.getMethod(getName, null);
                isAccMethod = getter.isAccessible();
                getter.setAccessible(true);

                return getter.invoke(obj, null);
            }

            // 无 getter 方法，直接通过属性获取
            isAccAttribute = field.isAccessible();
            field.setAccessible(true);
            Object val = field.get(obj);
            return val;
        } catch (Exception e) {
            return null;
        } finally {
            if (null != getter && null != isAccMethod)
                getter.setAccessible(isAccMethod);
            if (null != field && null != isAccAttribute)
                field.setAccessible(isAccAttribute);
        }
    }

    /**
     * 通过 Setter 方法设置属性值
     *
     * @param obj        设置对象
     * @param memberName 属性名称
     * @param value      欲设置的值
     * @throws NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void setValue(Object obj, String memberName, Object value) {
        Field field = null;
        Method setter = null;
        boolean isAcc = true;
        try {
            field = field(obj.getClass(), memberName);
            if (null == field)
                throw new RuntimeException("not " + memberName + " in " + obj.getClass().getName());

            // setter 方法名
            String methodName = genMemberGetSetName(memberName, false);
            setter = method(obj.getClass(), methodName, field.getType());
            if (null != setter)// setter 方法
            {
                isAcc = setter.isAccessible();
                setter.setAccessible(true);
                setter.invoke(obj, value);
            } else // 直接设置属性
            {
                isAcc = field.isAccessible();
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (!isAcc) {
                if (null != setter)
                    setter.setAccessible(isAcc);
                else if (null != field)
                    field.setAccessible(isAcc);
            }
        }
    }

    /**
     * 获取属性的 Getter 或 Setter 方法名
     *
     * @param memberName 属性名
     * @param isGet      为 true 表示获取 Getter 方法名;为 false 表示获取 Setter 方法名
     * @return
     */
    public static String genMemberGetSetName(String memberName, boolean isGet) {
        if (memberName.length() > 1)
            memberName = memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
        else
            memberName = memberName.toUpperCase();

        if (isGet)
            return "get" + memberName;

        return "set" + memberName;
    }

    /**
     * 获取泛型有实际使用中的类型
     *
     * @param cls   使用泛型的类
     * @param index 第几个泛型
     * @return
     */
    public static Class<Object> getGenericityClass(Class cls, int index) {
        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        Type genType = cls.getGenericSuperclass();

        if (null == genType) return null;

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        //返回表示此类型实际类型参数的 Type 对象的数组。
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            return null;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * 调用{@link ReflectUtils#membersToMap(Object, Class, Class, boolean)}，并且跳过{@code transient}属性
     *
     * @param from
     * @return
     */
    public static Map<String, Object> membersToMap(Object from) {
        return membersToMap(from, null == from ? null : from.getClass(), Object.class, true);
    }

    /**
     * 反射获取对象的所有属性和值封装成Map
     *
     * @param from          属性来源
     * @param fromClass     属性所在的目标类
     * @param targetClass   递归到该类则结束，该类的属性不返回
     * @param skipTransient 是否跳过{@code transient修饰的属性}
     * @return 包含该类及其父类的所有属性。如果对象为空，则返回包含0个元素的Map
     */
    public static Map<String, Object> membersToMap(Object from, Class fromClass, Class targetClass, boolean skipTransient) {
        if (null == from || null == fromClass) return new HashMap<>(0);

        if (null != targetClass) {
            if (from.getClass().isAssignableFrom(targetClass)
                    || fromClass == targetClass)
                return new HashMap<>(0);
        }

        Map<String, Object> map = new HashMap<>();

        Field[] fs = fromClass.getDeclaredFields();

        String name = null;
        for (Field f : fs) {
            name = f.getName();
            if (skipTransient && skipField(f)) continue;

            map.put(name, getValueNoThrow(from, fromClass, name));
        }

        map.putAll(membersToMap(from, fromClass.getSuperclass(), targetClass, skipTransient));

        return map;
    }

    /**
     * 在父类迭代获取field
     *
     * @param cls
     * @param name
     * @return
     */
    public static Field field(Class cls, String name) {
        Field f = null;

        try {
            if (null == cls)
                return null;
            f = cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            // ignore
        }

        if (null == f && null != cls.getSuperclass())
            f = field(cls.getSuperclass(), name);

        return f;
    }

    /**
     * 获取类的属性，详情{@link ReflectUtils#fields(Class, boolean)}
     *
     * @param cls
     * @return java.util.Map<java.lang.String, java.lang.reflect.Field>
     * @date 2021/10/14 14:25
     * @author chad
     * @since 1 by chat at 2021/10/14 兼容方法
     */
    public static Map<String, Field> fields(Class<?> cls) {
        return fields(cls, true);
    }

    /**
     * 获取类属性
     * <p>
     * 排除 transient, static, final修饰的属性
     * </p>
     *
     * @param cls          类型
     * @param mustGetField true仅获取有get方法的属性；false获取所有属性
     * @return java.util.Map<java.lang.String, java.lang.reflect.Field>
     * @date 2021/10/14 14:25
     * @author chad
     * @since 1 by chad at 2021/10/14 增加 {@code mustGetField}参数
     */
    public static Map<String, Field> fields(Class cls, boolean mustGetField) {

        Map<String, Field> fields = new HashMap();

        Field[] tmp = cls.getDeclaredFields();
        for (Field f : tmp) {
            if (skipField(f)) continue;
            if (mustGetField) {
                String name = f.getName();
                // getXXX
                String getMethodName = genMemberGetSetName(f.getName(), true);
                boolean hasGetMethod = hasMethod(cls, getMethodName, null);
                // isXXX
                if (!hasGetMethod) {
                    if (!name.startsWith("is")) {
                        char[] cs = name.toCharArray();
                        char first = cs[0];
                        if (first >= 'a' && first <= 'z') {
                            cs[0] = (char) (first - 32);
                        }
                        getMethodName = "is" + String.valueOf(cs);
                    }
                    // is
                    hasGetMethod = hasMethod(cls, getMethodName, null);
                }
                // hasXXX
                if (!hasGetMethod) {
                    if (!name.startsWith("has")) {
                        char[] cs = name.toCharArray();
                        char first = cs[0];
                        if (first >= 'a' && first <= 'z') {
                            cs[0] = (char) (first - 32);
                        }
                        getMethodName = "has" + String.valueOf(cs);
                    }
                    // has
                    hasGetMethod = hasMethod(cls, getMethodName, null);
                }
                // 同名
                if (!hasGetMethod) {
                    hasGetMethod = hasMethod(cls, f.getName(), null);
                }
                // 没有获取属性值的方法
                if (!hasGetMethod) {
                    continue;
                }
            }
            fields.put(f.getName(), f);
        }

        if (null != cls.getSuperclass())
            fields.putAll(fields(cls.getSuperclass()));

        return fields;
    }

    /**
     * 在父类迭代获取method
     *
     * @param cls
     * @param name
     * @return
     */
    public static Method method(Class cls, String name, Class... types) {
        Method m = null;

        try {
            if (null == cls)
                throw new NoSuchMethodException("no such method " + name + " with parameters: " + Arrays.toString(types));

            m = cls.getDeclaredMethod(name, types);
        } catch (NoSuchMethodException e) {
            // ignore
        }

        if (null == m && null != cls)
            m = method(cls.getSuperclass(), name, types);

        return m;
    }

    /**
     * 根据类和值生成Bean
     *
     * @param cls    类，该类需要有public的空构造器
     * @param values 生成类需要的值
     * @param <T>
     * @return
     */
    public static <T> T genBean(Class<T> cls, Map<String, Object> values) {
        T t = null;
        try {
            t = cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, Object> e : values.entrySet()) {
            ReflectUtils.setValue(t, e.getKey(), e.getValue());
        }

        return t;
    }

    /**
     * 解析时是否跳过该属性，跳过：
     * <p>
     * 1. static修饰的<br/>
     * 2. transient修饰的<br/>
     * 3. final修饰的
     * </P>
     *
     * @param field 属性
     * @return
     */
    private static boolean skipField(Field field) {
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isTransient(mod) || Modifier.isFinal(mod);
    }

    /**
     * 是否Java基础类型
     *
     * @param obj 该对象/类是否为Java基础类
     * @return
     */
    public static boolean isBaseClass(Object obj) {
        if (null == obj)
            return false;
        Class<?> cls = obj.getClass();
        return
                // 基本类型
                Integer.class == cls ||
                        Boolean.class == cls ||
                        Byte.class == cls ||
                        Short.class == cls ||
                        Long.class == cls ||
                        Double.class == cls ||
                        Float.class == cls ||
                        String.class == cls ||
                        // 时间类型
                        LocalDateTime.class == cls ||
                        LocalDate.class == cls ||
                        LocalTime.class == cls ||
                        Date.class == cls
                //
                ;
    }
}
