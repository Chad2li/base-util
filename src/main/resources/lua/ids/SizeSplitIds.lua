--
-- Created by IntelliJ IDEA.
-- User: chad
-- Date: 2020/12/26
-- Time: 12:15
-- 获取拼接ID字符中大小
--
local redisKey = KEYS[1]
local argObj = cjson.decode(ARGV[1])
local hashKey = argObj.hashKey

local ids = redis.call("hget", redisKey, hashKey)

if not ids then
    return 0
end

local t, s = string.gsub(ids, ',', '')
return s - 1

--[[
local size = 0
for i=1,string.len(ids) do
    if 44 == string.byte(ids, i) then
        size = size + 1
    end
end
-- 去掉开头的,
return size - 1
]] --