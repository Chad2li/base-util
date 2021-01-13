package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Data
@Service(RedisIdsOps.REDIS_IDS_OPS_NAME)
public class RedisIdsOps {
    public static final String REDIS_IDS_OPS_NAME = "appApiRedisIdsOps";
    private static DefaultRedisScript<Long> addScript = new DefaultRedisScript<>();
    private static DefaultRedisScript<Long> delScript = new DefaultRedisScript<>();
    private static DefaultRedisScript<Long> existsScript = new DefaultRedisScript<>();
    private static DefaultRedisScript<Long> sizeScript = new DefaultRedisScript<>();

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisIdsOps(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    static {
        addScript.setResultType(Long.class);
        addScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/ids/AddSplitIds.lua")));

        delScript.setResultType(Long.class);
        delScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/ids/DelSplitIds.lua")));

        existsScript.setResultType(Long.class);
        existsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/ids/ExistsSplitIds.lua")));

        sizeScript.setResultType(Long.class);
        sizeScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/ids/SizeSplitIds.lua")));
    }

    /**
     * 返回所有ID
     *
     * @param redisKey redis键
     * @param hashKey  hash键
     * @return
     */
    public List<Integer> ids(String redisKey, Object hashKey) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        String ids = hash.get(redisKey, JsonUtils.to(hashKey));

        System.out.println(ids);
        if (StringUtils.isNull(ids)) return Collections.EMPTY_LIST;
        String[] arr = ids.split(",");
        List<Integer> list = new ArrayList<>(arr.length);
        for (String id : arr) {
            if (StringUtils.isNull(id)) continue;

            list.add(Integer.parseInt(id));
        }
        return list;
    }

    /**
     * 获取拼接ID数量
     *
     * @param redisKey
     * @param hashKey
     * @return
     */
    public Long size(String redisKey, Object hashKey) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("hashKey", hashKey);

        /**
         * 调用脚本并执行
         */
        Long result = redisTemplate.execute(sizeScript, keyList, JsonUtils.to(argvMap));

        return result;
    }

    /**
     * 判断ID是否在ID串中
     *
     * @param redisKey
     * @param hashKey
     * @param id
     */
    public boolean existsId(String redisKey, Object hashKey, Integer id) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("hashKey", hashKey);
        argvMap.put("id", id);

        /**
         * 调用脚本并执行
         */
        Long result = redisTemplate.execute(existsScript, keyList, JsonUtils.to(argvMap));

        return result > 0;
    }

    /**
     * 删除ID串中指定的ID
     *
     * @param redisKey
     * @param hashKey
     * @param id
     * @return
     */
    public Long delId(String redisKey, Object hashKey, Integer id) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("hashKey", hashKey);
        argvMap.put("id", id);

        /**
         * 调用脚本并执行
         */
        Long result = redisTemplate.execute(delScript, keyList, JsonUtils.to(argvMap));

        return result;
    }

    /**
     * 增加ID到拼接ID串中，不存在hash则创建
     *
     * @param redisKey
     * @param hashKey
     * @param id
     * @return
     */
    public Long addId(String redisKey, Object hashKey, Integer id) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        argvMap.put("hashKey", hashKey);
        argvMap.put("id", id);

        /**
         * 调用脚本并执行
         */
        Long result = redisTemplate.execute(addScript, keyList, JsonUtils.to(argvMap));

        return result;
    }
}
