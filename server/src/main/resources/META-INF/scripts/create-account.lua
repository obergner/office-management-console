-- KEYS: account:mma:index
-- ARGV: uuid name mma created_at allowed_out_channels simsme_account_guid (opt.)

local accountkey = 'account:uuid:' .. ARGV[1]
local accountuuid = ARGV[1]
local accountname = ARGV[2]
local accountmma = ARGV[3]
local accountcreatedat = ARGV[4]
local accountallowedoutchannels = ARGV[5]
local accountsimsmeguid = ARGV[6]
if redis.call('hexists', accountkey, 'uuid') == 1 then
    return redis.error_reply('api.error.account.duplicate-account-uuid:Account with UUID ' .. accountuuid .. ' already exists')
elseif redis.call('hexists', KEYS[1], accountmma) == 1 then
    return redis.error_reply('api.error.account.duplicate-account-mma-id:MMA ' .. accountmma .. ' is already mapped to another account')
else
    redis.call('hmset', accountkey, 'uuid', accountuuid, 'name', accountname, 'mma', accountmma, 'created_at', accountcreatedat, 'outchannels', accountallowedoutchannels)
    redis.call('hset', KEYS[1], accountmma, accountuuid)
    if accountsimsmeguid ~= '__NULL__' then
        redis.call('hset', accountkey, 'simsme_account_guid', accountsimsmeguid)
    end
    return redis.status_reply('OK')
end
