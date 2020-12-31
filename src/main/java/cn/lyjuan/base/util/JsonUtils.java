package cn.lyjuan.base.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JsonUtils {
    private static Gson gson;

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
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
     *
     * @param obj 转换为 JSON 的字符串
     * @return
     */
    public static String to(Object obj) {
        if (null == obj) return null;

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
        } else if (objCls == Date.class)
            return new SimpleDateFormat(DateUtils.FMT_DATE_TIME).format((Date) obj);
        else if (objCls == LocalDate.class)
            return DateUtils.format((LocalDate) obj, DateUtils.FMT_DATE);
        else if (objCls == LocalDateTime.class)
            return DateUtils.format((LocalDateTime) obj, DateUtils.FMT_DATE_TIME);
        else if (objCls == LocalTime.class)
            return DateUtils.format((LocalTime) obj, DateUtils.FMT_TIME);

        return gson.toJson(obj);
    }

    /**
     * 将 JSON 转换为指定的类
     *
     * @param c    类
     * @param json JSON 字符串
     * @param <T>  泛型
     * @return
     */
    public static <T> T from(Class<T> c, String json) {
        return gson.fromJson(json, c);
    }

    /**
     * 将 JSON 转换为指定的类
     *
     * @param type 根据TypeToken获取的返回类型
     * @param json JSON 字符串
     * @param <T>  返回的数据类型
     * @return
     */
    public static <T> T from(Type type, String json) {
        return gson.fromJson(json, type);
    }
}
