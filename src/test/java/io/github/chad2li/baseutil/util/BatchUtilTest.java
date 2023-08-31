package io.github.chad2li.baseutil.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
        int size = 1;
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
        // 1.2
        size = 200;
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
        // 1.3
        size = 201;
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
        // 1.4
        size = 199;
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
        // 1.5
        size = 202;
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

    @Test
    public void batchAllByMaxId() {
        BatchMaxIdDemo demo = Mockito.mock(BatchMaxIdDemo.class);
        List<MaxIdDto> list;

        // 1. 99, 100, 0
        Mockito.when(demo.selectListByMaxId(0L, 200)).thenReturn(list(0L, 99));
        Mockito.when(demo.selectListByMaxId(99L, 200)).thenReturn(list(99L, 100));
        Mockito.when(demo.selectListByMaxId(199L, 200)).thenReturn(list(199, 0));
        list = BatchUtil.batchAllByMaxId((maxId, batchSize) -> demo.selectListByMaxId(maxId, batchSize));
        Assert.assertEquals(199, list.size());
        // 2. 0
        Mockito.when(demo.selectListByMaxId(0L, 200)).thenReturn(list(0L, 0));
        list = BatchUtil.batchAllByMaxId((maxId, batchSize) -> demo.selectListByMaxId(maxId, batchSize));
        Assert.assertEquals(0, list.size());
    }

    public List<MaxIdDto> list(long maxId, int size) {
        List<MaxIdDto> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            list.add(new MaxIdDto(i + maxId, String.valueOf(i + maxId)));
        }

        return list;
    }

    public interface BatchMaxIdDemo {
        List<MaxIdDto> selectListByMaxId(Long maxId, Integer batchSize);
    }

    @Data
    @AllArgsConstructor
    public class MaxIdDto {
        private Long id;
        private String value;
    }
}