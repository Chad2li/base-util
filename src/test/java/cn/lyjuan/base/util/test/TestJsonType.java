package cn.lyjuan.base.util.test;

import cn.lyjuan.base.util.JsonUtilsTest;
import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by chad on 2016/8/12.
 */
public class TestJsonType
{
    public static void main(String[] args)
    {
        printType(JsonUtilsTest.UserBean.class);
    }

    public static <T> void printType(T c)
    {
        Type superclass = c.getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;

        System.out.println(parameterized);
        $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }
}
