package io.github.chad2li.baseutil.util;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * BatchUtilTest
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/20 18:32
 */
public class BatchUtilTest {

    @Test
    public void batch() {
        List<Integer> list = null;
        List<String> result = null;
        // 1.1
        int size = 500;
        list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        result = BatchUtil.batch(list, it -> {
            List<String> subList = new ArrayList<>(it.size());
            for (Integer i : it) {
                subList.add(String.valueOf(i));
            }
            return subList;
        });
        Assert.assertEquals(size, result.size());
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(String.valueOf(i), result.get(i));
        }
    }
}