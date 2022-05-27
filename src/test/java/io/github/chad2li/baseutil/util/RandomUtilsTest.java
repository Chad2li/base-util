package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chad on 2016/8/2.
 */
public class RandomUtilsTest
{
    @Test
    public void randomIntStr() throws Exception
    {
        String rdm = null;
        for (int i = 0; i < 100000; i++)
        {
            int len = RandomUtils.randomInt(1);
            rdm = RandomUtils.randomIntStr(len);
            Assert.assertEquals(len, rdm.length());
        }
    }

    @Test
    public void randomInt_len() throws Exception
    {
        int rdm = -1;

        for (int len = 1; len < 9; len++)
        {
            int max = (int) Math.pow(10, len);
            for (int j = 0; j < 100000; j++)
            {
//                System.out.println("len >> " + len + " rdm >> " + rdm);
                rdm = RandomUtils.randomInt(len);
                Assert.assertTrue(rdm < max && rdm >= 0);
                Assert.assertEquals(len, String.valueOf(rdm).length());
            }
        }
    }

    @Test
    public void randomInt_min_max() throws Exception
    {
        int rdm = -1;

        for (int i = 0; i < 100000; i++)
        {
            int min = RandomUtils.randomInt(2);
            int max = RandomUtils.randomInt(2);
            max += min;
//            System.out.println("min >> " + min + " max >> " + max + " rdm >> " + rdm);
            rdm = RandomUtils.randomInt(min, max);

            Assert.assertTrue(rdm >= min && rdm <= max);
        }
    }
}