-- KEYS: account:mma:index
-- ARGV: uuid name mma

local accountkey = 'account:uuid:' .. ARGV[1]
local accountuuid = ARGV[1]
local accountname = ARGV[2]
local accountmma = ARGV[3]
if redis.call('hexists', accountkey, 'uuid') == 1 then
    return redis.error_reply('DuplicateAccountUUID')
elseif redis.call('hexists', KEYS[1], accountmma) == 1 then
    return redis.error_reply('DuplicateMMA')
else
    redis.call('hmset', accountkey, 'uuid', accountuuid, 'name', accountname, 'mma', accountmma)
    redis.call('hset', KEYS[1], accountmma, accountuuid)
    return redis.status_reply('OK')
end
