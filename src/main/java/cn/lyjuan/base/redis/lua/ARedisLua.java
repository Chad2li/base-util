package cn.lyjuan.base.redis.lua;

import cn.lyjuan.base.redis.redisson.RedissonOps;
import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
public abstract class ARedisLua {
    protected DefaultRedisScript<List> redisScript;

    protected RedisTemplate<String, String> redisTemplate;

    protected RedissonOps redissonOps;
    /**
     * redisson 脚本执行器
     */
    protected RScript rScript;
    /**
     * redisson luaID
     */
    protected String rScriptSha;
    /**
     * redisson读写模式
     */
    protected RScript.Mode rScriptMode;
    /**
     * 脚本资源
     */
    protected String luaScriptFile;

    public ARedisLua(RedisTemplate<String, String> redisTemplate, RedissonOps redissonOps, String luaScriptFile) {
        this.redisTemplate = redisTemplate;
        this.redissonOps = redissonOps;
        this.luaScriptFile = luaScriptFile;
    }

    /**
     * 执行
     *
     * @param keyList
     * @param values
     * @return
     */
    protected Object execute(List keyList, Object... values) {
        if (null != this.redissonOps) {
            return execByRedisson(keyList, values);
        } else if (null != this.redisTemplate) {
            return execByRedisTemplate(keyList, values);
        } else {
            throw new NullPointerException("Exec redis lua script:" + this.luaScriptFile + ", but cannot ioc Redisson/RedisTemplate");
        }
    }

    protected Object execByRedisson(List keyList, Object... values) {
        if (StringUtils.isNull(this.rScriptSha)) {
            synchronized (this) {
                if (StringUtils.isNull(this.rScriptSha)) {
                    this.rScript = this.redissonOps.getScript();
                    ResourceScriptSource rs = new ResourceScriptSource(new ClassPathResource(luaScriptFile));
                    try {
                        this.rScriptSha = this.rScript.scriptLoad(rs.getScriptAsString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Exec Lua script:{} mode:{} returnType:{} keys:{} params:{}", this.rScriptSha, this.rScriptMode.name(), RScript.ReturnType.MULTI.name(),
                    JsonUtils.to(keyList), JsonUtils.to(values));
        }
        Object obj = this.rScript.evalSha(this.rScriptMode, this.rScriptSha, RScript.ReturnType.MULTI, (List<Object>) keyList, values);
        return obj;
    }

    protected List execByRedisTemplate(List<String> keyList, Object... values) {
        if (null == this.redisScript) {
            synchronized (this) {
                if (null == this.redisScript) {
                    this.redisScript = new DefaultRedisScript<>();
                    this.redisScript.setResultType(List.class);
                    this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(this.luaScriptFile)));
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Exec Lua script:{} keys:{} params:{}", this.redisScript.getSha1(),
                    JsonUtils.to(keyList), JsonUtils.to(values));
        }
        return this.redisTemplate.execute(redisScript, keyList, values);
    }
}
