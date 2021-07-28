package cn.lyjuan.base.test;

import cn.lyjuan.base.util.ByteUtils;
import cn.lyjuan.base.util.HexUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 获取图片二进制信息
 */
public class ImageByteInfo {

    private static final String from = ImageByteInfo.class.getClassLoader().getResource("doc/imageByteInfo.jpg").getFile();

    public static void main(String[] args) throws Exception {
        byte[] bs = readFile(from);

        boolean isJPG = isJPG(bs);
        int width = getInt(bs, 163, 2);
        System.out.println("isJPG ==> " + isJPG + ", w: "+ width);
    }

    /**
     * 获取指定索引位置数据，并解析为int
     *
     * @param bs
     * @param start 开始位置，数组下标从0开始
     * @param len   长度
     * @return
     */
    public static int getInt(byte[] bs, int start, int len) {
        byte[] tmp = new byte[len];
        for (int i = 0; i < len; i++)
            tmp[i] = bs[start + i];
        String hex = HexUtils.toHex(tmp);
        return HexUtils.hex2Ten(hex);
    }

    /**
     * 读取文件二进制数据
     *
     * @param name
     * @return
     * @throws Exception
     */
    public static byte[] readFile(String name) throws Exception {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(name);
            out = new ByteArrayOutputStream();
            int len = -1;
            byte[] bs = new byte[4096];
            while (-1 != (len = in.read(bs)))
                out.write(bs, 0, len);
        } finally {
            if (null != in)
                in.close();
        }

        return out.toByteArray();
    }

    public static boolean isJPG(byte[] bs) {
        int len = bs.length;
        String head = ByteUtils.byteArr2HexStr(new byte[]{bs[0], bs[1]});
        String tail = ByteUtils.byteArr2HexStr(new byte[]{bs[len - 2], bs[len - 1]});

        System.out.println("head: " + head + ", tail: " + tail);

        return "FFD8".equalsIgnoreCase(head) && "FFD9".equalsIgnoreCase(tail);
    }
}
