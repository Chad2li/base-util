package io.github.chad2li.baseutil.thread.task;

import io.github.chad2li.baseutil.util.RandomUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestAtomic {
    private static final AtomicInteger ato1 = new AtomicInteger(0);
    private static final AtomicInteger ato2 = new AtomicInteger(0);

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 2; i++)
            new RunDemo(i).start();

        while (true)
            Thread.sleep(10 * 1000);
    }

    public static class RunDemo extends Thread {
        private boolean even;

        public RunDemo(int i) {
            super("t-" + i);
            even = i % 2 == 1;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(RandomUtils.randomInt(100, 500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (even) {
                    int count1 = ato1.incrementAndGet();
                    System.out.println("count1 ==> " + count1);
                } else {
                    int count2 = ato2.incrementAndGet();
                    System.out.println("count2 ==> " + count2);
                }
            }
        }
    }
}
