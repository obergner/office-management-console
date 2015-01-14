package io.obergner.office.accounts;

import io.obergner.office.accounts.impl.CoordinatingAccountManager;
import io.obergner.office.accounts.redis.RedisAccountDao;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManagementConfiguration;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManager;
import io.obergner.office.redis.client.RedisClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisPool;

@Configuration
@Import({RedisClientConfiguration.class, SimsmeAccountManagementConfiguration.class})
public class AccountManagementConfiguration {

    @Bean
    public AccountDao accountDao(final JedisPool jedisPool) {
        return new RedisAccountDao(jedisPool);
    }

    @Bean
    public AccountManager accountManager(final AccountDao accountDao, final SimsmeAccountManager simsmeAccountManager) {
        return new CoordinatingAccountManager(accountDao, simsmeAccountManager);
    }

    @Bean
    public AccountModificationController accountCreationController(final AccountManager accountManager) {
        return new AccountModificationController(accountManager);
    }

    @Bean
    public AccountController accountController(final AccountManager accountManager) {
        return new AccountController(accountManager);
    }
}
