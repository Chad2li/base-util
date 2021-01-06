-- 在bloom中新增数据
-- 百万条数据响应时间在 200毫秒左右
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local offset = argObj.offset

local old = 1
for i, v in ipairs(offset) do
    old = old * redis.call('setbit', redisKey, v, 1)
end

-- 原值不存在
if 0 == old then
    return 1
end

-- 原值存在
return 0