package io.github.chad2li.baseutil.util;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class XmlUtils {
    /**
     * 获取XML中的对象值
     *
     * @param xml  XML 文件
     * @param name XML 文件中要获取值的对象名
     * @return
     */
    public static String getMember(String xml, String name) {
        if (null == xml || xml.trim().length() < 1
                || null == name || name.trim().length() < 1) {
            return "";
        }

        int namei = xml.indexOf("<" + name + " ");
        int nameEndi = -1;
        if (namei < 0) {
            namei = xml.indexOf("<" + name + ">");
        } else {
            xml = xml.substring(namei);
            nameEndi = xml.indexOf(">");
        }

        if (namei < 0) {
            return "";
        }

        int nameLasti = xml.indexOf("</" + name + ">");
        if (nameLasti < 0) {
            return "";
        }

        if (nameEndi < 0) {
            // 元素以<name>value</name>

            xml = xml.substring(namei + name.length() + 2, nameLasti);
        } else {
            xml = xml.substring(nameEndi + 1, nameLasti);
        }
        if (xml.startsWith("<![CDATA[") && xml.length() >= "<![CDATA[]]>".length()) {
            xml = xml.substring("<![CDATA[".length());
            xml = xml.substring(0, xml.length() - 3);
        }

        return xml;
    }

    /**
     * 判断XML文件中是否有该属性
     *
     * @param xml
     * @param name
     * @return
     */
    public static boolean hasMember(String xml, String name) {
        if (null == xml || xml.trim().length() < 1
                || null == name || name.trim().length() < 1) {
            return false;
        }

        if (xml.indexOf("<" + name + ">") > -1 || xml.indexOf("<" + name + "/>") > -1) {
            return true;
        }

        return false;
    }

    /**
     * 判断XML文件中是否有该属性
     *
     * @param xml
     * @param name
     * @return
     */
    public static boolean hasMember(String xml, String... name) {
        if (null == name || name.length < 1) {
            return false;
        }

        boolean has = true;
        for (String n : name) {
            has = has && hasMember(xml, n);
        }
        return has;
    }

    public static String getMemberWithCDATA(String xml, String name) {
        String member = getMember(xml, name);

        if (StringUtils.isNull(member)) {
            return "";
        }

        if (member.length() < 12) {
            return member;
        }

        member = member.indexOf("<![CDATA[") > -1 ? member.substring("<![CDATA[".length()) : member;

        member = member.indexOf("]]>") == member.length() - 3
                ? member.substring(0, member.length() - 3) : member;

        return member;
    }

    /**
     * 以 map 的 key 为 xml 元素，以 value 为元素值，封装 xml
     *
     * @param root     根元素，如果为 null 或 空，则不封闭根元素，由用户自行定义根元素
     * @param xmlMap   转换 xml 元素
     * @param skipNull 是否跳过空值
     * @param putCDATA 是否填入CDATA标签
     * @return
     */
    public static String mapToXml(String root, Map<String, String> xmlMap, boolean skipNull, boolean putCDATA) {
        StringBuilder xml = new StringBuilder();

        boolean hasRoot = null != root && root.trim().length() > 0;

        if (hasRoot) {
            xml.append("<" + root.trim() + ">");
        }

        if (null != xmlMap && !xmlMap.isEmpty()) {
            for (Iterator<Map.Entry<String, String>> it = xmlMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> entry = it.next();

                if (skipNull && (null == entry.getValue() || entry.getValue().trim().length() < 1)) {
                    continue;
                }

                if (null == entry.getKey()) {
                    continue;
                }

                xml.append("<").append(entry.getKey()).append(">");
                if (putCDATA) {
                    xml.append("<![CDATA[");
                }
                xml.append(null == entry.getValue() ? "" : entry.getValue());
                if (putCDATA) {
                    xml.append("]]>");
                }
                xml.append("</").append(entry.getKey()).append(">");
            }
        }

        if (hasRoot) {
            xml.append("</" + root.trim() + ">");
        }

        return xml.toString();
    }

    /**
     * 给生成XML的结果增加 Root 标签
     *
     * @param root
     * @param obj
     * @return
     */
    public static String genXml(String root, Object obj) {
        if (StringUtils.isNull(root)) {
            return genXml(obj);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<").append(root).append(">")
                .append(genXml(obj))
                .append("</").append(root).append(">");

        return sb.toString();
    }

    /**
     * 将对象转换为XML文档
     * 并默认删除为空的属性值
     *
     * @param obj
     * @return
     */
    public static String genXml(Object obj) {
        return genXml(obj, true);
    }

    /**
     * 将对象转换为XML文档
     *
     * @param obj
     * @param delNull true删除为空（Null或空字符串）的属性
     * @return
     */
    public static String genXml(Object obj, boolean delNull) {
        return genXml(obj, null, delNull);
    }

    /**
     * 将对象转化为XML
     * List需要加 XmlItem 注解标识List item名称
     *
     * @param obj
     * @param delNull true删除为空（Null或空字符串）的属性
     * @return
     */
    private static String genXml(Object obj, Field field, boolean delNull) {
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
                return "";
            }
            if (null != field) {
                XmlItem itemAnnotation = field.getAnnotation(XmlItem.class);
                if (null == itemAnnotation) {
                    throw new RuntimeException("List field <" + field.getName() + "> must has XmlItem annotation");
                }
                String tag = itemAnnotation.value();
                if (StringUtils.isNull(tag)) {
                    throw new RuntimeException("XmlItem of <" + field.getName() + "> must has value");
                }
                for (Object listo : list) {
                    sb.append("<").append(tag).append(">");
                    sb.append(genXml(listo, null, delNull));
                    sb.append("</").append(tag).append(">");
                }
            } else {
                for (Object listo : list) {
                    sb.append(genXml(listo, null, delNull));
                }
            }
            return sb.toString();
        }

        if (Set.class.isInstance(obj)) {
            Set set = (Set) obj;
            if (set.isEmpty()) {
                return "";
            }
            if (null != field) {
                XmlItem itemAnnotation = field.getAnnotation(XmlItem.class);
                if (null == itemAnnotation) {
                    throw new RuntimeException("List field <" + field.getName() + "> must has XmlItem annotation");
                }
                String tag = itemAnnotation.value();
                if (StringUtils.isNull(tag)) {
                    throw new RuntimeException("XmlItem of <" + field.getName() + "> must has value");
                }
                for (Iterator<Object> it = set.iterator(); it.hasNext(); ) {
                    Object setto = it.next();
                    sb.append("<").append(tag).append(">");
                    sb.append(genXml(setto, null, delNull));
                    sb.append("</").append(tag).append(">");
                }
            } else {
                for (Iterator<Object> it = set.iterator(); it.hasNext(); ) {
                    Object setto = it.next();
                    sb.append(genXml(setto, null, delNull));
                }
            }

            return sb.toString();
        }

        if (Map.class.isInstance(obj)) {
            Map mapo = (Map) obj;
            if (mapo.isEmpty()) {
                return "";
            }

            for (Iterator<Map.Entry<Object, Object>> it = mapo.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Object, Object> keyo = it.next();
                sb.append("<").append(keyo.getKey().toString()).append(">");
                sb.append(genXml(keyo.getValue(), null, delNull));
                sb.append("</").append(keyo.getKey().toString()).append(">");
            }

            return sb.toString();
        }

        // toString
        Field[] fs = obj.getClass().getDeclaredFields();

        if (null == fs || fs.length < 1) {
            return "";
        }


        // 当前类
        for (Field f : fs) {
            f.setAccessible(true);
            Object sub = null;
            try {
                sub = f.get(obj);
                if (delNull && StringUtils.isNull(sub)) {// 根据 delNull 参数去掉为空的属性
                    continue;
                }
                if ("this$0".equals(f.getName())) {
                    continue;
                }
                sb.append("<").append(f.getName()).append(">");
                sb.append(genXml(sub, f, delNull));
                sb.append("</").append(f.getName()).append(">");
            } catch (IllegalAccessException e) {// 已处理访问控制，不会有此异常
                throw new RuntimeException(e);
            }
        }

        return sb.toString();
    }

    /**
     * 将 xml 文件解析成 cls 对象
     *
     * @param xml
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromXml(String xml, Type type, String itemName) {
        if (StringUtils.isNull(xml)) {
            return null;
        }

        if (StringUtils.isNull(xml)) {
            return null;
        }
        if (type == String.class) {
            return (T) xml;
        }
        if (type == Integer.class || type == int.class) {
            return (T) new Integer(xml);
        }
        if (type == Byte.class || type == byte.class) {
            return (T) new Byte(xml);
        }
        if (type == Boolean.class || type == boolean.class) {
            return (T) new Boolean(xml);
        }
        if (type == Float.class || type == float.class) {
            return (T) new Float(xml);
        }
        if (type == Double.class || type == double.class) {
            return (T) new Double(xml);
        }
        if (type == Character.class || type == char.class) {
            return (T) new Character(xml.charAt(0));
        }
        if (type == Long.class || type == long.class) {
            return (T) new Long(xml);
        }
        if (type == Short.class || type == short.class) {
            return (T) new Short(xml);
        }
        // list set map array
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            Type[] ts = ((ParameterizedType) type).getActualTypeArguments();
//            if ((rawType instanceof Class))  return fromXml(xml, )
            Class cls = (Class) rawType;
            itemName = StringUtils.isNull(itemName) ? "item" : itemName;
            int itemNameLen = itemName.length();

            if (List.class.isAssignableFrom(cls)) {
                // xmlItem
                Type itemCls = ts[0];

                String itemXml = null;

                itemXml = xml.substring(xml.indexOf("<" + itemName + ">") + itemNameLen + 3, xml.indexOf("</" + itemName + ">"));

            }
            if (Map.class.isAssignableFrom(cls)) {

            }
            if (Set.class.isAssignableFrom(cls)) {

            }
        } else if (type instanceof Class) {
            Class cls = (Class) type;
            if (cls.isArray()) {
                // array
            } else {
                // object
            }
        }
        if (List.class == type) {
            List list = new ArrayList();
        }

        return null;
    }

    public static <T> T fromXml(String xml, Field field, Class<T> cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (StringUtils.isNull(xml)) {
            return null;
        }
/**
 String name = field.getName();
 T obj = cls.getConstructor().newInstance();

 if (cls == String.class){

 }
 || cls == Integer.class
 || cls == Byte.class
 || cls == Boolean.class
 || cls == Float.class
 || cls == Double.class
 || cls == Character.class
 || cls == Long.class
 || cls == Short.class)
 {
 return String.valueOf(obj);
 } else if (cls == Date.class)
 return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) obj);
 else if (cls == LocalDate.class)
 return DateUtils.format((LocalDate) obj, "yyyy-MM-dd");
 else if (cls == LocalDateTime.class)
 return DateUtils.format((LocalDateTime) obj, "yyyy-MM-dd HH:mm:ss");
 return null;
 */
        return null;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface XmlItem {
        String value() default "item";
    }
}