-- KEYS: account:mma:index
-- ARGV: uuid

local accountkey = 'account:uuid:' .. ARGV[1]
if redis.call('exists', accountkey) == 0 then
    return redis.error_reply('api.error.account.no-account-with-matching-uuid-found:Account with UUID ' .. ARGV[1] .. ' does not exist')
elseif redis.call('hexists', accountkey, 'mma') == 0 then
    return redis.error_reply('api.error.account.inconsistent-data-found:Account ' .. ARGV[1] .. ' is not mapped to any MMA')
else
    local accountmma = redis.call('hget', accountkey, 'mma')
    redis.call('hdel', KEYS[1], accountmma)
    redis.call('del', accountkey)
    return redis.status_reply('OK')
end
