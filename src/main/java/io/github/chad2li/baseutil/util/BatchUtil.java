package io.github.chad2li.baseutil.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.chad2li.baseutil.consts.DefaultConstant;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量工具
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/20 18:15
 */
public class BatchUtil {
    public static final int BATCH_SIZE = 200;

    /**
     * @author chad
     * @see BatchUtil#batchAllByMaxId(BatchMaxId, int)
     * @since 1 by chad at 2023/8/24
     */
    public static <R> List<R> batchAllByMaxId(BatchMaxId<R> batchMaxIdFun) {
        return batchAllByMaxId(batchMaxIdFun, BATCH_SIZE);
    }

    /**
     * 根据 maxId 方式分批取所有值
     *
     * @param batchMaxIdFun 根据 maxId 分批取值函数
     * @return list
     * @author chad
     * @since 1 by chad at 2023/8/24
     */
    public static <R> List<R> batchAllByMaxId(BatchMaxId<R> batchMaxIdFun, int batchSize) {
        List<R> allList = new ArrayList<>(100);
        List<R> subList;
        Long maxId = 0L;
        while (true) {
            // 1. 查询
            subList = batchMaxIdFun.apply(maxId, batchSize);
            if (CollUtil.isEmpty(subList)) {
                return allList;
            }
            // 2. 取 maxId
            maxId = maxId(subList);
            // 3. 存值
            allList.addAll(subList);
        }
    }

    /**
     * 取最大ID
     *
     * @param list list
     * @return max id
     * @author chad
     * @since 1 by chad at 2023/8/24
     */
    private static <T> Long maxId(List<T> list) {
        Optional<T> o = list.stream().max((a, b) -> {
            Long idA = (Long) ReflectUtil.getFieldValue(a, DefaultConstant.Db.ID);
            Long idB = (Long) ReflectUtil.getFieldValue(b, DefaultConstant.Db.ID);
            return NumberUtil.compare(idA, idB);
        });
        return o.map(t -> (Long) ReflectUtil.getFieldValue(t, DefaultConstant.Db.ID)).orElse(null);

    }

    @FunctionalInterface
    public interface BatchMaxId<R> {
        /**
         * 使用maxId模式查询所有数据
         *
         * @param maxId     maxId
         * @param batchSize 分页大小
         * @return 当前分页数据，如果无数据返回空集合
         * @author chad
         * @since 1 by chad at 2023/8/24
         */
        List<R> apply(Long maxId, Integer batchSize);
    }

    /**
     * 分批处理
     *
     * @param list      需要分批处理的数据
     * @param function  分批处理的函数
     * @param batchSize 每次分批数量，如：200
     * @return 分批结果
     * @author chad
     * @since 1 by chad at 2023/8/20
     */
    public static <T, R> List<R> batch(Collection<T> list, Function<List<T>, List<R>> function,
                                       int batchSize) {
        if (!NumberUtils.isPositive(batchSize)) {
            throw new IllegalArgumentException("batchSize must > 0");
        }
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        int index = 0;
        int allSize = list.size();
        List<T> subList = new ArrayList<>(batchSize);
        List<R> subResultList;
        List<R> allResultList = new ArrayList<>(allSize);
        for (T t : list) {
            // index 从1开始
            index++;
            subList.add(t);
            if (0 != (index % batchSize) && index != allSize) {
                // 1.未达到分页数量
                // 2.不是最后一个
                continue;
            }
            // 执行一次
            subResultList = function.apply(subList);
            if (CollUtil.isNotEmpty(subResultList)) {
                allResultList.addAll(subResultList);
            }
            // 清除已执行结果
            subList.clear();
        }
        return allResultList;
    }

    /**
     * 分批处理，默认分批数量为 {@link BatchUtil#BATCH_SIZE}
     *
     * @author chad
     * @see BatchUtil#batch(Collection, Function, int)
     * @since 1 by chad at 2023/8/20
     */
    public static <T, R> List<R> batch(Collection<T> list, Function<List<T>, List<R>> function) {
        return batch(list, function, BATCH_SIZE);
    }

    /**
     * 分批处理，并且忽略结果
     *
     * @author chad
     * @see BatchUtil#batch(Collection, Function, int)
     * @since 1 by chad at 2023/8/20
     */
    public static <T> void batch(Collection<T> list, Consumer<List<T>> consumer, int batchSize) {
        batch(list, it -> {
            consumer.accept(it);
            return null;
        }, batchSize);
    }

    /**
     * 分批处理，并且忽略结果
     *
     * @author chad
     * @see BatchUtil#batch(Collection, Function, int)
     * @since 1 by chad at 2023/8/20
     */
    public static <T> void batch(Collection<T> list, Consumer<List<T>> consumer) {
        batch(list, it -> {
            consumer.accept(it);
            return null;
        }, BATCH_SIZE);
    }

    private BatchUtil() {
        // do nothing
    }
}
