package cn.lyjuan.base.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtilsTest {

    @Test
    public void join() {
        List<Integer> list = new ArrayList<>(3);
        list.add(1);
        list.add(3);
        list.add(2);

        System.out.println(ArrayUtils.join(list, ","));
    }
}