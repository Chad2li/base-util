package cn.lyjuan.base.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by ly on 2014/12/23.
 */
public class ReflectUtils
{
    /**
     * 解析类中有 Getter 和 Setter 方法的属性
     *
     * @param clazz 需要解析的类
     * @param clazz 解析最上级的类，该类的属性不解析
     * @return
     */
    public static Set<String> parseMember(Class<?> clazz)
    {
        Set<String> members = new HashSet<String>();
        Field[] fs = clazz.getDeclaredFields();
        boolean hasGetMethod = false;// 是否有get方法
        boolean hasSetMethod = false;// 是否有set方法
        String getMethodName = null;// get方法名
        String setMethodName = null;// set方法名
        for (Field f : fs)
        {
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
     * @return
     */
    public static boolean hasMethod(Class<?> clazz, String method, Class<?>... types)
    {
        try
        {
            if (null == types)
                clazz.getMethod(method);
            else
                clazz.getMethod(method, types);
            return true;
        } catch (NoSuchMethodException e)
        {
            // do not have this method
            // drop this throw
        }

        return false;
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
    public static Object getValue(Object obj, String memberName)
    {
        // getter 方法名
        memberName = genMemberGetSetName(memberName, true);

        Method getter = null;
        Boolean isAcc = null;
        try
        {
            getter = obj.getClass().getMethod(memberName);
            isAcc = getter.isAccessible();// 改变访问控制
            getter.setAccessible(true);

            return getter.invoke(obj);
        } catch (NoSuchMethodException e)
        {
            // do not have this method
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            // this method invoke error
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            // this method parameters error
            throw new RuntimeException(e);
        } finally
        {
            if (null == getter && null == isAcc)
                getter.setAccessible(isAcc);
        }
    }

    public static <T> Object getValueNoThrow(T obj, String memeberName)
    {
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
    public static <T> Object getValueNoThrow(T obj, Class<? super T> fromClass, String memberName)
    {
        Method getter = null;
        Boolean isAccMethod = null;
        Field field = null;
        Boolean isAccAttribute = null;

        try
        {
            field = fromClass.getDeclaredField(memberName);

            // getter 方法名
            String getName = genMemberGetSetName(memberName, true);

            boolean hasGet = hasMethod(obj.getClass(), memberName, field.getType());

            if (hasGet)
            {
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
        } catch (Exception e)
        {
            return null;
        } finally
        {
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
    public static void setValue(Object obj, String memberName, Object value)
    {
        Field field = null;
        Boolean isAccAttribute = null;
        Method setter = null;
        Boolean isAccMethod = null;
        try
        {
            field = obj.getClass().getDeclaredField(memberName);

            // setter 方法名
            memberName = genMemberGetSetName(memberName, false);

            setter = null;
            setter = obj.getClass().getMethod(memberName, field.getType());
            isAccMethod = setter.isAccessible();
            setter.setAccessible(true);

            setter.invoke(obj, value);
        } catch (NoSuchMethodException e)
        {
            // do not has this method
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            // this method invoke error
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            // this method parameters error
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e)
        {
            // not such field
            throw new RuntimeException(e);
        } finally
        {
            if (null != setter && null != isAccMethod)
                setter.setAccessible(isAccMethod);
        }
    }

    /**
     * 获取属性的 Getter 或 Setter 方法名
     *
     * @param memberName 属性名
     * @param isGet      为 true 表示获取 Getter 方法名;为 false 表示获取 Setter 方法名
     * @return
     */
    public static String genMemberGetSetName(String memberName, boolean isGet)
    {
        if (memberName.length() > 1)
            memberName = memberName.substring(0, 1).toUpperCase() + memberName.substring(1);
        else
            memberName = memberName.toUpperCase();

        if (isGet)
            return "get" + memberName;

        return "set" + memberName;
    }

    /**
     * 返回类属性的类型
     *
     * @param clazz  类
     * @param member 属性名
     * @return
     * @throws NoSuchFieldException
     */
    public static Class<?> returnMemberType(Class<?> clazz, String member)
    {
        Field f = null;
        try
        {
            f = clazz.getDeclaredField(member);
        } catch (NoSuchFieldException e)
        {
            // do not has this member
            throw new RuntimeException(e);
        }

        return f.getType();
    }

    /**
     * 获取泛型有实际使用中的类型
     *
     * @param cls   使用泛型的类
     * @param index 第几个泛型
     * @return
     */
    public static Class<Object> getGenericityClass(Class cls, int index)
    {
        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        Type genType = cls.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType))
        {
            return Object.class;
        }
        //返回表示此类型实际类型参数的 Type 对象的数组。
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0)
        {
            return Object.class;
        }
        if (!(params[index] instanceof Class))
        {
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Map<String, Object> membersToMap(Object from)
    {
        return membersToMap(from, null == from ? null : from.getClass(), Object.class);
    }

    /**
     * 反射获取对象的所有属性和值封装成Map
     *
     * @param from        属性来源
     * @param fromClass   属性所在的目标类
     * @param targetClass 递归到该类则结束，该类的属性不返回
     * @return 包含该类及其父类的所有属性。如果对象为空，则返回包含0个元素的Map
     */
    public static Map<String, Object> membersToMap(Object from, Class fromClass, Class targetClass)
    {
        if (null == from || null == fromClass) return new HashMap<>(0);

        if (null != targetClass)
        {
            if (from.getClass().isAssignableFrom(targetClass)
                    || fromClass == targetClass)
                return new HashMap<>(0);
        }

        Map<String, Object> map = new HashMap<>();

        Field[] fs = fromClass.getDeclaredFields();

        String name = null;
        for (Field f : fs)
        {
            name = f.getName();

            map.put(name, getValueNoThrow(from, fromClass, name));
        }

        map.putAll(membersToMap(from, fromClass.getSuperclass(), targetClass));

        return map;
    }

    /**
     * 在父类迭代获取field
     *
     * @param cls
     * @param name
     * @return
     */
    public static Field field(Class cls, String name)
    {
        Field f = null;

        try
        {
            if (null == cls)
                throw new NoSuchFieldException("no such field " + name);
            f = cls.getDeclaredField(name);
        } catch (NoSuchFieldException e)
        {
            // ignore
        }

        if (null == f && null != cls)
        {
            f = field(cls.getSuperclass(), name);
        }

        return f;
    }

    /**
     * 在父类迭代获取method
     *
     * @param cls
     * @param name
     * @return
     */
    public static Method method(Class cls, String name, Class... types)
    {
        Method m = null;

        try
        {
            if (null == cls)
                throw new NoSuchMethodException("no such method " + name + " with parameters: " + Arrays.toString(types));

            m = cls.getDeclaredMethod(name, types);
        } catch (NoSuchMethodException e)
        {
            // ignore
        }

        if (null == m && null != cls)
            m = method(cls.getSuperclass(), name, types);

        return m;
    }
}
