package io.github.chad2li.baseutil.util;

import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * BeanUtils
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/21 23:02
 */
public class BeanUtils {

    /**
     * 转换
     * <p>
     * 会忽略list中的null，也会忽略func返回的null值
     * </p>
     *
     * @param list original list
     * @param func convert function
     * @return target list
     * @author chad
     * @since 1 by chad at 2023/8/21
     */
    public static <T, R> List<R> convert(List<T> list, Function<T, R> func) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<R> resultList = new ArrayList<>(list.size());
        R r;
        for (T t : list) {
            if (null == t) {
                continue;
            }
            r = func.apply(t);
            if (null != r) {
                resultList.add(r);
            }
        }
        return resultList;
    }

    private BeanUtils() {
        // do nothing
    }
}
