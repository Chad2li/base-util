package cn.lyjuan.base.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtils
{
	private static Gson gson;

	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	static
	{
//		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
				return new JsonPrimitive(localDateTime.format(timeFormatter));
			}


		}).registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
			@Override
			public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
				return new JsonPrimitive(localDate.format(dateFormatter));
			}
		}).registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), timeFormatter);
			}
		}).registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
			@Override
			public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), dateFormatter);
			}
		}).create();
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
