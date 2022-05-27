package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class HexUtilsTest
{
    @Test
    public void toHex()
    {
        byte[] b = {0xc, 0x8, 0xa};

        String hex = HexUtils.toHex(b);

        Assert.assertEquals("0C080A", hex);
    }

    @Test
    public void fromHex()
    {
        String hex = "0C08C0";
        byte[] b = HexUtils.fromHex(hex);

        System.out.println(Arrays.toString(b));
        for (byte s : b)
        {
            System.out.println(Integer.toHexString(s));
        }
//        Assert.assertArrayEquals(new byte[]{0xc, 0x8, 0xc0}, b);
    }

    @Test
    public void str2Binary()
    {
        String str = "12ab";
        String expect = "0001001010101011";

        str = HexUtils.toBinary(str);
        Assert.assertEquals(expect, str);

        str = "0001";
        expect = "0000000000000001";
        str = HexUtils.toBinary(str);
        Assert.assertEquals(expect, str);
    }
}