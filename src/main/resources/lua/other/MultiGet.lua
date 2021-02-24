local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local keys = argObj.keys
local sep = argObj.sep

local values = {}

-- 获取hash值
if hashKey then
    for i, v in ipairs(keys) do
        values[i] = redis.call("hget", redisKey, hashKey .. sep .. v)
    end
else
    for i, v in ipairs(keys) do
        values[i] = redis.call("get", redisKey .. sep .. v)
    end
end

return values
