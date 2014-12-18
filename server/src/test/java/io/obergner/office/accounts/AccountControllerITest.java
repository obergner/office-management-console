package io.obergner.office.accounts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.obergner.office.ApiError;
import io.obergner.office.ApiErrorCode;
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void create_account_should_return_created_account_and_store_it_in_redis() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 783561234L;
        final CreateAccount request = new CreateAccount(newAccountName, newAccountMmaId, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

        final ResponseEntity<Account> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accounts",
                request,
                Account.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());

        final Account createdAccount = entity.getBody();
        assertEquals(request.name, createdAccount.name);
        assertEquals(request.mmaId, createdAccount.mmaId);

        final boolean accountHasBeenStored = this.redisClient.hexists(AccountSchema.Keys.accountUuid(createdAccount.uuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);
    }

    @Test
    public void create_account_should_return_error_response_if_new_account_has_duplicate_mma_id() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long duplicateMmaId = RedisTestAccounts.existingAccount().mmaId;
        final CreateAccount request = new CreateAccount(newAccountName, duplicateMmaId, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accounts", request, String.class);

        assertEquals(HttpStatus.CONFLICT, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.CONFLICT.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.DUPLICATE_ACCOUNT_MMAID, responseCode.asText());
    }

    @Test
    public void create_account_should_return_error_response_if_new_account_has_empty_name() throws Exception {
        final String newAccountName = "";
        final CreateAccount request = new CreateAccount(newAccountName, 111111111111111L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accounts", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void create_account_should_return_error_response_if_new_account_has_non_positive_mma_id() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final CreateAccount request = new CreateAccount(newAccountName, 0L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accounts", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void create_account_should_return_error_response_if_new_account_has_no_allowed_out_channels() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final CreateAccount request = new CreateAccount(newAccountName, 2222222222222L, new String[0]);

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accounts", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void update_account_should_return_updated_account_and_update_it_in_redis() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 783561234L;
        final String[] newAccountAllowedOutChannels = new String[]{"Updated_Channel_1", "Updated_Channel_2"};
        final Account request = new Account(RedisTestAccounts.existingAccount().uuid,
                newAccountName,
                newAccountMmaId,
                RedisTestAccounts.existingAccount().createdAt,
                newAccountAllowedOutChannels);

        final ResponseEntity<Account> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                Account.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final Account updatedAccount = entity.getBody();
        assertEquals(request.name, updatedAccount.name);
        assertEquals(request.mmaId, updatedAccount.mmaId);
        assertArrayEquals(request.allowedOutChannels, updatedAccount.allowedOutChannels);

        final String accountNameInRedis = this.redisClient.hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(request.name, accountNameInRedis);
        final String accountMmaInRedis = this.redisClient.hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(request.mmaId, Long.parseLong(accountMmaInRedis));
    }

    @Test
    public void update_account_should_return_not_found_if_updating_a_non_existent_account() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final UUID nonExistingAccountUuid = UUID.randomUUID();
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 783561234L;
        final Account request = new Account(nonExistingAccountUuid, newAccountName, newAccountMmaId, -1L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + UUID.randomUUID().toString(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void update_account_should_return_error_response_if_updated_account_has_empty_name() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final String newAccountName = "";
        final long newAccountMmaId = 783561234L;
        final String[] newAccountAllowedOutChannels = new String[]{"Updated_Channel_1", "Updated_Channel_2"};
        final Account request = new Account(RedisTestAccounts.existingAccount().uuid.toString(),
                newAccountName,
                newAccountMmaId,
                RedisTestAccounts.existingAccount().createdAt,
                newAccountAllowedOutChannels);

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void update_account_should_return_error_response_if_updated_account_has_non_positive_mma_id() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = -1L;
        final String[] newAccountAllowedOutChannels = new String[]{"Updated_Channel_1", "Updated_Channel_2"};
        final Account request = new Account(RedisTestAccounts.existingAccount().uuid,
                newAccountName,
                newAccountMmaId,
                RedisTestAccounts.existingAccount().createdAt,
                newAccountAllowedOutChannels);

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void update_account_should_return_error_response_if_updated_account_has_no_allowed_out_channels() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 783561234L;
        final String[] newAccountAllowedOutChannels = new String[0];
        final Account request = new Account(RedisTestAccounts.existingAccount().uuid.toString(),
                newAccountName,
                newAccountMmaId,
                RedisTestAccounts.existingAccount().createdAt,
                newAccountAllowedOutChannels);

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void update_account_should_return_error_response_if_account_to_update_contains_malformed_uuid() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final String accountJson = "{\"uuid\":\"a-mightily-malformed-uuid\",\"name\":\"updateAccountShouldReturnErrorResponseIfAccountToUpdateContainsMalformedUuid\",\"mmaId\":783561234,\"createdAt\":1416949301176,\"allowedOutChannels\":[\"ch1\"]}";

        final ResponseEntity<ApiError> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.PUT,
                new HttpEntity<>(accountJson, headers),
                ApiError.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    public void delete_account_should_successfully_delete_existing_account() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> entity = this.restClient.exchange(
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid.toString(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final boolean accountStillStoredInRedis = this.redisClient.exists(AccountSchema.Keys.accountUuid(RedisTestAccounts.existingAccount().uuid));
        assertFalse(accountStillStoredInRedis);
        final boolean accountMmaStillMappedInRedis = this.redisClient.hexists(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(RedisTestAccounts.existingAccount().mmaId));
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
                "http://localhost:" + this.port + "/accounts/uuid/" + RedisTestAccounts.existingAccount().uuid,
                Account.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final Account matchingAccount = entity.getBody();
        assertEquals(RedisTestAccounts.existingAccount().uuid, matchingAccount.uuid);
        assertEquals(RedisTestAccounts.existingAccount().name, matchingAccount.name);
        assertEquals(RedisTestAccounts.existingAccount().mmaId, matchingAccount.mmaId);
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
                "http://localhost:" + this.port + "/accounts/mma/" + RedisTestAccounts.existingAccount().mmaId,
                Account.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final Account matchingAccount = entity.getBody();
        assertEquals(RedisTestAccounts.existingAccount().uuid, matchingAccount.uuid);
        assertEquals(RedisTestAccounts.existingAccount().name, matchingAccount.name);
        assertEquals(RedisTestAccounts.existingAccount().mmaId, matchingAccount.mmaId);
    }

    @Test
    public void account_by_mma_id_should_return_http_status_not_found_if_no_matching_account_could_be_found() throws Exception {
        final ResponseEntity<String> entity = this.restClient.getForEntity(
                "http://localhost:" + this.port + "/accounts/mma/" + 12345111111L,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }
}
