package io.github.chad2li.baseutil.thread.task;

import io.github.chad2li.baseutil.util.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程任务测试
 */
public class TaskCtlTest {

    private TaskStore<TaskPData, TaskCData> taskStore;
    private TaskCtl taskCtl;
    private TaskServiceImpl taskService;

    // 关键是生产者获取原始数据是同步的
    private AtomicInteger initDate = new AtomicInteger(100);

    @Before
    public void before() {
        taskStore = new TaskStore<>();
        taskCtl = new TaskCtl(taskStore);
        taskService = new TaskServiceImpl();
    }

    @Test
    public void test() throws InterruptedException {
        // 生产者
        TaskProducer<TaskPData, TaskCData> p1 = new TaskProducer<>("p-1", taskCtl, taskStore, taskService, new TaskPData(initDate));
        TaskProducer<TaskPData, TaskCData> p2 = new TaskProducer<>("p-2", taskCtl, taskStore, taskService, new TaskPData(initDate));

        // 消费者
        TaskConsumer<TaskPData, TaskCData> c1 = new TaskConsumer<>("c-1", taskCtl, taskStore, taskService);
        TaskConsumer<TaskPData, TaskCData> c2 = new TaskConsumer<>("c-2", taskCtl, taskStore, taskService);

        // start
        p1.start();
        p2.start();
        c1.start();
        c2.start();

        while (true)
            Thread.sleep(10 * 1000);
    }

    /**
     * 任务中心
     */
    @Slf4j
    public static class TaskServiceImpl implements ITaskService<TaskPData, TaskCData> {
        @Override
        public TaskCData produce(TaskPData taskPData) {
            try {
                Thread.sleep(RandomUtils.randomInt(100, 300));
            } catch (InterruptedException e) {
                log.debug("[{}] wait producer interrupted", Thread.currentThread().getName());
            }
            int index = taskPData.getAndDecr();
            if (index < 1) return null;
            return new TaskCData(Thread.currentThread().getName() + "-" + index);
        }

        @Override
        public boolean consumer(TaskCData taskCData) {
            try {
                Thread.sleep(RandomUtils.randomInt(100, 300));
            } catch (InterruptedException e) {
                log.debug("[{}] wait producer interrupted", Thread.currentThread().getName());
            }
            System.out.println(Thread.currentThread().getName() + " ==> " + taskCData.getName());
            return true;
        }
    }


    /**
     * 生产者原始数据
     */
    public static class TaskPData {
        private AtomicInteger index;

        public TaskPData(AtomicInteger index) {
            this.index = index;
        }

        public int getAndDecr() {
            return index.getAndDecrement();
        }
    }

    /**
     * 生产者产出和消费者消费的数据
     */
    @AllArgsConstructor
    public static class TaskCData {
        @Getter
        private String name;
    }
}