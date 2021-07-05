-- 在bloom中新增数据
-- 百万条数据响应时间在 200毫秒左右
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey
local offset = argObj.offset

local res = 0
for i,v in ipairs(offset) do
    res = redis.call('getbit', redisKey, v)
    if 0 == res then
        return 0
    end
end

return 1
