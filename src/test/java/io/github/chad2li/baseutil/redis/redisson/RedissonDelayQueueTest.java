package io.github.chad2li.baseutil.redis.redisson;

import io.github.chad2li.baseutil.redis.redisson.codec.CustomJsonJacksonCodec;
import io.github.chad2li.baseutil.util.DateUtils;
import io.github.chad2li.baseutil.util.HexUtils;
import io.github.chad2li.baseutil.util.RandomUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redisson延迟队列
 */
@Ignore
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

        CustomJsonJacksonCodec codec = new CustomJsonJacksonCodec(createObjectMapper());

        config.setCodec(codec)
                .useReplicatedServers()
                .addNodeAddress(MASTER)
                .addNodeAddress(SLAVER)
                .setPassword(PWD)
                .setTimeout(3000)
                .setMasterConnectionPoolSize(5)
                .setSlaveConnectionPoolSize(5)
                .setSubscriptionConnectionPoolSize(5)
                .setMasterConnectionMinimumIdleSize(5)
                .setSlaveConnectionMinimumIdleSize(5)
                .setSubscriptionConnectionPoolSize(5);

        client = Redisson.create(config);

        blockQueue = client.getBlockingQueue("test_block_queue2");
        delayQueue = client.getDelayedQueue(blockQueue);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 为null的数据不序列化
        mapper.setDateFormat(new SimpleDateFormat(DateUtils.FMT_DATE_TIME));
        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 下面配置解决LocalDateTime序列化的问题
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //日期序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        //日期反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE_TIME)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_DATE)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.FMT_TIME)));

        mapper.registerModule(javaTimeModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    @Test
    public void duplicateMember() throws Exception {
        String queueName = "test:for:delayQueue:duplicate";

        RBlockingQueue<String> block = client.getBlockingDeque(queueName);// 获取
        RDelayedQueue<String> delay = client.getDelayedQueue(block);// 加
        // 增加
        delay.offer("a", 10, TimeUnit.SECONDS);
        delay.offer("b", 10, TimeUnit.SECONDS);
        delay.offer("c", 10, TimeUnit.SECONDS);
        Thread.sleep(2 * 1000);
        // 覆盖
        delay.remove("a");
        delay.offer("a", 10, TimeUnit.SECONDS);
        Thread.sleep(5 * 1000);
//        delay.offer("2", 10, TimeUnit.SECONDS);
//        delay.offer("1", 5, TimeUnit.SECONDS);
        System.out.println("Delay add all done");

        int i = 0;
        while (true) {
            System.out.println("block take in " + i++);
            String key = block.take();// 阻塞获取
            System.out.println("Get delay key ==> " + key);
        }
    }

    @Test
    public void delayQueueTake() throws InterruptedException {
        DemoInfo f = null;
        long time = 0;
        int i = 0;
        ExecutorService es = Executors.newFixedThreadPool(8);

        while (true) {
            f = blockQueue.take();
            es.submit(new TakeRun(i++, f));
        }
    }

    public static class TakeRun implements Runnable {
        private int i;
        private DemoInfo info;

        public TakeRun(int i, DemoInfo info) {
            this.i = i;
            this.info = info;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
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
        for (int i = 0; i < 10; i++) {
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
            RFuture f = null;
            while (i < 10 * 1000) {
                ALL_COUNTER.incrementAndGet();
                delay = RandomUtils.randomInt(100, 30 * 1000);
                time = DateUtils.long2Time(System.currentTimeMillis() + delay);
//                queue.offer(new DemoInfo(this.getName() + "-" + i++, time, delay), delay, TimeUnit.MILLISECONDS);
                f = queue.offerAsync(new DemoInfo(this.getName() + "-" + i++, time, delay), delay, TimeUnit.MILLISECONDS);
                queue.remove(null);
                list.add(f);
                System.out.println("[" + this.getName() + "]-[" + i + "] delay to add: " + delay / 1000);
                Thread.sleep(RandomUtils.randomInt(100, 1000));
            }
            i = 0;
            for (RFuture f1 : list) {
                f1.isSuccess();
                System.out.println("[" + this.getName() + "]-[" + i++ + "] delay to done");
            }

            ADD_DOWN_LATCH.countDown();
        }
    }

    @Test
    public void covert16() {
        String xlf = "\\x04>\\x1B";
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

    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoInfo implements Serializable {
        public String id;
        public LocalDateTime time;
        public int delay;
    }
}
