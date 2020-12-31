package cn.lyjuan.base.util;

import cn.lyjuan.base.exception.util.ErrUtils;
import cn.lyjuan.base.http.aop.service.IHeaderService;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具
 */
public class HttpSignUtil {

    public static final String CHARSET = "UTF-8";

    /**
     * 拼接签名字符串
     *
     * @param path
     * @param method
     * @param header
     * @param getParams
     * @param body
     * @param md5Key
     * @return
     */
    public static String appendSign(String path, String method, IHeaderService.AHeaderParam header
            , Map<String, String> getParams, String body, String md5Key) {
        StringBuilder sb = new StringBuilder();
        sb.append(path).append("&").append(method).append("&");
        // header
        appendHeader(header, sb);
        // get
        appendGet(getParams, sb);
        // body
        if (!StringUtils.isNull(body)) {
            sb.append(body).append("&");
        }
        // key
        sb.append(md5Key);

        return sb.toString();
    }

    private static void appendHeader(IHeaderService.AHeaderParam header, StringBuilder sb) {
        Map<String, Object> param = ReflectUtils.membersToMap(header);

        TreeMap<String, String> map = new TreeMap<>();

        for (Map.Entry<String, Object> f : param.entrySet()) {
            Object value = f.getValue();
            if (StringUtils.isNull(f.getValue()))// 跳过空值
                continue;
            String name = f.getKey();
            if ("sign".equalsIgnoreCase(name)
                    || "md5".equalsIgnoreCase(name)
                    || "md5Sign".equalsIgnoreCase(name))// 跳过sign签名字段
                continue;
            map.put(name, value.toString());
        }

        appendTreeMap(map, sb);

        sb.append("&");
    }

    /**
     * 拼接GET参数
     *
     * @param map
     * @param sb
     */
    private static void appendGet(Map<String, String> map, StringBuilder sb) {
        if (null == map || map.isEmpty()) return;
        TreeMap<String, String> treeMap = new TreeMap<>(map);
        appendTreeMap(treeMap, sb);
        sb.append("&");
    }

    /**
     * 拼接已经排序过的数组
     *
     * @param map
     * @param sb
     */
    private static void appendTreeMap(TreeMap<String, String> map, StringBuilder sb) {
        // 拼接
        for (Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            if (StringUtils.isNull(entry.getValue()))
                continue;
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        // 删除最后一个&
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
    }

    /**
     * 对字符串进行md5签名
     *
     * @param str
     * @return 小写的md5 32位签名
     */
    public static String md5sign(String str) {
        return DigestUtils.md5LowerCase(str, CHARSET);
    }
}
