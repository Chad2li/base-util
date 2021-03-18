package cn.lyjuan.base.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP 工具类
 *
 * @author chad
 * @deprecated 请使用HttpClient或RestTemplate
 */
@Slf4j
public class HttpUtils {
    /**
     * Form 表单参数分隔符
     */
    public static final String FORM_SEPARATE = "&";

    /**
     * Form 表单中的赋值标识
     */
    public static final String FORM_EP = "=";

    /**
     * PUT 请求方式
     **/
    public static final String HTTP_METHOD_PUT = "PUT";

    public static final String HTTP_METHOD_DELETE = "DELETE";

    /**
     * POST 请求方式
     **/
    public static final String HTTP_METHOD_POST = "POST";

    /**
     * GET 请求方式
     **/
    public static final String HTTP_METHOD_GET = "GET";

    private static final int CONN_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 20000;

    /**
     * 发送 PUT 请求
     *
     * @param url
     * @param params
     * @param charset
     * @param headers
     * @return
     */
    public static String sendPut(String url, String params, String charset, Map<String, String> headers) {
        return send(HTTP_METHOD_PUT, url, params, false, charset, headers);
    }

    /**
     * 发送 DELETE 请求
     *
     * @param url
     * @param params
     * @param charset
     * @param headers
     * @return
     */
    public static String sendDelete(String url, String params, String charset, Map<String, String> headers) {
        return send(HTTP_METHOD_DELETE, url, params, false, charset, headers);
    }

    /**
     * 发送请求，将map参数以HTML表单形式拼接成字符串提交
     *
     * @param url     请求的 URL 地址
     * @param params  请求参数
     * @param charset 编码方式
     * @return
     */
    public static String sendPost(String url, Map<String, String> params, String charset) {
        return sendPost(url, paraToForm(params), true, charset, null);
    }

    /**
     * 发送POST请求
     *
     * @param url     链接
     * @param params  请求内容
     * @param charset 字符编码
     * @return
     */
    public static String sendPost(String url, String params, String charset) {
        return sendPost(url, params, false, charset, null);
    }

    /**
     * 发送POST请求
     *
     * @param url     链接
     * @param params  请求内容
     * @param charset 字符编码
     * @param headers 请求头
     * @return
     */
    public static String sendPost(String url, String params, String charset, Map<String, String> headers) {
        return send(HTTP_METHOD_POST, url, params, false, charset, headers);
    }

    /**
     * 发送请求，将map参数以HTML表单形式拼接成字符串提交
     *
     * @param url     请求的 URL 地址
     * @param params  请求参数
     * @param charset 编码方式
     * @param headers 请求头属性
     * @return
     */
    public static String sendPost(String url, Map<String, String> params, String charset, Map<String, String> headers) {
        return sendPost(url, paraToForm(params), true, charset, headers);
    }

    /**
     * 发送 HTTP POST 请求
     *
     * @param url     请求的 URL 地址
     * @param params  请求的参数内容
     * @param isForm  form请求标识，为true时会在请求头属性中添加：application/x-www-form-urlencoded;charset={charset}
     * @param charset 编码方式
     * @param headers 请求头属性
     * @return 返回请求响应内容
     */
    public static String sendPost(String url, String params, boolean isForm, String charset, Map<String, String> headers) {
        return send(HTTP_METHOD_POST, url, params, isForm, charset, headers);
    }

    /**
     * 发送请求
     *
     * @param method  方法
     * @param url     链接
     * @param params  参数
     * @param isForm  是否Form表单格式提交
     * @param charset 字符编码
     * @param headers 请求头
     * @return
     */
    public static String send(String method, String url, String params, boolean isForm, String charset, Map<String, String> headers) {
        if (isForm)// 增加FORM表单提交属性
        {
            if (null == headers)
                headers = new HashMap<>(2);

            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        }

        return send(method, url, params, charset, headers);
    }

    /**
     * 发送 POST/PUT 等内容请求
     *
     * @param method  请求方法名
     * @param url     请求地址
     * @param params  请求POST内容
     * @param charset 编码
     * @param headers 请求头信息
     * @return
     */
    public static String send(String method, String url, String params, String charset, Map<String, String> headers) {
        if (log.isDebugEnabled()) {
            if (null == headers)
                log.debug(method + "请求 URL >> " + url + " params >> " + params);
            else
                log.debug(method + "请求 URL >> " + url + " params >> " + params + " headers >> " + headers);
        }

        params = null == params ? "" : params;
        StringBuilder result = new StringBuilder();
        try {
            // 打开连接，并设置参数
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl
                    .openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if (!HTTP_METHOD_DELETE.equalsIgnoreCase(method)) {
                conn.setDoInput(true); // 设置可输入
                if (!HTTP_METHOD_GET.equalsIgnoreCase(method))
                    conn.setDoOutput(true);// 设置可输出
            }
            if (!CollectionUtils.isEmpty(headers)) {
                for (Map.Entry<String, String> h : headers.entrySet()) {
                    if (StringUtils.isNull(h.getKey())
                            || StringUtils.isNull(h.getValue()))
                        continue;
                    conn.setRequestProperty(h.getKey(), h.getValue());
                }
            }

            conn.connect();

            // 输出
            if (!HTTP_METHOD_DELETE.equalsIgnoreCase(method) && !HTTP_METHOD_GET.equalsIgnoreCase(method)) {
                conn.getOutputStream().write(params.getBytes(charset));
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
            }

            // 输入
            BufferedReader in = new BufferedReader(new InputStreamReader(conn
                    .getInputStream(), charset));
            String line = null;
            while (null != (line = in.readLine()))
                result.append(line);

            in.close();

            // 关闭连接
            conn.disconnect();
        } catch (IOException e) {
            log.warn("网络异常 url >> " + url + " params >> " + params
                    + " charset >> " + charset + " headers >> " + headers
                    + " error >> " + e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("result >> " + result.toString());

        return result.toString();
    }

    /**
     * 发送 GET 请求
     *
     * @param url    请求的 URL 地址
     * @param encode 请求的编码方式，null为默认编码
     * @return
     * @throws java.io.IOException
     * @throws java.io.IOException
     */
    public static String sendGet(String url, String encode) {
//        log.severe(HTTP_METHOD_GET + "请求 URL >> " + url);
        StringBuilder result = new StringBuilder();
        try {
            // 打开连接，设置参数
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl
                    .openConnection();
            conn.setDoInput(true); // 有返回参数
            conn.setRequestMethod(HTTP_METHOD_GET); // 请求 GET 方式
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setUseCaches(false); // 不使用缓存
            conn.setRequestProperty("Content-type", "text/html;charset=" + encode); // 设置请求头属性
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Safari/605.1.15");
            conn.connect();

            String line = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(conn
                    .getInputStream(), encode));
            while (null != (line = in.readLine()))
                result.append(line);

            // 关闭连接
            conn.getInputStream().close();
            conn.disconnect();
        } catch (IOException e) {
            log.warn("网络异常 url >> " + url + " error >> " + e.getMessage());
            throw new RuntimeException(e);
        }
//        log.severe("result >> " + result.toString());
        return result.toString();
    }

    /**
     * 将 map 转化为 Form 形式的参数字符串，如：name=123&pass=123
     *
     * @param params
     * @return
     */
    public static String paraToForm(Map<String, String> params) {
        if (null == params)
            return "";

        StringBuilder result = new StringBuilder();

        for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it
                .hasNext(); ) {
            Entry<String, String> e = it.next();
            result.append(e.getKey()).append("=").append(e.getValue()).append(
                    FORM_SEPARATE);
        }

        if (result.length() > 0)
            result.delete(result.length() - 1, result.length());

        return result.toString();
    }

    /**
     * 获取 POST 参数
     * <p/>
     * 由于使用 MAP 的原因，没有值的参数不获取
     *
     * @param in
     * @return
     * @throws java.io.IOException
     */
    public static Map<String, String> postParam(InputStream in)
            throws IOException {
        // 读取POST请求
        String post = postStr(in);

        if (StringUtils.isNull(post.toString()))
            return new HashMap<String, String>();

        // 解析字符串为请求参数
        return parseURLParam(post);
    }

    /**
     * 获取POST提交的数据
     *
     * @param in
     * @return
     * @throws java.io.IOException
     */
    public static String postStr(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (null != (line = reader.readLine()))
            sb.append(line);

        return sb.toString();
    }

    /**
     * 解析 URL 中的参数
     *
     * @return
     */
    public static Map<String, String> parseURLParam(String strs) {
        Map<String, String> map = new HashMap<String, String>();

        if (StringUtils.isNull(strs))
            return map;

        String str[] = strs.split(FORM_SEPARATE);

        for (String s : str) {
            String[] str2 = s.split(FORM_EP);
            if (str2 == null || str2.length == 0)
                continue;

            if (str2.length == 1)
                map.put(str2[0], "");
            else
                map.put(str2[0], s.substring(str2[0].length() + 1));
        }

        return map;
    }
}
