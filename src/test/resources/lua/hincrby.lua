local redisKeys = KEYS
local argObj = ARGV[1]
--- - 转码JSON
-- if string.sub(argObj, 1, 1) == '"' then
-- argObj = string.sub(argObj, 2, string.len(argObj) - 1)
-- argObj = string.gsub(argObj, '\\', '')
-- end

local hashKey = ARGV[1]
local incrby = ARGV[2]
local limitEqMax = ARGV[3]
local limitEqMin = ARGV[4]
local exists = ARGV[5]

--redis.call('del', 'test:for:hincrby')
--redis.call('del', 'test:for:hincrbylist')
--redis.call('hset', 'test:for:hincrby', 'hashKey', hashKey, 'incrby', incrby, 'limitEqMax', limitEqMax, 'limitEqMin', limitEqMin, 'exists', exists)
--for i, v in ipairs(redisKeys) do
--    redis.call('hset', 'test:for:hincrby', i, v)
--end
if 'null' == hashKey or '"null"' == hashKey then hashKey = nil end
if 'null' == limitEqMax or '"null"' == limitEqMax then limitEqMax = nil end
if 'null' == limitEqMin or '"null"' == limitEqMin then limitEqMin = nil end
if 'null' == exists or '"null"' == exists then exists = nil end

--if string.find(limitEqMax, 'null', 1) then limitEqMax = nil end
--if string.find(limitEqMin, 'null', 1) then limitEqMin = nil end
--if string.find(exists, 'null', 1) then exists = nil end

if hashKey and string.sub(hashKey, 1, 1) == '"' then
    hashKey = string.sub(hashKey, 2, string.len(hashKey) - 1)
end
--if hashKey then
--    redis.call('set', 'test:for:incrby:hashKey1', hashKey)
--end
if incrby and string.sub(incrby, 1, 1) == '"' then
    incrby = string.sub(incrby, 2, string.len(incrby) - 1)
end
--if limitEqMax then
--    redis.call('set', 'test:for:incrby:limitEqMax1', limitEqMax)
--end
if limitEqMax and string.sub(limitEqMax, 1, 1) == '"' then
    limitEqMax = string.sub(limitEqMax, 2, string.len(limitEqMax) - 1)
end
--if limitEqMin then
--    redis.call('set', 'test:for:incrby:limitEqMin1', limitEqMin)
--end
if limitEqMin and string.sub(limitEqMin, 1, 1) == '"' then
    limitEqMin = string.sub(limitEqMin, 2, string.len(limitEqMin) - 1)
end
if exists and string.sub(exists, 1, 1) == '"' then
    exists = string.sub(exists, 2, 3)
    --    redis.call('set', 'test:for:incrby:exists', exists)
end
-- 第1个值为状态标识
local value = {1}
for i, rk in ipairs(redisKeys) do
--    redis.call('lpush', 'test:for:hincrbylist', rk)
    -- 填充value
    if hashKey then
        --    redis.call('set', 'test:for:incrby:hashKey', hashKey)
        value[i+1] = redis.call("hget", rk, tostring(hashKey))
    else
        value[i+1] = redis.call("get", rk)
    end

    -- 设置了nx，但值存在
    if "NX" == exists and value[i+1] then
        value[1]=-1
        return value
        -- 设置了xx，但值不存在
    elseif "XX" == exists and not value[i+1] then
        value[1]=-2
        return value
    end

    local oldVal = value[i+1]
    if not oldVal then
        oldVal = 0
    end

    local newVal = oldVal + incrby
    --if limitEqMax then
    --    redis.call('set', 'test:for:incrby:limitEqMax2', limitEqMax)
    --end
    --if limitEqMin then
    --    redis.call('set', 'test:for:incrby:limitEqMin2', limitEqMin)
    --end
    -- 限制最大值
    if limitEqMax and newVal > tonumber(limitEqMax) then
        value[1] = -3
        return value
        -- 限制最小值
    elseif limitEqMin and newVal < tonumber(limitEqMin) then
        value[1] = -4
        return value
    end
end

-- 失败，有错误
if not 1 == value[1] then
    return value
end

-- 操作成功
for i, rk in ipairs(redisKeys) do
    if hashKey then
        value[i+1] = redis.call("hincrby", rk, hashKey, incrby)
    else
        value[i+1] = redis.call("incrby", rk, incrby)
    end
end

return value

-- create by chad at 2022/3/22
-- 对多个 redis key 或多个redis key 的一个hash key 做 incrby 操作