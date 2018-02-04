package cn.lyjuan.base.util;

/**
 * 16进制工具
 * <p>
 * 来源：https://www.cnblogs.com/freeliver54/archive/2012/07/30/2615149.html
 * </p>
 */
public class HexUtils
{
    /**
     * 字节转16进制字符串
     *
     * @param src
     * @return
     */
    public static String toHex(byte... src)
    {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0)
            return null;

        for (int i = 0; i < src.length; i++)
        {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
            {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 16进制字符串转字节
     *
     * @param hex
     * @return
     */
    public static byte[] fromHex(String hex)
    {
        if (hex == null || hex.equals(""))
            return null;

        hex = hex.toUpperCase();
        int length = hex.length() / 2;
        char[] hexChars = hex.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 十六进制转二进制
     *
     * @param hex
     * @return
     */
    public static String toBinary(String hex)
    {
        byte[] bytes = HexUtils.fromHex(hex);

        if (null == bytes || bytes.length < 1)
            return null;

        String str = null;
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            str = toBinary(b);
            sb.append(str);
        }

        // 补齐 8 的倍数
        str = sb.toString();
        int len = hex.length() * 4;
        while (len > str.length())
            str = "0" + str;

        return str;
    }

    /**
     * byte转二进制字符串
     *
     * @param b
     * @return
     */
    public static String toBinary(Byte b)
    {
        if (null == b) return null;

        String binary = Integer.toBinaryString(Byte.toUnsignedInt(b));
        return binary;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c)
    {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
