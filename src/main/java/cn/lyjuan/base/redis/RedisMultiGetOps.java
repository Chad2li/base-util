package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RedisMultiGetOps {
    public static final String BEAN_NAME = "appApiRedisMultiGetOps";

    private static DefaultRedisScript<List> multiGetScript = new DefaultRedisScript<>();

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisMultiGetOps(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    static {
        multiGetScript.setResultType(List.class);
        multiGetScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/other/MultiGet.lua")));
    }

    /**
     * 返回所有 {redisKey + keys} 的值
     *
     * @param redisKey  redis键
     * @param separator 完整Key的分隔符
     * @param keys      hash键，空表示sds结构
     * @return
     */
    public List getMultiKey(String redisKey, String separator, Object... keys) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("keys", keys);
        argvMap.put("sep", separator);

        String json = JsonUtils.to(argvMap);

        /**
         * 调用脚本并执行
         */
        List list = redisTemplate.execute(multiGetScript, keyList, json);

        return list;
    }
}
