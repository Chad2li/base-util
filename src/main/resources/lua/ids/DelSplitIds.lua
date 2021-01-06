-- 从拼接的ID字符串中删除指定的ID
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local id = argObj.id

local ids = redis.call("hget", redisKey, hashKey)

if not ids then
    return 0
end
local count = 0

-- 替换掉指定的ID
ids,count = string.gsub(ids, ','..id, '')

-- 没有替换任何数据
if 0 == count then
    return 0
end

-- 没有id了
if ids == ',' then
    redis.call('hdel', redisKey, hashKey)
    return 1
end

-- 重新设置更新后的ids
redis.call("hset", redisKey, hashKey, ids)
return 1