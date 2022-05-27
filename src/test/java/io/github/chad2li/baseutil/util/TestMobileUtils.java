package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by chad on 2016/5/10.
 */
public class TestMobileUtils
{
    private String mobile = "13221062344";
    private MobileUtils.MobileInfo expect;

    @Before
    public void init()
    {
        expect = new MobileUtils.MobileInfo();
        expect.province = "浙江";
        expect.city = "杭州";
        expect.mobile = mobile;
        expect.operater = "联通";
    }

    @Test
    public void testSendGetMobileInfo()
    {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(mobile);

        Assert.assertEquals(StringUtils.toStr(expect), StringUtils.toStr(info));
    }

    @Test
    public void testSendGetMobileInfoForAPI_TEN_PAY()
    {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_TEN_PAY, mobile);

        Assert.assertEquals(StringUtils.toStr(expect), StringUtils.toStr(info));
    }

    @Test
    public void testSendGetMobileInfoForAPI_TEN_PAY_Null()
    {
        String mobile = "1300003";
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_TEN_PAY, mobile);

        Assert.assertNull(info);
    }

    @Test
    public void testSendGetMobileInfoForAPI_IP_138()
    {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_IP_138, mobile);

        Assert.assertEquals(StringUtils.toStr(expect), StringUtils.toStr(info));
    }

    @Test
    public void testSendGetMobileInfoForAPI_IP_138_Null()
    {
        String mobile = "1300003";
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_IP_138, mobile);

        Assert.assertNull(info);
    }

    @Test
    @Ignore
    public void testSendGetMobileInfoForAPI_XP_CHA()
    {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_XP_CHA, mobile);

        Assert.assertEquals(StringUtils.toStr(expect), StringUtils.toStr(info));
    }

    @Test
    public void testSendGetMobileInfoForAPI_XP_CHA_Null()
    {
        String mobile = "1231212";
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_XP_CHA, mobile);

        Assert.assertNull(info);
    }

    @Test
    @Ignore
    public void testSendGetMobileInfoForAPI_SHOW_JI()
    {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_SHOW_JI, mobile);

        Assert.assertEquals(StringUtils.toStr(expect), StringUtils.toStr(info));
    }

    @Test
    @Ignore
    public void testSendGetMobileInfoForAPI_SHOW_JI_Null()
    {
        String mobile = "1300003";
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_SHOW_JI, mobile);

        Assert.assertNull(info);
    }
}
