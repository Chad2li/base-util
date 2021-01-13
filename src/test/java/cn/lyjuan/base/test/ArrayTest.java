package cn.lyjuan.base.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayTest {
    @Test
    public void test() {
        print(1, 2, 3);

        List<String> list = new ArrayList<>(3);
        list.add("a");
        list.add("b");
        list.add("c");
        print(list);

    }

    public static <T> void print(T... v) {
        System.out.println(v.getClass().getSimpleName());
        Arrays.stream(v).forEach(item -> {
            System.out.println(item);
        });
//        for (int i = 0; i < v.length; i++)
//            System.out.println(i + ": " + v[i]);
    }
}
