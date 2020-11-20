package cn.lyjuan.base.util.test;

import cn.lyjuan.base.util.MobileUtils;

public class MobileTest {
    public static void main(String[] args) {
        MobileUtils.MobileInfo info = MobileUtils.sendGetMobileInfo(MobileUtils.ApiName.API_IP_138, "13221062341");

        System.out.println(info);
    }
}
