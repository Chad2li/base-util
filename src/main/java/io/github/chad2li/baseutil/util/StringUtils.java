package io.github.chad2li.baseutil.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class StringUtils {

    /**
     * 判断一个字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(Object str) {
        return str != null && str.toString().matches("^-?[0-9]+$");
    }

    /**
     * 判断一个对象转换为字符串是否为一定长度范围内的数字
     *
     * @param obj    字符串对象
     * @param min    最小长度
     * @param max    最大长度
     * @param isTrim 替换空格
     * @return boolean
     * @date 2022/1/21 10:51
     * @author chad
     * @since 如果该对象转换的字符串为数字，并长度在一定范围内，则返回 true，否则 false
     */
    public static boolean isNumLen(Object obj, int min, int max, boolean isTrim) {
        if (isNull(obj)) {
            return false;
        }
        String str = obj.toString();
        if (isTrim) {
            str = str.replaceAll(" ", "");
        }
        // 匹配正则
        boolean isOk = str.matches("^-?[\\d]{" + min + "," + max + "}$");
        return isOk;
    }

    /**
     * 判断一个字符串是否定长的字符
     *
     * @param str
     * @param len
     * @return
     */
    public static boolean isNumber(Object str, int len) {
        return str != null && str.toString().matches("^-?[0-9]{" + len + "}$");
    }

    /**
     * 是否为11位手机号，不允许有空格
     *
     * @param mobile 手机号
     * @return boolean true是手机号
     * @date 2022/1/21 10:54
     * @author chad
     * @since 1  by chad create
     */
    public static boolean isMobile(String mobile) {
        boolean isLen = isNumLen(mobile, 11, 11, false);
        if (!isLen) {
            return false;
        }
        // 首字符不是 -
        return !mobile.startsWith("-");
    }

    /**
     * 是否为邮箱格式
     *
     * @param email 邮箱
     * @return boolean true是
     * @date 2022/1/21 10:55
     * @author chad
     * @since 1 by chad create
     */
    public static boolean isEmail(String email) {
        if (isNull(email)) {
            return false;
        }

        // 正则匹配
        boolean isRegex = email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
        return isRegex;
    }

    /**
     * 脱敏邮箱信息
     *
     * @param email 邮箱
     * @return string 脱敏后的邮箱
     * @date 2022/1/21 10:57
     * @author chad
     * @since 1 by chad create
     */
    public static String hiddenEmail(String email) {
        boolean isEmail = isEmail(email);
        if (!isEmail) {
            throw new IllegalStateException("Not email format: " + email);
        }
        int atIndex = email.indexOf("@");
        String account = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        // 只保留首位和末位
        String accountHide = hide(account, '*', 1, 1);

        return accountHide + domain;
    }

    /**
     * @param mobile 需要做隐藏处理的手机号
     * @return 返回进行隐藏处理后的字符串
     * @author Chad
     * 对手机号码进行处理，在显示时隐藏手机号中间四位，
     * 如：13912341234 隐藏为 139****1234。
     * 该方法调用重载方法 hiddenPhoneNumber(phoneNumber, '*', 3, 7)
     * 注意：如果手机号为 null 或长度不足 7  ，返回 "" 空字符串
     */
    public static String hideMobile(String mobile) {
        return hide(mobile, '*', 3, 4);
    }

    /**
     * 隐藏银行卡号
     *
     * @param card
     * @return
     */
    public static String hideBankCard(String card) {
        if (!isNumber(card)) {
            throw new IllegalStateException("not bank card: " + card);
        }

        String hide = hide(card, '*', 6, 4);
        return hide;
    }

    public static String hideName(String name) {
        return hide(name, '*', 0, 1);
    }

    /**
     * @param replaceFrom 被替换的字符串，为空则返回 "" 空字符串。
     * @param replaceTo   使用该字符替换原字符串中的特定字符。
     * @param start       前保留几位，最小为0
     * @param end         后保留几位，最小为0
     * @return 返回进行隐藏处理后的字符串
     * @author Chad
     * 使用特定字符(replaceTo)替换原字符串(replaceFrom)中指定的内容(索引在 start 和 end 之间)。
     * <p>
     * 注意，如果出现以下情况，则返回 "" 空字符串：
     * 1. 要替换的字符串为空；
     */
    public static String hide(String replaceFrom, char replaceTo, int start, int end) {
        if (StringUtils.isNull(replaceFrom)) {
            return "";
        }
        //如果被替换字符串为空，或替换终点小于0， 返回 "" 空字符串
        if (end < 0) {
            end = 0;
        }
        //如果替换起点小于 0，则置为 0 。
        if (start < 0) {
            start = 0;
        }
        int len = replaceFrom.length();
        // hidden 保存用于拼接字符串
        StringBuilder hidden = new StringBuilder();

        if (start + end >= len) {
            // 当前显示字符大于整体长度时，起不到隐藏效果，故全部隐藏
            for (int i = 0; i < len; i++) {
                hidden.append(replaceTo);
            }
            return hidden.toString();
        }

        //拼接替换起点前的字符串
        if (start > 0) {
            hidden.append(replaceFrom, 0, start);
        }

        //拼接替换字符串，最后 end 不替换
        int dur = len - start - end;
        for (int i = 0; i < dur; i++) {
            hidden.append(replaceTo);
        }

        //拼接替换终点后的字符串
        if (end > 0) {
            hidden.append(replaceFrom, len - end, len);
        }

        return hidden.toString();
    }

    /**
     * 判断一个字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNull(Object str) {
        if (str == null || str.toString().trim().length() < 1) {
            return true;
        }

        // array
        if (str.getClass().isArray()) {
            return isNullArray(str);
        }

        return false;
    }

    /**
     * 判断一个数据是否为空
     *
     * @param arr
     * @return
     */
    public static boolean isNullArray(Object arr) {
        return null == arr || Array.getLength(arr) <= 0;
    }

    /**
     * 是否是空集合
     *
     * @param collection
     * @return
     */
    public static boolean isNull(Collection collection) {
        return null == collection || collection.isEmpty();
    }

    /**
     * 验证 map 是否为空
     *
     * @param map map对象
     * @return boolean true表示 map为 null或内容为空
     * @date 2022/1/4 17:44
     * @author chad
     * @since 1 by chad create
     */
    public static boolean isNull(Map map) {
        return null == map || map.isEmpty();
    }

    /**
     * 判断字符串的长度限制
     *
     * @param str    限制长度的字符串
     * @param min    最小长度，-1表示不限制
     * @param max    最大长度，-1表示不限制
     * @param isTrim 判断长度时是否去掉空格
     * @return
     */
    public static boolean isLen(Object str, int min, int max, boolean isTrim) {
        if (isNull(str)) {
            // 字符串为空
            return false;
        }

        if (isTrim) {
            // 去掉空格
            str = str.toString().trim();
        }

        // 长度判断
        boolean isLen = min != -1 && str.toString().length() < min || max != -1 && str.toString().length() > max;
        return isLen;
    }

    /**
     * 判断手机短信内容的长度
     * <p>默认不去掉空格，最小长度不限制，最大长度为 70<br />
     * 实际调用{@code isLen(msg, -1, 70, false)}
     *
     * @param msg
     * @return
     */
    public static boolean isSmsLen(String msg) {
        return isLen(msg, -1, 70, false);
    }

    /**
     * 将一个对象使用{@code toString}转换为字符串
     *
     * @param obj 转换对象
     * @return 如果对象为空，返回空字符串，否则返回该对象的{@code toString}的值
     */
    public static String nullToStr(Object obj) {
        return nullToStr(obj, true);
    }

    /**
     * 将一个对象使用{@code toString}转换为字符串
     *
     * @param obj  转换对象
     * @param trim 是否清掉字符串前后的空字符
     * @return 如果对象为空，返回空字符串，否则返回该对象的{@code toString}的值
     */
    public static String nullToStr(Object obj, boolean trim) {
        return isNull(obj) ? "" : trim ? obj.toString().trim() : obj.toString();
    }

    /**
     * 将一个字符串中的占位符按顺序依次替换
     *
     * @param src
     * @param param
     * @return
     */
    public static String format(String src, String plac, Object... param) {
        if (isNull(src) || isNull(param)) {
            return "";
        }

        int len = param.length;
        for (int i = 0; i < len; i++) {
            if (len < i) {
                return plac;
            }

            if (isNull(param[i])) {
                src = src.replaceFirst(plac, "");
            } else {
                src = src.replaceFirst(plac, param[i].toString());
            }
        }

        return src;
    }

    /**
     * 判断对象是否是数字 double 类型，没有小数点的数字也是 double 类型
     *
     * @param obj
     * @return 如果是 double 类型，返回true，否则返回 false
     */
    public static boolean isDouble(String obj) {
        return !isNull(obj) && obj.matches("(^[0-9]+\\.[0-9]+|([0-9]+$))");
    }

    /**
     * 判断字符串对象是否为时间格式
     *
     * @param obj     时间字符串
     * @param pattern 时间格式
     * @return true 表示该字符串为正确的时间格式，否则不是
     */
    public static boolean isDate(Object obj, String pattern) {
        LocalDate date = parseDate(obj, pattern);

        return !isNull(date);
    }

    /**
     * 根据时间格式解析字符串时间
     *
     * @param obj     字符串时间对象
     * @param pattern 时间解析模式
     * @return 成功解析返回时间{@code Date}，解析失败或{@code obj}参数为空， 返回{@code null}
     */
    public static LocalDate parseDate(Object obj, String pattern) {
        if (isNull(obj)) {
            return null;
        }

        return DateUtils.parseDate(obj.toString(), pattern);
    }

    /**
     * 判断对象是否为身份证格式
     *
     * @param cert
     * @return
     */
    public static boolean isCertId(String cert) {
        if (isNull(cert)) {
            return false;
        }

        boolean isCert = cert.trim().matches("^[\\d]{17}[\\dxX]$");
        return isCert;
    }

    /**
     * 判断是否为联通手机
     *
     * @param mobile
     * @param operator 1 移动 2 联通 3电信， 该标志关系到视图 vll_operator 数据
     * @return true 表示手机号格式正确，否则表示手机号格式错误
     */
    public static boolean isOperator(String mobile, int operator) {
        String reg = "^1[\\d]{10}$";

        switch (operator) {
            case 1: // 移动
                reg = "^1[\\d]{10}$";
                break;
            case 2: // 联通
                reg = "^13[0,1,2]\\d{8}$|^15[5,6]\\d{8}$|^18[5,6]\\d{8}$";
                break;
            case 3: // 电信
                reg = "^1[\\d]{10}$";
                break;
        }

        return !isNull(mobile) && mobile.trim().matches(reg);
    }

    /**
     * 处理分页参数
     *
     * @param param 分页参数
     * @param def   参数的默认值
     * @return
     */
    public static int pageParam(String param, int def) {
        return isNumber(param) && Integer.parseInt(param) > 0 ? Integer.parseInt(param) : def;
    }

    /**
     * 返回当前对象当前类信息
     *
     * @param obj
     * @return
     */
    public static String toStr(Object obj) {
        return toStr(obj, null, null);
    }

    /**
     * 输出对象的内容
     *
     * @param obj     对象
     * @param currCls 输出对象指定类的信息，默认打印当前类
     * @param stopCls 输出对象递归父类信息，停止于指定类（stopCls类对象信息不会输出），默认仅打印当前类
     * @return
     */
    public static String toStr(Object obj, Class currCls, Class stopCls) {
        if (null == obj) {
            return "";
        }

        // 基本类型直接输出
        if (null == obj) {
            return "";
        }
        Class objCls = obj.getClass();
        if (objCls == String.class
                || objCls == Integer.class
                || objCls == Byte.class
                || objCls == Boolean.class
                || objCls == Float.class
                || objCls == Double.class
                || objCls == Character.class
                || objCls == Long.class
                || objCls == Short.class) {
            return String.valueOf(obj);
        } else if (objCls == Date.class) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) obj);
        } else if (objCls == LocalDate.class) {
            return DateUtils.format((LocalDate) obj, "yyyy-MM-dd");
        } else if (objCls == LocalDateTime.class) {
            return DateUtils.format((LocalDateTime) obj, "yyyy-MM-dd HH:mm:ss");
        }

        StringBuilder sb = new StringBuilder();

        if (List.class.isInstance(obj)) {
            List list = (List) obj;

            if (list.isEmpty()) {
                return "[]";
            }
            sb.append("[");
            for (Object listo : list) {
                sb.append(toStr(listo)).append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("]");

            return sb.toString();
        }

        if (Map.class.isInstance(obj)) {
            Map mapo = (Map) obj;
            if (mapo.isEmpty()) {
                return "[]";
            }

            sb.append("[");
            for (Iterator<Map.Entry<Object, Object>> it = mapo.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Object, Object> keyo = it.next();
                sb.append(toStr(keyo.getKey())).append("=").append(toStr(keyo.getValue()));
                sb.append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("]");

            return sb.toString();
        }

        if (objCls.isArray()) {
            Class<?> arrCls = objCls.getComponentType();
            List list = null;
            int len = Array.getLength(obj);
            if (0 == len) {
                return "[]";
            }
            sb.append("[");
            for (int i = 0; i < len; i++) {
                sb.append(toStr(Array.get(obj, i))).append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("]");
            return sb.toString();
        }

        currCls = null == currCls ? obj.getClass() : currCls;// 默认打印当前类
        stopCls = null == stopCls ? obj.getClass() : stopCls;// 默认仅打印当前类
        // 指定的Class为Object或当前对象为Object，则简单输出Object.toString
        if (currCls == Object.class || obj.getClass() == Object.class) {
            return obj.toString();
        }

        Map<String, Field> fs = ReflectUtils.fields(currCls, true);

        if (null == fs || fs.isEmpty()) {
            return "";
        }


        sb.append(currCls.getSimpleName()).append("{");
        // 当前类
        for (Field f : fs.values()) {
            f.setAccessible(true);
            Object sub = null;
            try {
                sub = f.get(obj);
                if (sub == obj) {
                    sb.append(obj.toString()).append(", ");
                    break;
                }
            } catch (IllegalAccessException e) {// 已处理访问控制，不会有此异常
            }
            sb.append(f.getName() + "=");
            sb.append(toStr(sub, null, null));

            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        // 父类
        Class superCls = currCls.getSuperclass();
        if (superCls != Object.class// 父类为Object
                && currCls != stopCls // 当前类为停止类
                && obj.getClass() != stopCls// 当前对象类为停止类
        ) {
            sb.append(" Parent_").append(toStr(obj, superCls, null));
        }

        sb.append("}");

        return sb.toString();
    }
}
