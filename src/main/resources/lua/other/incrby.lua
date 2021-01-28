local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local incrby = argObj.incrby
local limitEqMax = argObj.limitEqMax
local limitEqMin = argObj.limitEqMin
local exists = argObj.exists

local value
if hashKey then
    value = redis.call("hget", redisKey, hashKey)
else
    value = redis.call("get", redisKey)
end

-- 设置了nx，但值存在
if "NX" == exists and value then
    return {-1, value}
    -- 设置了xx，但值不存在
elseif "XX" == exists and not value then
    return {-2, 0}
end

if not value then
    value = 0
end

local newVal = value + incrby
-- 限制最大值
if limitEqMax and newVal > limitEqMax then
    return {-3, value}
-- 限制最小值
elseif limitEqMin and newVal < limitEqMin then
    return {-4, value}

end

-- 操作成功
if hashKey then
    return {1, redis.call("hincrby", redisKey, hashKey, incrby)}
else
    return {1, redis.call("incrby", redisKey, incrby)}
end


--
-- User: chad
-- Date: 2021/1/16
-- Time: 21:49
-- 对 incrby 进行扩展操作
-- isHash, hashKey, increby, isMin, limitEq, exists
-- isHash 是否为hash结构，默认为sds
-- hashKey 如果为hash结构，从hashKey取值
-- increby 增加或减少的值
-- isMin 是否限制为最小值，默认为最大值
-- limitEq 限制的域值（包含）
-- exists: nx 不存在则处理，xx存在则处理，为空使用redis自动策略
-- 返回值 有两个
-- 第一个表示状态:
--      1 操作成功
--      -1 NX操作失败
--      -2 XX操作失败
--      -3 由于 limitEqMax 操作失败
--      -4 由于 limitEqMin 操作失败
-- 第二个表示：
--      操作成功后表示新值
--      操作失败表示原值（如果存在）
--      原值不存在则不返回
