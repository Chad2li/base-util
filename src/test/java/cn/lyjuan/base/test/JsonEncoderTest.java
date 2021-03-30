package cn.lyjuan.base.test;

import cn.lyjuan.base.util.JsonUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonEncoderTest {
    public static void main(String[] args) throws Exception {
        String json = "{\"text\":\"发挥价格杠杆哈哈\",\"imgs\":[\"feedback\\/2021\\/03\\/25\\/6f0d7d70095b4b0d99fe817473969a4e.jpeg\"]}";
        System.out.println("ori ==> " + json);
        String url = URLDecoder.decode(json, StandardCharsets.UTF_8.name());
        System.out.println("URLd ==> " + url);
        System.out.println("URLe ==> " + URLDecoder.decode(url, StandardCharsets.UTF_8.name()));
        Map map = JsonUtils.from(Map.class, json);
        System.out.println("map ==> " + JsonUtils.to(map));

        System.out.println("rep ==> " + json.replaceAll("\\\\", ""));
    }
}
