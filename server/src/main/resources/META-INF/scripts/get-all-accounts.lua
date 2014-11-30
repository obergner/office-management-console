--
-- Created by IntelliJ IDEA.
-- User: obergner
-- Date: 09.11.14
-- Time: 01:19
-- To change this template use File | Settings | File Templates.
--

local all_account_keys = redis.call('keys', 'account:uuid:*')
local data = {}

for idx, key in ipairs(all_account_keys) do
    data[idx] = redis.call('HMGET', key, 'uuid', 'name', 'mma', 'createdAt', 'allowedOutChannels')
end

return data

