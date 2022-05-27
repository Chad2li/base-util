package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.util.DateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Redis分布式操作
 */
@Slf4j
@Data
public class RedisDistOps {
    public static final String BEAN_NAME = "baseRedisDistOpsName";
    /**
     * 加锁名称，标识加锁者身份
     */
    private String name;

    private RedisOps redisOps;
    /**
     * 加锁失败重试次数
     */
    private int lockRetry = 3;
    /**
     * 锁释放秒
     */
    private long lockLeastSeconds = 30;

    @Autowired
    public RedisDistOps(final String appName, RedisOps redisOps) {
        this.redisOps = redisOps;

        // 生成 redis 分布式锁 源目标的名称，防止重复及日志查错
        // 组成： 应用名:ipv4(多个):时间
        List<String> ips = getLocalIPList();
        StringBuilder sb = new StringBuilder();
        sb.append(appName).append(":");
        if (null != ips && !ips.isEmpty()) {
            for (String str : ips) {
                if ("127.0.0.1".equalsIgnoreCase(str)
                        || "localhost".equalsIgnoreCase(str))
                    continue;
                sb.append(str).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(":").append(DateUtils.format(LocalDateTime.now(), "yyyyMMddHHmmss"));

        this.name = sb.toString();
        log.info("Redis Distribute name: {}", this.name);
    }

    /**
     * 获取所有本机IPv4地址
     *
     * @return
     */
    private static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("redis get local ipv4 error", e);
        }
        return ipList;
    }

    /**
     * redis分布式锁
     *
     * @param lockName
     * @return
     */
    public boolean lock(String lockName) {
        synchronized (lockName.intern()) {
            return lock(lockName, lockRetry);
        }
    }

    /**
     * 分布式解锁
     *
     * @param lockName
     * @return
     */
    public boolean unlock(String lockName) {
        byte[] nameBytes = lockName.getBytes();
        // 只能解锁自己加的锁
        List<Object> list = redisOps.executePipelined(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] valBytes = redisConnection.get(nameBytes);
                String val = String.valueOf(valBytes);
                if (name.equalsIgnoreCase(val))// 是自己的，删除
                    redisConnection.del(nameBytes);// =0是可能redis执行过期删除
                // todo redis配置在访问时删除过期的key
                return null;
            }
        });
        if (log.isDebugEnabled())
            log.debug("Redis unlock: {}", lockName);
        return null != list && !list.isEmpty();
    }

    /**
     * 重试加锁，如果加锁失败，最多重试 retry 次，每次重试前休眠 500 毫秒
     *
     * @param lockName 锁全称
     * @param retry    重试次数
     * @return true最终加锁成功
     */
    private boolean lock(String lockName, int retry) {
        boolean ok = redisOps.set(lockName, name, false, lockLeastSeconds);
        if (log.isDebugEnabled())
            log.debug("Redis set lock: {} {} ==> {}", lockName, retry, ok);
        if (ok) return true;

        if (--retry <= 0) return false;

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        return lock(lockName, retry);
    }
}


















