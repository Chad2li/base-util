-- 给拼接ID字符中新增ID
-- 百万条数据响应时间在 200毫秒左右
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local id = argObj.id

local ids = redis.call("hget", redisKey, hashKey)

if not ids then
    redis.call("hset", redisKey, hashKey, ',' .. id .. ',')
    return 1
end

local start = string.find(ids, ',' .. id .. ',')

-- 不存在则增加
if not start then
    redis.call("hset", redisKey, hashKey, ids .. id .. ',')
    return 1
end

return 0
