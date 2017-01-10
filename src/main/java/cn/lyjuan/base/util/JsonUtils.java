package cn.lyjuan.base.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonUtils
{
	private static Gson gson;

	static
	{
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	}

	/**
	 * 将对象转换为 JSON 字符串
	 * @param obj	转换为 JSON 的字符串
	 * @return
	 */
	public static String to(Object obj)
	{
		return gson.toJson(obj);
	}

	/**
	 * 将 JSON 转换为指定的类
	 * @param c			类
	 * @param json		JSON 字符串
	 * @param <T>		泛型
	 * @return
	 */
	public static <T> T from(Class<T> c, String json)
	{
		return gson.fromJson(json, c);
	}

	/**
	 * 将 JSON 转换为指定的类
	 * @param type		根据TypeToken获取的返回类型
	 * @param json		JSON 字符串
	 * @param <T>		返回的数据类型
	 * @return
	 */
	public static <T> T from(Type type, String json)
	{
		return gson.fromJson(json, type);
	}
}
