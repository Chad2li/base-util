-- 判断拼接ID中是否存在指定的ID
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local id = argObj.id

local ids = redis.call("hget", redisKey, hashKey)

if not ids then
    return 0
end

local start = string.find(ids, ',' .. id .. ',')

-- 存在
if start then
    return 1
end

return 0
