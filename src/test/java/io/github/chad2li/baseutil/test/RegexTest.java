package io.github.chad2li.baseutil.test;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author chad
 * @date 2021/11/25 20:35
 * @since
 */
public class RegexTest {
    @Test
    public void w() {
        String str = null;
        String regex = "^[-\\w]+$";

        // 数字
        Assert.assertTrue("123123123".matches(regex));
        // 字母
        Assert.assertTrue("fewofjwefFWEFW".matches(regex));
        // -
        Assert.assertTrue("-".matches(regex));
        // 数字+字母
        Assert.assertTrue("123jofwe231FEW1".matches(regex));
        // 前空格
        Assert.assertFalse(" fwejofiw12123F".matches(regex));
        // 后空格
        Assert.assertFalse("fwejo123F ".matches(regex));
        // 中间空格
        Assert.assertFalse("fjow fj123".matches(regex));
        // 其他字符
        Assert.assertFalse(".fe312".matches(regex));
        // 中文
        Assert.assertFalse("中文few12".matches(regex));
    }
}
