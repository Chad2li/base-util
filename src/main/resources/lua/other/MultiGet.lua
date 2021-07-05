local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local isHash = argObj.isHash
local keys = argObj.keys
local sep = argObj.sep

local values = {}

-- 获取hash值
if isHash then
    for i, v in ipairs(keys) do
        values[i] = redis.call("hget", redisKey, v)
    end
elseif sep then
    for i, v in ipairs(keys) do
        values[i] = redis.call("get", redisKey .. sep .. v)
    end
else
    for i, v in ipairs(keys) do
        values[i] = redis.call("get", v)
    end
end

return values
