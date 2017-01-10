package cn.lyjuan.base.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 利用网络获取解析手机归属地及运营商信息
 * Created by ly on 2015/8/14.
 */
public class MobileUtils
{
    private static Logger log = Logger.getLogger(MobileUtils.class.getName());

    /**
     * 运营商名称
     * 用于统一运营商名称
     */
    public static class MobileOperater
    {
        public static final String CMCC = "移动";
        public static final String CUCC = "联通";
        public static final String CTCC = "电信";
    }

    /**
     * 接口名称
     * 用于选用指定的接口解析手机号信息
     */
    public static class ApiName
    {
        /**
         * 财付通
         */
        public static final String API_TEN_PAY = TenPayMobile.class.getSimpleName();

        /**
         * IP138
         */
        public static final String API_IP_138 = IP138Mobile.class.getSimpleName();

        /**
         * 手机在线
         */
        public static final String API_SHOW_JI = ShowJiMobile.class.getSimpleName();

        /**
         * XP查
         */
        public static final String API_XP_CHA = XPChaMobile.class.getSimpleName();
    }

    private static final List<String> list;

    static
    {
        // 按优先级顺序添加解析工具
        list = new ArrayList<>(4);
        list.add(ApiName.API_IP_138);
        list.add(ApiName.API_TEN_PAY);
        list.add(ApiName.API_SHOW_JI);
        list.add(ApiName.API_XP_CHA);
    }

    /**
     * 遍历所有接口，直到成功获取手机信息
     * @param mobile
     * @return              未解析成功返回 null
     */
    public static MobileInfo sendGetMobileInfo(String mobile)
    {
        String name = null;

        // 遍历接口
        int i = 0;
        MobileInfo info = null;
        while (i < list.size() && null == info)
            info = sendGetMobileInfo(list.get(i++), mobile);

        return info;
    }

    /**
     * 使用指定API获取并解析手机号信息
     * @param apiName       指定要使用的API名称，详情见{@link ApiName}
     * @param mobile        手机号
     * @return              未解析成功返回 null
     */
    public static MobileInfo sendGetMobileInfo(String apiName, String mobile)
    {
        IMobile mobileUtils = null;

        if (ApiName.API_TEN_PAY.equalsIgnoreCase(apiName))
            mobileUtils = TenPayMobile.newInstance();
        else if (ApiName.API_IP_138.equalsIgnoreCase(apiName))
            mobileUtils = IP138Mobile.newInstance();
        else if (ApiName.API_XP_CHA.equalsIgnoreCase(apiName))
            mobileUtils = XPChaMobile.newInstance();
        else if (ApiName.API_SHOW_JI.equalsIgnoreCase(apiName))
            mobileUtils = ShowJiMobile.newInstance();
        else
            throw new RuntimeException("api name not found, to see " + ApiName.class.getName());

        // 一个不可用的或新的号码会导致整个工具不可用，所以取消该功能
//        if (!mobileUtils.isAvailable()) return null;

        MobileInfo info = mobileUtils.req(mobile);

        // 统一处理省，市，运营商
        mobileUtils.tidy(info);

        return info;
    }

    static class ShowJiMobile extends AMobile
    {
        /**
         * 单例
         */
        private static IMobile obj;
        private ShowJiMobile()
        {
            name = "手机在线";
            url = "http://v.showji.com/Locating/showji.com2016234999234.aspx?m=#MOBILE#&output=json&callback=querycallback";
            available = true;
            charset = "UTF-8";

            provinceName = "Province";
            cityName = "City";
            operaterName = "TO";
        }
        public static IMobile newInstance()
        {
            if (null == obj)// 仅在第一次初始化时，需要加锁；生成后不需要走这步，所以提高了性能
            {
                synchronized (ShowJiMobile.class)
                {
                    if (null == obj) obj = new ShowJiMobile();
                }
            }
            return obj;
        }

        @Override
        protected String parseByName(String result, String name)
        {
            int index = result.indexOf(name + "\":\"");

            if (index > -1)
            {
                result = result.substring(index + (name + "\":\"").length());

                result = result.substring(0, result.indexOf("\""));
            } else
                result = null;

            return result;
        }

        @Override
        protected boolean isRespSucc(String result)
        {
            return !StringUtils.isNull(result) && result.indexOf("\"QueryResult\":\"True\"") > -1;
        }
    }

    static class XPChaMobile extends AMobile
    {
        /**
         * 单例
         */
        private static IMobile obj;
        private XPChaMobile()
        {
            name = "XP查";
            url = "http://shouji.xpcha.com/#MOBILE#.html";
            available = true;
            charset = "UTF-8";
        }

        public static IMobile newInstance()
        {
            if (null == obj)// 仅在第一次初始化时，需要加锁；生成后不需要走这步，所以提高了性能
            {
                synchronized (XPChaMobile.class)
                {
                    if (null == obj) obj = new XPChaMobile();
                }
            }
            return obj;
        }

        /**
         * @see AMobile
         * @param mobile
         * @return
         */
        @Override
        public MobileInfo req(String mobile)
        {
            // 暂停
            if (true) return null;

            // 全号码会返回吉凶，降低流量，只需前七位
            String mUlr = urlWithMobile(mobile.substring(0, 7));

            String result = httpReq(mUlr);

            return parse(result, mobile);
        }

        private MobileInfo parse(String result, String mobile)
        {
            try
            {
                Document doc = Jsoup.parse(result);

                Elements elts = doc.getElementById("result").getElementsByTag("span");

                String[] citys = elts.get(0).html().trim().split(" ");
                String oper = elts.get(1).html().trim();
                oper = matchOperater(oper);

                MobileInfo info = new MobileInfo();

                info.mobile = mobile;
                info.province = citys[0];
                info.city = citys.length > 1 ? citys[1] : citys[0];
                info.operater = oper;

                return info;
            } catch (Exception e)
            {
                log.warning("[" + name + "] 手机号归属地结果解析失败 err >> " + e.getMessage());
                available = false;
            }

            return null;
        }
    }

    static class IP138Mobile extends AMobile
    {
        /**
         * 单例
         */
        private static IMobile obj;
        private IP138Mobile()
        {
            name = "IP138";
            url = "http://wap.ip138.com/sim_search138.asp?mobile=#MOBILE#";
            available = true;
            charset = "UTF-8";
        }
        public static IMobile newInstance()
        {
            if (null == obj)// 仅在第一次初始化时，需要加锁；生成后不需要走这步，所以提高了性能
            {
                synchronized (IP138Mobile.class)
                {
                    if (null == obj) obj = new IP138Mobile();
                }
            }
            return obj;
        }

        /**
         * @see AMobile
         * @param mobile
         * @return
         */
        @Override
        public MobileInfo req(String mobile)
        {
            String mUlr = urlWithMobile(mobile.substring(0, 7));

            String result = httpReq(mUlr);

            return parse(result, mobile);
        }

        private MobileInfo parse(String result, String mobile)
        {
            try
            {
                Document doc = Jsoup.parse(result);

                Element elt = doc.getElementsByTag("div").get(0).getElementsByTag("div").get(2);

                String[] infos = elt.html().split("<br>");
                if (null == infos || infos.length < 2)
                    infos = elt.html().split("<br />");

                String[] citys = infos[1].replace("归属地：", "").trim().split(" ");
                String oper = infos[2].replace("卡类型：", "").trim();

                MobileInfo info = new MobileInfo();
                info.mobile = mobile;
                info.province = citys[0];
                info.city = citys.length > 1 ? citys[1] : citys[0];// 北京，上海等地
                info.operater = oper;

                if ("未知".equals(info.mobile)
                        || "未知".equals(info.province)
                        || "未知".equals(info.city)
                        || "未知".equals(info.operater)
                        || StringUtils.isNull(info.mobile)
                        || StringUtils.isNull(info.province)
                        || StringUtils.isNull(info.city)
                        || StringUtils.isNull(info.operater))
                    return null;

                return info;
            } catch (Exception e)
            {
                log.warning("[" + name + "] 手机号归属地结果解析失败 err >> " + e.getMessage());
                available = false;
            }

            return null;
        }
    }

    static class TenPayMobile extends AMobile
    {
        /**
         * 单例
         */
        private static IMobile obj;
        private TenPayMobile()
        {
            name = "财付通";
            url = "https://chong.qq.com/tws/extinfo/GetMobileProductInfo?mobile=#MOBILE#&amount=5000&group=12&type=1&callname=&dtag=1476782038358&g_ty=ls";
            available = true;
            charset = "GBK";
            provinceName = "province";
            cityName = "cityname";
            operaterName = "isp";
        }
        public static IMobile newInstance()
        {
            if (null == obj)// 仅在第一次初始化时，需要加锁；生成后不需要走这步，所以提高了性能
            {
                synchronized (TenPayMobile.class)
                {
                    if (null == obj) obj = new TenPayMobile();
                }
            }
            return obj;
        }

        @Override
        protected boolean isRespSucc(String result)
        {
            if (StringUtils.isNull(result)) return false;
            result = result.replace("(", "").replaceAll("\\);.*", "");
            Map map = JsonUtils.from(Map.class, result);
            String province = StringUtils.toStr(map.get("province"));

            return !StringUtils.isNull(province) && !"未知".equalsIgnoreCase(province);
        }

        @Override
        protected String parseByName(String result, String name)
        {
            result = result.replace("(", "").replaceAll("\\);.*", "");
            Map map = JsonUtils.from(Map.class, result);

            return StringUtils.toStr(map.get(name));
        }
    }

    public static class MobileInfo
    {
        public String mobile;
        public String province;
        public String city;
        public String operater;

        @Override
        public String toString()
        {
            return "MobileInfo{" +
                    "mobile='" + mobile + '\'' +
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", operater='" + operater + '\'' +
                    '}';
        }
    }

    interface IMobile
    {
        /**
         * 请求解析手机号信息
         * @param mobile
         * @return
         */
        MobileInfo req(String mobile);

        /**
         * 统一省，市，运营商信息
         */
        void tidy(MobileInfo info);

        /**
         * 该接口是否可用
         * @return
         */
        boolean isAvailable();
    }

    static abstract class AMobile implements IMobile
    {
        public String name;
        public String url;
        public boolean available = true;
        public String charset;
        public String provinceName;
        public String cityName;
        public String operaterName;

        /**
         * @see IMobile
         * @param mobile
         * @return
         */
        @Override
        public MobileInfo req(String mobile)
        {
            String mUrl = urlWithMobile(mobile);

            String result = httpReq(mUrl);

            boolean isSucc = isRespSucc(result);

            // 响应不成功时，该接口不再使用
            available = isSucc;
            if (!isSucc) return null;

            MobileInfo m = new MobileInfo();

            m.mobile = mobile;
            m.province = parseProvince(result);
            m.city = parseCity(result);
            m.operater = parseOperater(result);

            if (StringUtils.isNull(m.mobile)
                    || StringUtils.isNull(m.province)
                    || StringUtils.isNull(m.city)
                    || StringUtils.isNull(m.operater))
                return null;

            return m;
        }

        /**
         * 将URL地址绑定手机信息
         * @param mobile
         * @return
         */
        protected String urlWithMobile(String mobile)
        {
            return url.replace("#MOBILE#", mobile);
        }

        /**
         * 发起 HTTP 请求手机号信息
         * @param mUrl
         * @return
         */
        protected String httpReq(String mUrl)
        {
            String result = null;
            try
            {
                result = HttpUtils.sendGet(mUrl, charset);
            } catch (Exception e)
            {
                log.warning("[" + name + "] 手机号归属地结果解析失败 err >> " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }

            if (null == result || result.trim().length() < 1)
                return null;

            return result;
        }

        /**
         * 判断请求响应的结果是否成功
         * @param result
         * @return
         */
        protected boolean isRespSucc(String result)
        {
            throw new RuntimeException("this api do not implements isRespSucc method");
        }

        /**
         * 解析请求响应结果中的 省份信息
         * @param result        请求响应成功时的结果
         * @return
         */
        protected String parseProvince(String result)
        {
            return parseByName(result, provinceName);
        }

        /**
         * 解析请求响应结果中的 城市信息
         * @param result        请求响应成功时的结果
         * @return
         */
        protected String parseCity(String result)
        {
            return parseByName(result, cityName);
        }

        /**
         * 解析请求响应结果中的 运营商信息
         * @param result
         * @return
         */
        protected String parseOperater(String result)
        {
            String operater = parseByName(result, operaterName);

            return matchOperater(operater);
        }

        /**
         * 根据名称获取手机信息中的值
         * @param result        请求获取的手机信息
         * @param name              解析名称
         * @return
         */
        protected String parseByName(String result, String name)
        {
            throw new RuntimeException("this api do not implements parseByName method");
        }

        /**
         * 匹配运营商，将各网站上不同的字符转化为统一格式
         * 使用 indexOf 来匹配运营商字符串
         * @param operater      接口解析出的运营商字符
         * @return
         */
        protected String matchOperater(String operater)
        {
            if (StringUtils.isNull(operater)) return "";

            if (operater.indexOf(MobileOperater.CMCC) > -1) return MobileOperater.CMCC;
            else if (operater.indexOf(MobileOperater.CUCC) > -1) return MobileOperater.CUCC;
            else if (operater.indexOf(MobileOperater.CTCC) > -1) return MobileOperater.CTCC;

            return operater;
        }

        @Override
        public void tidy(MobileInfo info)
        {
            if (null == info) return;

            if (!StringUtils.isNull(info.province))
                info.province = info.province.replaceAll("省$", "");
            if (!StringUtils.isNull(info.city))
                info.city = info.city.replaceAll("市$", "");
            if (!StringUtils.isNull(info.operater))
                info.operater = matchOperater(info.operater);
        }

        @Override
        public boolean isAvailable()
        {
            return available;
        }
    }
}
