package cn.lyjuan.base.redis;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
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
public class RedisIncrbyOps {
    public static final String BEAN_NAME = "appApiRedisIncrbyOps";

    private static DefaultRedisScript<List> incrbyScript = new DefaultRedisScript<>();

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisIncrbyOps(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    static {
        incrbyScript.setResultType(List.class);
        incrbyScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/other/incrby.lua")));
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
     * @param redisKey redis键
     * @param hashKey  hash键，空表示sds结构
     * @param incrby    增加的步长，不传表示默认值 1
     * @param limitEqMax    限制最大值，当改变后的值超过最大值，此次操作失败；为空不限制
     * @param limitEqMin    限制最小值，当改变后的值小于最小值，此次操作失败；为空不限制
     * @param exists        空表示不对值的存在性作校验，NX强制不存在才操作，XX强制存在才操作
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
        System.out.println("Redis lua script argvObj: " + json);

        /**
         * 调用脚本并执行
         */
        List list = redisTemplate.execute(incrbyScript, keyList, json);
        System.out.println(list);

        Result result = new Result();
        result.code = Integer.parseInt(list.get(0).toString());
        result.value = Long.parseLong(list.get(1).toString());

        return result;
    }

    /**
     * 操作结果
     */
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
