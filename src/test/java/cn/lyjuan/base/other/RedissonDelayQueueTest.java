package cn.lyjuan.base.other;

import cn.lyjuan.base.util.DateUtils;
import cn.lyjuan.base.util.HexUtils;
import cn.lyjuan.base.util.RandomUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redisson延迟队列
 */
public class RedissonDelayQueueTest {
    private RedissonClient client;
    private RBlockingQueue<DemoInfo> blockQueue;
    private RDelayedQueue<DemoInfo> delayQueue;

    private static final String MASTER = "redis://192.168.1.201:7101";
    private static final String SLAVER = "redis://192.168.1.202:7102";
    private static final String PWD = "XXWck7QQQghPbittPNQErNyzxtOhcikVP0KifN3VsKjw8oht4gxN6RgSh3FGbVsPOskBF9AVQMXmjtCIDCrkUx8h10ifWSBcecd";

    @Before
    public void before() {
        Config config = new Config();
        config.useReplicatedServers()
                .addNodeAddress(MASTER)
                .addNodeAddress(SLAVER)
                .setPassword(PWD)
                .setTimeout(3000)
                .setMasterConnectionPoolSize(1)
                .setSlaveConnectionPoolSize(2)
                .setSubscriptionConnectionPoolSize(1)
                .setMasterConnectionMinimumIdleSize(1)
                .setSlaveConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(1);

        client = Redisson.create(config);

//        blockQueue = client.getBlockingQueue("test_block_queue");
//        delayQueue = client.getDelayedQueue(blockQueue);
    }

    @Test
    public void duplicateMember() throws Exception {
        String queueName = "test:for:delayQueue:duplicate";
        RBlockingQueue<String> block = client.getBlockingDeque(queueName);
        RDelayedQueue<String> delay = client.getDelayedQueue(block);
        // 增加
        delay.offer("abc", 10, TimeUnit.SECONDS);
//        delay.offer("2", 10, TimeUnit.SECONDS);
//        delay.offer("1", 5, TimeUnit.SECONDS);
        System.out.println("Delay add all done");

        while (true) {
            String key = block.take();
            System.out.println("Get delay key ==> " + key);
        }
    }

    @Test
    public void delayQueueTake() throws InterruptedException {
        DemoInfo info = null;
        long time = 0;
        int i = 0;
        while (true) {
            i++;
            time = System.currentTimeMillis();
            info = blockQueue.take();
            System.out.println("[" + i + "]-[" + info.id + "] Receive: " + DateUtils.format(info.time, "HH:mm:ss.SSS")
                    + " [" + info.delay + "] ==> " + (time - DateUtils.time2long(info.time)));
        }
    }

    private static AtomicInteger ALL_COUNTER = new AtomicInteger(0);
    private static final int ADD_THREAD_SIZE = 10;
    private static CountDownLatch ADD_DOWN_LATCH = new CountDownLatch(ADD_THREAD_SIZE);

    @Test
    public void delayQueueAdd() throws InterruptedException {
        // 放入数据
        for (int i = 0; i < 1; i++) {
            AddRun run = new AddRun("Thread-" + i, delayQueue);
            run.start();
        }

        ADD_DOWN_LATCH.await();

        System.out.println("All Counte ==> " + ALL_COUNTER.get());
        while (true)/// 等待同步信息
            Thread.sleep(10 * 1000);
    }

    public static class AddRun extends Thread {
        private RDelayedQueue<DemoInfo> queue;

        public AddRun(String name, RDelayedQueue<DemoInfo> queue) {
            super(name);
            this.queue = queue;
        }

        @SneakyThrows
        @Override
        public void run() {
            LocalDateTime time = null;
            int delay = 0;
            int i = 0;
            List<RFuture> list = new ArrayList<>(1 * 10000);
            while (i < (1 * 10000)) {
                ALL_COUNTER.incrementAndGet();
                delay = RandomUtils.randomInt(100, 30 * 1000);
                time = DateUtils.long2Time(System.currentTimeMillis() + delay);
                RFuture f = queue.offerAsync(new DemoInfo(this.getName() + "-" + i++, time, delay), delay, TimeUnit.MILLISECONDS);
                list.add(f);
                System.out.println("[" + this.getName() + "]-[" + i + "] delay to add: " + delay / 1000);
//                Thread.sleep(RandomUtils.randomInt(100, 1000));
            }
            i = 0;
            for (RFuture f : list) {
                f.isSuccess();
                System.out.println("[" + this.getName() + "]-[" + i++ + "] delay to done");
            }

            ADD_DOWN_LATCH.countDown();
        }
    }

    @Test
    public void covert16() {
        String xlf = "2nQ\\x1b\\xd6\\xdb\\xc3\\x06\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x04>\\x03abc";
        String[] arr = xlf.split("\\\\x");
        String result = "";
        for (String s : arr) {
            if (StringUtils.isNull(s)) continue;
            String hexStr = s;
            if (s.length() > 2) {
                hexStr = s.substring(0, 2);
            }
            result += HexUtils.hex2Ten(hexStr);
            if (s.length() > 2)
                result += s.substring(2);
        }
        System.out.println("result ==> " + result);
    }


    @AllArgsConstructor
    public static class DemoInfo implements Serializable {
        public String id;
        public LocalDateTime time;
        public int delay;
    }
}
