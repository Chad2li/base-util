local redisKey = KEYS[1]
local argObj = ARGV[1]
--- - 转码JSON
-- if string.sub(argObj, 1, 1) == '"' then
-- argObj = string.sub(argObj, 2, string.len(argObj) - 1)
-- argObj = string.gsub(argObj, '\\', '')
-- end

--local argObj = cjson.decode(ARGV[1])
--local hashKey = argObj.hashKey
--local incrby = argObj.incrby
--local limitEqMax = argObj.limitEqMax
--local limitEqMin = argObj.limitEqMin
--local exists = argObj.exists

local hashKey = ARGV[1]
local incrby = ARGV[2]
local limitEqMax = ARGV[3]
local limitEqMin = ARGV[4]
local exists = ARGV[5]
--redis.call('del', 'test:for:incrby:hashKey', 'test:for:incrby:hashKey1', 'test:for:incrby:hashKey2')
--redis.call('del', 'test:for:incrby:incrby', 'test:for:incrby:incrby1', 'test:for:incrby:incrby2')
--redis.call('del', 'test:for:incrby:limitEqMax', 'test:for:incrby:limitEqMax1', 'test:for:incrby:limitEqMax2')
--redis.call('del', 'test:for:incrby:limitEqMin', 'test:for:incrby:limitEqMin1', 'test:for:incrby:limitEqMin2')
--redis.call('del', 'test:for:incrby:exists', 'test:for:incrby:exists1', 'test:for:incrby:exists2')
if string.find(hashKey, 'null', 1) then hashKey = nil end
if string.find(limitEqMax, 'null', 1) then limitEqMax = nil end
if string.find(limitEqMin, 'null', 1) then limitEqMin = nil end
if string.find(exists, 'null', 1) then exists = nil end

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

local value
if hashKey and not '' == hashKey then
    redis.call('set', 'test:for:incrby:hashKey', hashKey)
    value = redis.call("hget", redisKey, tostring(hashKey))
else
    value = redis.call("get", redisKey)
end

-- 设置了nx，但值存在
if "NX" == exists and value then
    return { -1, value }
    -- 设置了xx，但值不存在
elseif "XX" == exists and not value then
    return { -2, 0 }
end

if not value then
    value = 0
end

local newVal = value + incrby
--if limitEqMax then
--    redis.call('set', 'test:for:incrby:limitEqMax2', limitEqMax)
--end
--if limitEqMin then
--    redis.call('set', 'test:for:incrby:limitEqMin2', limitEqMin)
--end
-- 限制最大值
if limitEqMax and newVal > tonumber(limitEqMax) then
    return { -3, value }
    -- 限制最小值
elseif limitEqMin and newVal < tonumber(limitEqMin) then
    return { -4, value }
end

-- 操作成功
if hashKey then
    return { 1, redis.call("hincrby", redisKey, hashKey, incrby) }
else
    return { 1, redis.call("incrby", redisKey, incrby) }
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
