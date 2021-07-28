package cn.lyjuan.base.thread.task;

import cn.lyjuan.base.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class TaskStore<P, C> {
    /**
     * 防止内存溢出
     */
    public int MAX_SIZE = 1000;
    /**
     * 生产者生产的数据，由消费者处理
     */
    private List<C> STORE = new ArrayList<>(MAX_SIZE * 2);

    /**
     * 是否还有任务数据需要消费
     *
     * @return true还有
     */
    public boolean hasConsumeData() {
        return STORE.size() > 1;
    }

    /**
     * 新增下载完成图片
     *
     * @param dto 存储内容
     * @return true放入成功，false放入失败
     */
    public boolean add(C dto) {
        synchronized (STORE) {
            if (STORE.size() > MAX_SIZE) {
                try {
                    STORE.wait();
                } catch (InterruptedException e) {
                    log.warn("[{}] wait add interrupted", Thread.currentThread().getName());
                }
            }

//            if (STORE.size() > MAX_SIZE)// 未放入成功
//                return false;

            STORE.add(dto);
//            if (STORE.size() > MIN_SIZE) {// 消费者批处理
            STORE.notifyAll();// 唤醒消费者
//            }
            return true;
        }
    }

    /**
     * 批量添加
     *
     * @param dtos
     * @return
     */
    public boolean addAll(Collection<C> dtos) {
        if (StringUtils.isNull(dtos)) return true;
        synchronized (STORE) {
            while (true) {
                if (STORE.size() + dtos.size() <= MAX_SIZE)// 能放下
                    break;
                try {
                    STORE.wait();
                } catch (InterruptedException e) {
                    // 被中断后直接放入
                    log.warn("[{}] wait add interrupted", Thread.currentThread().getName());
                }
            }

            STORE.addAll(dtos);
            STORE.notifyAll();// 唤醒消费者
            return true;
        }
    }

    /**
     * 获取下载图片的内容
     *
     * @return null无数据可用
     */
    public C get() {
        synchronized (STORE) {
            if (STORE.size() < 1) {// 可以做批量操作
                try {
                    STORE.wait();
                } catch (InterruptedException e) {
                    log.warn("[{}] wait get interrupted", Thread.currentThread().getName());
                }
            }

            if (STORE.size() < 1)// 控制结束，如果被外部中断，继续执行任务
                return null;

            C dto = STORE.remove(0);
            STORE.notifyAll();// 唤醒生产者
            return dto;
        }
    }
}
