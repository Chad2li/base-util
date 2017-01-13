package cn.lyjuan.base.util;

import java.util.Random;
import java.util.UUID;

/**
 * 线程安全的随机数字和字符串工具
 */
public class RandomUtils
{
    /**
     * 随机工具
     */
    private static final Random RDM = new Random();

    /**
     * 随机工具锁
     */
    private static final String LOCK_RDM = "#L_LOCK_RDM_L#";

    /**
     * <p>使用UUID生成随机字符串，去掉UUID中的“-”</p>
     * <p>长度为32位</p>
     *
     * @return
     */
    public static String uuid()
    {
        String uuid = null;

        synchronized (UUID.class)
        {
            uuid = UUID.randomUUID().toString().replace("-", "");
        }

        return uuid;
    }

    /**
     * 生成随机固定长度的数字字符串
     *
     * @param len
     * @return
     */
    public static String randomIntStr(int len)
    {
        if (len < 0)
            throw new RuntimeException("长度不能小于 1,当前为 " + len);

        if (len <= 8)
        {
            String rdm = String.valueOf(randomInt(len));

            while (rdm.length() < len)// rdm长度在不断增长
                rdm = "0" + rdm;// 这里rdm长度加一了

            return rdm;
        }

        StringBuilder sb = new StringBuilder();

        int count = len / 8;

        for (int i = 0; i < count; i++)// 8 个一次
        {
            sb.append(randomIntStr(8));// 递归8位一次
        }

        if (len % 8 > 0)
            sb.append(randomIntStr(len % 8));

        return sb.toString();
    }

    /**
     * 生成一个固定长度的随机数
     *
     * @param len 随机数的固定长度，出于int长度限制，len小于等于8
     * @return 返回一个固定长度的伪随机数
     */
    public static int randomInt(int len)
    {
        if (1 > len || 8 < len)
            throw new RuntimeException("Method[randomInt] 随机数的固定长度为正整数,且不能大于 8 ,当前为 " + len);

        int sub = (int) Math.pow(10, len - 1);
        synchronized (LOCK_RDM)
        {
            return RDM.nextInt((int) (Math.pow(10, len) - sub)) + sub;
        }
    }

    /**
     * 根据最小值和最大值生成之间的一个随机数,该随机数包括最小值和最大值
     *
     * @param min 随机数的最小值
     * @param max 随机数的最大值
     * @return 返回一个介于最小值与最大值之间的伪随机数
     */
    public static int randomInt(int min, int max)
    {
        if (max >= Integer.MAX_VALUE - 1)
            throw new RuntimeException("随机数返回值为正整数,当前的 max 为 " + max);

        synchronized (LOCK_RDM)
        {
            return RDM.nextInt(max - min + 1) + min;
        }
    }
}
















