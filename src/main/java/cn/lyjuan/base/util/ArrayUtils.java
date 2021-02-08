package cn.lyjuan.base.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ly on 2014/12/22.
 */
public class ArrayUtils {
    /**
     * 使用分隔{@Code separator} 拼接数组
     *
     * @param objArr    数组
     * @param separator 分隔符
     * @return
     */
    public static String join(Object[] objArr, String separator) {
        if (null == objArr || objArr.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        Object obj = null;
        for (int i = 0; i < objArr.length; i++) {
            obj = Array.get(objArr, i);
            if (StringUtils.isNull(obj))
                sb.append(separator);
            else
                sb.append(StringUtils.toStr(objArr).trim()).append(separator);
        }

        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

    public static String join(Collection coll, String sep) {
        if (null == coll || coll.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        for (Object c : coll) {
            sb.append(StringUtils.toStr(c).trim()).append(sep);
        }

        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * 使用逗号分隔数组
     *
     * @param array
     * @return
     */
    public static String join(Object[] array) {
        return join(array, ",");
    }

    /**
     * 去掉数组中的空数据
     *
     * @param arr
     * @return
     */
    public static String[] arrUnnull(String[] arr) {
        if (null == arr || arr.length < 1) return null;

        List<String> list = new ArrayList<String>();

        for (String s : arr) {
            if (StringUtils.isNull(s))
                continue;

            list.add(s);
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * 将字符串拆分成数组，并去掉空字符串
     *
     * @param str
     * @param separate
     * @return
     */
    public static String[] splitUnnull(String str, String separate) {
        if (StringUtils.isNull(str)) return null;

        String[] arr = str.split(separate);

        if (StringUtils.isNull(arr)) return null;

        return arrUnnull(arr);
    }
}
