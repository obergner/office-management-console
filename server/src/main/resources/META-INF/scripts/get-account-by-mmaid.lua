-- KEYS: account:mma:index
-- ARGV: mma

if redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1 then
    local accountuuid = redis.call('hget', KEYS[1], ARGV[1])
    return redis.call('HGETALL', 'account:uuid:' .. accountuuid)
else
    return redis.error_reply('api.error.account.no-account-with-matching-mmaid-found:Account with MMA ' .. ARGV[1] .. ' does not exist')
end

