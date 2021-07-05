package cn.lyjuan.base.test;

import cn.lyjuan.base.http.aop.service.IHeaderService;
import cn.lyjuan.base.util.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class SpringTestUtil {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.APPLICATION_JSON;
    /**
     * 成功响应
     */
    public static final String SUCC_CODE = "BASE_COMMON_0000";

    @Setter
    protected String appKey;

    @Resource
    protected MockMvc mvc;


    /**
     * 发送HTTP请求
     *
     * @return
     */
    public ResultActions http(String path, String method, IHeaderService.AHeaderParam header, TreeMap<String, String> queryParams, String body) throws Exception {

        MockHttpServletRequestBuilder builder = null;
        switch (method) {
            case HttpUtils.HTTP_METHOD_GET:
                builder = MockMvcRequestBuilders.get(path);
                break;
            case HttpUtils.HTTP_METHOD_POST:
                builder = MockMvcRequestBuilders.post(path);
                break;
            case HttpUtils.HTTP_METHOD_PUT:
                builder = MockMvcRequestBuilders.put(path);
                break;
            case HttpUtils.HTTP_METHOD_DELETE:
                builder = MockMvcRequestBuilders.delete(path);
                break;
        }


        // query params
        if (null != queryParams && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> p : queryParams.entrySet()) {
                if (StringUtils.isNull(p.getValue())) continue;
                builder.queryParam(p.getKey(), p.getValue());
            }
        }

        // body
        if (!StringUtils.isNull(body))
            builder.content(body).contentType(JSON_MEDIA_TYPE);

        // header
        String signStr = HttpSignUtil.appendSign(path, method, header, queryParams, body, this.appKey);
        String sign = HttpSignUtil.md5sign(signStr);
        HttpHeaders httpHeaders = toHttpHeader(header);
        httpHeaders.add("sign", sign);
        builder.headers(httpHeaders);

        return mvc.perform(builder.accept(JSON_MEDIA_TYPE));
    }

    /**
     * 获取响应值
     *
     * @param actions
     * @return
     * @throws Exception
     */
    public <T> T res(ResultActions actions, Type type) throws Exception {
        String json = resJson(actions);

        return JsonUtils.from(type, json);
    }

    public String resJson(ResultActions actions) throws Exception {
        String json = actions
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        log.debug("res ==> {}", json);
        return json;
    }

    /**
     * 生成query参数，一个key一个value
     *
     * @param values
     * @return
     */
    public static TreeMap<String, String> query(Object... values) {
        TreeMap<String, String> query = new TreeMap<>();
        if (StringUtils.isNull(values))
            return query;

        for (int i = 0; i < values.length; i += 2) {
            query.put(StringUtils.toStr(values[i]), StringUtils.toStr(values[i + 1]));
        }

        return query;
    }

    private static HttpHeaders toHttpHeader(IHeaderService.AHeaderParam header) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, Field> fmap = ReflectUtils.fields(header.getClass());
        for (Map.Entry<String, Field> e : fmap.entrySet()) {
            httpHeaders.add(e.getKey(), StringUtils.toStr(e.getValue()));
        }
        return httpHeaders;
    }
}
