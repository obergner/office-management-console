package io.obergner.office.accounts;

import io.obergner.office.accounts.redis.RedisAccountManager;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManagementConfiguration;
import io.obergner.office.redis.client.RedisClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisPool;

@Configuration
@Import({RedisClientConfiguration.class, SimsmeAccountManagementConfiguration.class})
public class AccountManagementConfiguration {

    @Bean
    public AccountManager accountManager(final JedisPool jedisPool) {
        return new RedisAccountManager(jedisPool);
    }

    @Bean
    public AccountController accountController(final AccountManager accountManager) {
        return new AccountController(accountManager);
    }
}
