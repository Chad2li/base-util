package io.github.chad2li.baseutil.redis;

import io.github.chad2li.baseutil.redis.lua.ARedisLua;
import io.github.chad2li.baseutil.redis.redisson.RedissonOps;
import io.github.chad2li.baseutil.util.JsonUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展redis incr和 hincr 命令
 * <p>
 * 1. 同时支持 incrby 和hash的 hincrby 命令。{@code hashKey}为null时使用 incrby，不为null时使用 hincrby<br/>
 * 2. 默认增值为1<br/>
 * 3. 支持限制{@code limitEqMax}(最大值，包含)和{@code limitEqMin}(最小值，包含)，当超出限制时不改变原值，并返回具体的操作失败原因<br/>
 * 4. {@code exists} 强制操作key存在或不存在，可用的值为{@link Exists}。该值为null表示不要求原值是否存在（当不存在默认原值为0）。
 * 原值不符合{@code exists}强制要求时，不改变原值，并返回具体操作失败原因<br/>
 * 5. 失败原因查看{@link Result}
 *
 * </p>
 */
@Slf4j
public class RedisIncrbyOps extends ARedisLua {
    public static final String BEAN_NAME = "appApiRedisIncrbyOps";

    /**
     * 脚本资源
     */
    private static final String LUA_SCRIPT_FILE = "lua/other/incrby.lua";


    public RedisIncrbyOps(@Autowired(required = false) RedisTemplate<String, String> redisTemplate
            , @Autowired(required = false) RedissonOps redissonOps) {
        super(redisTemplate, redissonOps, LUA_SCRIPT_FILE);
        this.redisTemplate = redisTemplate;
        this.redissonOps = redissonOps;
        this.rScriptMode = RScript.Mode.READ_WRITE;
    }

    public Result incrby(String redisKey, Object hashKey, Exists exists) {
        return incrby(redisKey, hashKey, null, null, null, exists);
    }

    public Result incrby(String redisKey, Object hashKey, Long limitEqMax) {
        return incrby(redisKey, hashKey, null, limitEqMax, null, null);
    }

    public Result incrbyMinXX(String redisKey, Object hashKey, int incrby, Long limitEqMin) {
        return incrby(redisKey, hashKey, incrby, null, limitEqMin, Exists.XX);
    }

    public Result incrby(String redisKey, Exists exists) {
        return incrby(redisKey, null, null, null, null, exists);
    }

    public Result incrby(String redisKey, Long limitEqMax) {
        return incrby(redisKey, null, null, limitEqMax, null, null);
    }

    /**
     * 限制最小值，且键值必须存在
     *
     * @param redisKey
     * @param limitEqMin
     * @return
     */
    public Result incrbyMinXX(String redisKey, Long limitEqMin) {
        return incrby(redisKey, null, null, null, limitEqMin, Exists.XX);
    }

    /**
     * 限制最小值，且键值必须存在
     *
     * @param redisKey
     * @param incrby     增加指定的值
     * @param limitEqMin
     * @return
     */
    public Result incrbyMinXX(String redisKey, int incrby, Long limitEqMin) {
        return incrby(redisKey, null, incrby, null, limitEqMin, Exists.XX);
    }


    /**
     * 对redis值做增值操作
     *
     * @param redisKey   redis键
     * @param hashKey    hash键，空表示sds结构
     * @param incrby     增加的步长，不传表示默认值 1
     * @param limitEqMax 限制最大值，当改变后的值超过最大值，此次操作失败；为空不限制
     * @param limitEqMin 限制最小值，当改变后的值小于最小值，此次操作失败；为空不限制
     * @param exists     空表示不对值的存在性作校验，NX强制不存在才操作，XX强制存在才操作
     * @return
     */
    public Result incrby(String redisKey, Object hashKey, Integer incrby, Long limitEqMax, Long limitEqMin, Exists exists) {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add(redisKey);

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
        result.value = Long.parseLong(list.get(1).toString());

        return result;
    }

    /**
     * 操作结果
     */
    @Setter
    @ToString
    public static class Result {
        private int code;

        private Long value;

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
            return Code.NX.isCode(this.code);
        }

        /**
         * 由于值不存在而失败，当设置 exists = XX时
         *
         * @return
         */
        public boolean isXXFail() {
            return Code.XX.isCode(this.code);
        }

        /**
         * 由于操作后值超MaxEq限制
         *
         * @return
         */
        public boolean isMaxFail() {
            return Code.MAX.isCode(this.code);
        }

        /**
         * 由于操作后值超MinEq限制
         *
         * @return
         */
        public boolean isMinFail() {
            return Code.MIN.isCode(this.code);
        }

        public int code() {
            return this.code;
        }

        public long value() {
            return this.value;
        }
    }

    /**
     * 结果状态码
     */
    public enum Code {
        SUCC(1)
        //
        , NX(-1)
        //
        , XX(-2)
        //
        , MAX(-3)
        //
        , MIN(-4)
        //
        ;
        private int code;

        Code(int code) {
            this.code = code;
        }

        public boolean isCode(int code) {
            return this.code == code;
        }
    }


    /**
     * Redis值存在性校验
     */
    public enum Exists {
        /**
         * 操作的值必须不存在
         */
        NX
        /**
         * 操作的值必须存在
         */
        , XX;
    }
}
