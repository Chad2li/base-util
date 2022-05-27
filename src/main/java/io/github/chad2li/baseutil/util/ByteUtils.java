package io.github.chad2li.baseutil.util;

/**
 * Created by ly on 2015/4/18.
 */
public class ByteUtils
{
    /**
     * 将二进制字节数组转换为十六进制字符串
     * @param arrB
     * @return
     */
    public static String byteArr2HexStr(byte[] arrB)
    {
        if (arrB == null) { return ""; }
        int iLen = arrB.length;

        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++)
        {
            int intTmp = arrB[i];

            while (intTmp < 0)
            {
                intTmp += 256;
            }

            if (intTmp < 16)
            {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为二进制字节数组
     * @param strIn
     * @return
     */
    public static byte[] hexStr2ByteArr(String strIn)
    {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;

        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i += 2)
        {
            String strTmp = new String(arrB, i, 2);
            arrOut[(i / 2)] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }
}
