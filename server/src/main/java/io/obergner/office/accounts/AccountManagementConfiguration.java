package io.obergner.office.accounts;

import io.obergner.office.accounts.redis.RedisAccountManager;
import io.obergner.office.redis.client.RedisClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisPool;

@Configuration
@Import(RedisClientConfiguration.class)
public class AccountManagementConfiguration {

    @Autowired
    private JedisPool jedisPool;

    @Bean
    public AccountManager accountManager() {
        return new RedisAccountManager(this.jedisPool);
    }

    @Bean
    public AccountController accountController() {
        return new AccountController(accountManager());
    }
}
