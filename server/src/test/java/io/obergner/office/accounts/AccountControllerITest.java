package io.obergner.office.accounts;

import io.obergner.office.Application;
import io.obergner.office.Profiles;
import io.obergner.office.accounts.redis.AccountSchema;
import io.obergner.office.test.RedisTestAccounts;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@DirtiesContext
@ActiveProfiles(Profiles.DEVELOPMENT)
public class AccountControllerITest {

    @Rule
    public final TestName testName = new TestName();

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private JedisPool jedisPool;

    private Jedis redisClient;

    private final RestTemplate restClient = new TestRestTemplate();

    @Before
    public void checkOutRedisClient() {
        this.redisClient = this.jedisPool.getResource();
    }

    @Before
    public void prepareRedis() {
        this.redisClient.flushDB();
        RedisTestAccounts.storeUsing(this.accountDao);
    }

    @After
    public void returnRedisClient() {
        this.jedisPool.returnResource(this.redisClient);
    }

    @Test
    public void index_should_return_all_accounts_stored_in_redis() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final ResponseEntity<List<Account>> entity = this.restClient.exchange("http://localhost:" + this.port + "/accounts",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Account>>() {
                });

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        assertEquals(RedisTestAccounts.ALL_ACCOUNTS.size(), entity.getBody().size());
    }

    @Test
    public void delete_account_should_successfully_delete_existing_account() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().uuid.toString(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final boolean accountStillStoredInRedis = this.redisClient.exists(AccountSchema.Keys.accountUuid(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().uuid));
        assertFalse(accountStillStoredInRedis);
        final boolean accountMmaStillMappedInRedis = this.redisClient.hexists(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().mmaId));
        assertFalse(accountMmaStillMappedInRedis);
    }

    @Test
    public void delete_account_should_return_not_found_for_non_existing_account() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + UUID.randomUUID().toString(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void delete_account_should_return_api_validation_if_passing_a_malformed_uuid() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + "abcZ",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    public void account_by_uuid_should_return_matching_account() throws Exception {
        final ResponseEntity<Account> entity = this.restClient.getForEntity(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().uuid,
                Account.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final Account matchingAccount = entity.getBody();
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().uuid, matchingAccount.uuid);
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().name, matchingAccount.name);
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().mmaId, matchingAccount.mmaId);
    }

    @Test
    public void account_by_uuid_should_return_http_status_not_found_if_no_matching_account_could_be_found() throws Exception {
        final ResponseEntity<String> entity = this.restClient.getForEntity(
                "http://localhost:" + this.port + "/accounts/uuid/" + UUID.randomUUID().toString(),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void account_by_mma_id_should_return_matching_account() throws Exception {
        final ResponseEntity<Account> entity = this.restClient.getForEntity(
                "http://localhost:" + this.port + "/accounts/mma/" + RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().mmaId,
                Account.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final Account matchingAccount = entity.getBody();
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().uuid, matchingAccount.uuid);
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().name, matchingAccount.name);
        assertEquals(RedisTestAccounts.existingAccountWithoutSimsmeAccountRef().mmaId, matchingAccount.mmaId);
    }

    @Test
    public void account_by_mma_id_should_return_http_status_not_found_if_no_matching_account_could_be_found() throws Exception {
        final ResponseEntity<String> entity = this.restClient.getForEntity(
                "http://localhost:" + this.port + "/accounts/mma/" + 12345111111L,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }
}
