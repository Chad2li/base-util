package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author chad
 * @date 2022/1/21 11:01
 * @since
 */
public class StringUtilsTest {

    @Test
    public void isEmail() {
        String email = null;
        boolean isEmail = false;

        // ok
        email = "1@a.b";
        isEmail = StringUtils.isEmail(email);
        Assert.assertTrue(isEmail);

        // false - not @
        email = "1";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - has @, but no domain
        email = "1@";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - has domain, but domain not 2 level
        email = "1@a";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - no account
        email = "@a.b";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - illegal char
        email = "1*@a.b";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - illegal char
        email = "1@a.*b";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // false - illegal domain
        email = "1@a.b.";
        isEmail = StringUtils.isEmail(email);
        Assert.assertFalse(isEmail);

        // ok - domain has 3 leve
        email = "1@a.b.c";
        isEmail = StringUtils.isEmail(email);
        Assert.assertTrue(isEmail);

        // ok - complex account
        email = "12FWfR2jf9zFef@a.b.c";
        isEmail = StringUtils.isEmail(email);
        Assert.assertTrue(isEmail);
    }

    @Test
    public void hiddenEmail() {
        String email = null;
        String result = null;

        // ok - account = 1
        email = "1@a.b";
        result = StringUtils.hiddenEmail(email);
        Assert.assertEquals("*@a.b", result);
        // ok - account = 2
        email = "12@a.b";
        result = StringUtils.hiddenEmail(email);
        Assert.assertEquals("**@a.b", result);

        email = "123@a.b";
        result = StringUtils.hiddenEmail(email);
        Assert.assertEquals("1*3@a.b", result);
    }

    @Test
    public void hide() {
        String str = null;
        String result = null;

        // ok - null;
        str = null;
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("", result);

        // ok - null
        str = "";
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("", result);

        // ok - 1
        str = "1";
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("*", result);

        // ok - 2
        str = "12";
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("*2", result);

        // ok - 3,0,1
        str = "123";
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("**3", result);

        // ok - 3,1,1
        str = "123";
        result = StringUtils.hide(str, '*', 1, 1);
        Assert.assertEquals("1*3", result);

        // ok - 4,1,1
        str = "1234";
        result = StringUtils.hide(str, '*', 1, 1);
        Assert.assertEquals("1**4", result);

        // ok - 4,1,2
        str = "1234";
        result = StringUtils.hide(str, '*', 1, 2);
        Assert.assertEquals("1*34", result);

        // ok - 3,1,0
        str = "123";
        result = StringUtils.hide(str, '*', 1, 0);
        Assert.assertEquals("1**", result);

        // ok - 3,0,1
        str = "123";
        result = StringUtils.hide(str, '*', 0, 1);
        Assert.assertEquals("**3", result);

        // ok - > len
        str = "123";
        result = StringUtils.hide(str, '*', 1, 3);
        Assert.assertEquals("***", result);

        // ok - = len
        str = "123";
        result = StringUtils.hide(str, '*', 1, 2);
        Assert.assertEquals("***", result);

        // ok - start < 0
        str = "123";
        result = StringUtils.hide(str, '*', -1, 1);
        Assert.assertEquals("**3", result);

        // ok - end < 0
        str = "123";
        result = StringUtils.hide(str, '*', 1, -1);
        Assert.assertEquals("1**", result);
    }

    @Test
    public void hideMobile() {
        String mobile = null;
        String result = null;

        mobile = "13212345678";
        result = StringUtils.hideMobile(mobile);
        Assert.assertEquals("132****5678", result);
    }

    @Test
    public void hideBankCard() {
        String bankCard = null;
        String result = null;

        bankCard = "6222020012345678887";
        result = StringUtils.hideBankCard(bankCard);
        Assert.assertEquals("622202*********8887", result);
    }

    @Test
    public void hideName() {
        String name = null;
        String result = null;

        name = "张三";
        result = StringUtils.hideName(name);
        Assert.assertEquals("*三", result);
    }
}