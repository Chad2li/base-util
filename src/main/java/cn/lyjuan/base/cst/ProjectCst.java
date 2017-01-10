package cn.lyjuan.base.cst;

/**
 * Created by ly on 2015/3/10.
 */
public class ProjectCst
{
    /**
     * 当为 true 时， 开启测试模式
     */
    public static boolean $_ISDEBUG;

    /**
     * 项目名称
     */
    public static String $_PROJECT_NAME;

    /**
     * 项目编码
     */
    public static String $_CHARSET;

    /**
     * 本地项目地址，非WEB项目可设置为项目名
     */
    public static String $_LOCAL_URL;

    /**
     * 接口返回状态码
     */
    public static String $_RESULT_CODE;

    /**
     * 接口返回描述
     */
    public static String $_RESULT_CONTENT;

    /**
     * 接口返回Data内容
     */
    public static String $_RESULT_DATA;

    /**
     * URL中标识后台请求
     */
    public static String $_MANAGE_SUFFIX;

    /**
     * URL中标识接口请求
     */
    public static String $_API_SUFFIX;

    /**
     * URL中标识前台请求
     */
    public static String $_WEB_SUFFIX;
}
