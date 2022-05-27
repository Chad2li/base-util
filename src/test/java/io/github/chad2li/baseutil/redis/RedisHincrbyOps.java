package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.redis.lua.ARedisLua;
import io.github.chad2li.baseutil.redis.redisson.RedissonOps;
import io.github.chad2li.baseutil.util.JsonUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;

import java.util.*;

/**
 * @author chad
 * @date 2022/3/22 15:16
 * @since 1 by chad create
 */
@Slf4j
public class RedisHincrbyOps extends ARedisLua {

    /**
     * 脚本资源
     */
    private static final String LUA_SCRIPT_FILE = "lua/hincrby.lua";

    public RedisHincrbyOps(RedissonOps redissonOps) {
        super(null, redissonOps, LUA_SCRIPT_FILE);
        this.rScriptMode = RScript.Mode.READ_WRITE;
    }

    /**
     * 对redis值做增值操作
     *
     * @param hashKey    hash键，空表示sds结构
     * @param incrby     增加的步长，不传表示默认值 1
     * @param limitEqMax 限制最大值，当改变后的值超过最大值，此次操作失败；为空不限制
     * @param limitEqMin 限制最小值，当改变后的值小于最小值，此次操作失败；为空不限制
     * @param exists     空表示不对值的存在性作校验，NX强制不存在才操作，XX强制存在才操作
     * @param redisKeys  redis键
     * @return
     */
    public Result hincrby(Object hashKey, Integer incrby, Long limitEqMax, Long limitEqMin, RedisIncrbyOps.Exists exists, String... redisKeys) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = Arrays.asList(redisKeys);

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        Map<String, Object> argvMap = new HashMap<>();
        if (!StringUtils.isNull(hashKey))
            argvMap.put("hashKey", JsonUtils.to(hashKey));
        argvMap.put("incrby", null == incrby ? 1 : incrby);
        if (null != limitEqMax)
            argvMap.put("limitEqMax", limitEqMax);
        if (null != limitEqMin)
            argvMap.put("limitEqMin", limitEqMin);
        if (null != exists)
            argvMap.put("exists", exists.name());

        String json = JsonUtils.to(argvMap);
//        log.debug("Redis lua script argvObj: " + json);

        /**
         * 调用脚本并执行
         */
//        List list = (List) execute(keyList, json);
        String incrStr = String.valueOf(null == incrby ? 1 : incrby);
        String existCtl = null == exists ? null : exists.name();
        List<String> values = new ArrayList<>();
        // hashKey为null会出错，必须在null会加双引号
        List list = (List) execute(keyList, String.valueOf(JsonUtils.to(hashKey)), incrStr,
                String.valueOf(limitEqMax), String.valueOf(limitEqMin), String.valueOf(existCtl));
        log.debug("result ==> " + JsonUtils.to(list));
//
        Result result = new Result();
        result.code = Integer.parseInt(list.get(0).toString());
        result.values = new ArrayList<>(list.size() - 1);
        for (int i = 1; i < list.size(); i++) {
            Object val = list.get(i);
            result.values.add(null == val ? null : Long.parseLong(val.toString()));
        }

        return result;
    }

    /**
     * 操作结果
     */
    @Getter
    @ToString
    public static class Result {
        private int code;

        private List<Long> values;

        /**
         * 是否操作成功
         *
         * @return
         */
        public boolean isSucc() {
            return 1 == this.code;
        }

        /**
         * 由于值存在而失败，当设置 exists = NX时
         *
         * @return
         */
        public boolean isNXFail() {
            return RedisIncrbyOps.Code.NX.isCode(this.code);
        }

        /**
         * 由于值不存在而失败，当设置 exists = XX时
         *
         * @return
         */
        public boolean isXXFail() {
            return RedisIncrbyOps.Code.XX.isCode(this.code);
        }

        /**
         * 由于操作后值超MaxEq限制
         *
         * @return
         */
        public boolean isMaxFail() {
            return RedisIncrbyOps.Code.MAX.isCode(this.code);
        }

        /**
         * 由于操作后值超MinEq限制
         *
         * @return
         */
        public boolean isMinFail() {
            return RedisIncrbyOps.Code.MIN.isCode(this.code);
        }
    }

}
