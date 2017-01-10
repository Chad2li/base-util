package cn.lyjuan.base.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class XmlUtils
{
	/**
	 * 获取XML中的对象值
	 * @param xml
	 * 				XML 文件
	 * @param name
	 * 				XML 文件中要获取值的对象名
	 * @return
	 */
	public static String getMember(String xml, String name)
	{
		if (null == xml || xml.trim().length() < 1
				|| null == name || name.trim().length() < 1)
			return "";

		int namei = xml.indexOf("<" + name + " ");
		int nameEndi = -1;
		if (namei < 0)
		{
			namei = xml.indexOf("<" + name + ">");
		}
		else
		{
			xml = xml.substring(namei);
			nameEndi = xml.indexOf(">");
		}

		if (namei < 0) return "";

		int nameLasti = xml.indexOf("</" + name + ">");
		if (nameLasti < 0) return "";

		if (nameEndi < 0)// 元素以<name>value</name>
			xml = xml.substring(namei + name.length() + 2, nameLasti);
		else
			xml = xml.substring(nameEndi + 1, nameLasti);

		if (xml.startsWith("<![CDATA[") && xml.length() >= "<![CDATA[]]>".length())
		{
			xml = xml.substring("<![CDATA[".length());
			xml = xml.substring(0, xml.length() - 3);
		}

		return xml;
	}

	/**
	 * 判断XML文件中是否有该属性
	 * @param xml
	 * @param name
	 * @return
	 */
	public static boolean hasMember(String xml, String name)
	{
		if (null == xml || xml.trim().length() < 1
				|| null == name || name.trim().length() < 1)
			return false;

		if (xml.indexOf("<" + name + ">") > -1 || xml.indexOf("<" + name + "/>") > -1)
			return true;

		return false;
	}

	/**
	 * 判断XML文件中是否有该属性
	 * @param xml
	 * @param name
	 * @return
	 */
	public static boolean hasMember(String xml, String...name)
	{
		if (null == name || name.length < 1)
			return false;

		boolean has = true;
		for (String n : name)
			has = has && hasMember(xml, n);

		return has;
	}

	public static String getMemberWithCDATA(String xml, String name)
	{
		String member = getMember(xml, name);

		if (StringUtils.isNull(member))
			return "";

		if (member.length() < 12)
			return member;

		member = member.indexOf("<![CDATA[") > -1 ? member.substring("<![CDATA[".length()) : member;

		member = member.indexOf("]]>") == member.length() - 3
				? member.substring(0, member.length() - 3) : member;

		return member;
	}

	/**
	 * 以 map 的 key 为 xml 元素，以 value 为元素值，封装 xml
	 * @param root		根元素，如果为 null 或 空，则不封闭根元素，由用户自行定义根元素
	 * @param xmlMap	转换 xml 元素
	 * @param skipNull	是否跳过空值
	 * @param putCDATA	是否填入CDATA标签
	 * @return
	 */
	public static String mapToXml(String root, Map<String, String> xmlMap, boolean skipNull, boolean putCDATA)
	{
		StringBuilder xml = new StringBuilder();

		boolean hasRoot = null != root && root.trim().length() > 0;

		if (hasRoot)
			xml.append("<" + root.trim() + ">");

		if (null != xmlMap && !xmlMap.isEmpty())
		{
			for (Iterator<Map.Entry<String, String>> it = xmlMap.entrySet().iterator(); it.hasNext(); )
			{
				Map.Entry<String, String> entry = it.next();

				if (skipNull && (null == entry.getValue() || entry.getValue().trim().length() < 1)) continue;

				if (null == entry.getKey()) continue;

				xml.append("<").append(entry.getKey()).append(">");
				if (putCDATA) xml.append("<![CDATA[");
				xml.append(null == entry.getValue() ? "" : entry.getValue());
				if (putCDATA) xml.append("]]>");
				xml.append("</").append(entry.getKey()).append(">");
			}
		}

		if (hasRoot)
			xml.append("</" + root.trim() + ">");

		return xml.toString();
	}

	/**
	 * 将对象转化为XML
	 * @param obj
	 * @return
	 */
	public static String genXml(Object obj)
	{
		if (null == obj) return "";

		StringBuilder sb = new StringBuilder();

		Class c = obj.getClass();

		Field[] fs = c.getDeclaredFields();
		if (null == fs || fs.length < 1) return "";

		String name = null;
		for (Field f : fs)
		{
			name = f.getName();
			sb.append("<").append(name).append(">")
					.append(ReflectUtils.getValue(obj, f.getName()))
					.append("</").append(name).append(">");
		}

		return sb.toString();
	}
}