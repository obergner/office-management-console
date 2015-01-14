-- KEYS: account:mma:index
-- ARGV: uuid name mma allowed_out_channels simsme_account_guid (opt.)

local accountkey = 'account:uuid:' .. ARGV[1]
local accountuuid = ARGV[1]

local newaccountname = ARGV[2]
local newaccountmma = ARGV[3]
local newaccountallowedoutchannels = ARGV[4]
local newaccountsimsmeguid = ARGV[5]

if redis.call('hexists', accountkey, 'uuid') == 0 then
    return redis.error_reply('api.error.account.no-account-with-matching-uuid-found:Account with UUID ' .. accountuuid .. ' does not exist')
end

if redis.call('hexists', accountkey, 'mma') == 0 then
    return redis.error_reply('api.error.account.inconsistent-data-found:Account ' .. accountuuid .. ' is not mapped to any MMA')
end

local oldaccountmma = redis.call('hget', accountkey, 'mma')
if newaccountmma ~= oldaccountmma then
    if redis.call('hexists', KEYS[1], newaccountmma) == 1 then
        return redis.error_reply('api.error.account.duplicate-account-mma-id:MMA ' .. newaccountmma .. ' is already mapped to another account')
    end
    redis.call('hdel', KEYS[1], oldaccountmma)
    redis.call('hset', KEYS[1], newaccountmma, accountuuid)
end

if newaccountsimsmeguid ~= '__NULL__' then
    redis.call('hset', accountkey, 'simsme_account_guid', newaccountsimsmeguid)
else
    redis.call('hdel', accountkey, 'simsme_account_guid')
end

redis.call('hmset', accountkey, 'uuid', accountuuid, 'name', newaccountname, 'mma', newaccountmma, 'outchannels', newaccountallowedoutchannels)

return redis.status_reply('OK')

