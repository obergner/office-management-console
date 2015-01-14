package io.obergner.office.accounts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.obergner.office.ApiErrorCode;
import io.obergner.office.Application;
import io.obergner.office.Profiles;
import io.obergner.office.accounts.redis.AccountSchema;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
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

import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@DirtiesContext
@ActiveProfiles(Profiles.DEVELOPMENT)
public class AccountModificationControllerITest {

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
    public void post_account_creation_should_store_account_with_reference_to_existing_simsme_account_in_redis() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 783561234L;
        final SimsmeGuid refToExistingSimsmeAccount = new SimsmeGuid(0, UUID.randomUUID());
        final AccountCreation request = AccountCreation.newBuilder()
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels(RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS)
                .withReferenceToExistingSimsmeAccount(refToExistingSimsmeAccount)
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountcreations",
                request,
                String.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());

        final URI location = entity.getHeaders().getLocation();
        assertNotNull(location);

        final String locationPath = location.getPath();
        final String newAccountUuid = locationPath.substring(locationPath.lastIndexOf("/") + 1);

        final boolean accountHasBeenStored = this.redisClient.hexists(AccountSchema.Keys.accountUuid(newAccountUuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String addedSimsmeGuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(newAccountUuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(refToExistingSimsmeAccount, SimsmeGuid.parse(addedSimsmeGuid));
    }

    @Test
    public void post_account_creation_should_store_account_with_reference_to_new_simsme_account_in_redis() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 7835612111L;
        final AccountCreation request = AccountCreation.newBuilder()
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels(RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS)
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountcreations",
                request,
                String.class);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());

        final URI location = entity.getHeaders().getLocation();
        assertNotNull(location);

        final String locationPath = location.getPath();
        final String newAccountUuid = locationPath.substring(locationPath.lastIndexOf("/") + 1);

        final boolean accountHasBeenStored = this.redisClient.hexists(AccountSchema.Keys.accountUuid(newAccountUuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String addedSimsmeGuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(newAccountUuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertNotNull(addedSimsmeGuid);
    }

    @Test
    public void post_account_creation_should_return_error_response_if_new_account_has_duplicate_mma_id() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = RedisTestAccounts.existingAccountWithSimsmeAccountRef().mmaId;
        final AccountCreation request = AccountCreation.newBuilder()
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels(RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS)
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountcreations",
                request,
                String.class);

        assertEquals(HttpStatus.CONFLICT, entity.getStatusCode());

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.CONFLICT.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.DUPLICATE_ACCOUNT_MMAID, responseCode.asText());
    }

    @Test
    public void post_account_creation_should_return_error_response_if_new_account_has_empty_name() throws Exception {
        final long newAccountMmaId = 77000888L;
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode tree = mapper.createObjectNode();
        tree.put("name", "").put("mmaId", newAccountMmaId).putArray("allowedOutChannels").add("NewOutChannel1");
        final String request = mapper.writeValueAsString(tree);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final ResponseEntity<String> entity = this.restClient.exchange("http://localhost:" + this.port + "/accountcreations",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void post_account_creation_should_return_error_response_if_new_account_has_non_positive_mma_id() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 0L;
        final AccountCreation request = AccountCreation.newBuilder()
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels(RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS)
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountcreations",
                request,
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
    public void post_account_creation_should_return_error_response_if_new_account_has_no_allowed_out_channels() throws Exception {
        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 1234166734222L;
        final AccountCreation request = AccountCreation.newBuilder()
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels()
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountcreations",
                request,
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
    public void post_account_update_should_return_updated_account_and_update_it_in_redis() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 1234166734222L;
        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels("SIMSme", "USSD")
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final URI location = entity.getHeaders().getLocation();
        assertNotNull(location);

        final String locationPath = location.getPath();
        final String newAccountUuid = locationPath.substring(locationPath.lastIndexOf("/") + 1);
        assertEquals(request.uuid, UUID.fromString(newAccountUuid));

        final String updatedAccountUuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.UUID);
        assertEquals(request.uuid.toString(), updatedAccountUuid);

        final String updatedAccountName = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.NAME);
        assertEquals(request.name, updatedAccountName);

        final String updatedAccountMmaId = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(request.mmaId, Long.parseLong(updatedAccountMmaId));

        final String[] updatedAccountAllowedOutChannels = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(request.allowedOutChannels, updatedAccountAllowedOutChannels);

        final String secondaryMmaIdIndex = this.redisClient.hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(request.mmaId));
        assertEquals(request.uuid.toString(), secondaryMmaIdIndex);
    }

    @Test
    public void post_account_update_should_add_reference_to_existing_simsme_account_in_redis() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final SimsmeGuid refToExistingSimsmeAccount = new SimsmeGuid(0, UUID.randomUUID());
        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(accountToUpdate.name)
                .withMmaId(accountToUpdate.mmaId)
                .withAllowedOutChannels(accountToUpdate.allowedOutChannels)
                .withReferenceToExistingSimsmeAccount(refToExistingSimsmeAccount)
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final String addedSimsmeGuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(refToExistingSimsmeAccount, SimsmeGuid.parse(addedSimsmeGuid));
    }

    @Test
    public void post_account_update_should_add_reference_to_new_simsme_account_in_redis() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(accountToUpdate.name)
                .withMmaId(accountToUpdate.mmaId)
                .withAllowedOutChannels(accountToUpdate.allowedOutChannels)
                .withReferenceToNewSimsmeAccount("Added SIMSme account", "hhghffhd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final String addedSimsmeGuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertNotNull(addedSimsmeGuid);
    }

    @Test
    public void post_account_update_should_remove_reference_to_simsme_account_in_redis() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithSimsmeAccountRef();

        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(accountToUpdate.name)
                .withMmaId(accountToUpdate.mmaId)
                .withAllowedOutChannels(accountToUpdate.allowedOutChannels)
                .withDeletionOfExistingSimsmeAccountReference(accountToUpdate.simsmeAccountRef.simsmeGuid)
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        final String addedSimsmeGuid = this.redisClient.hget(AccountSchema.Keys.accountUuid(accountToUpdate.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertNull(addedSimsmeGuid);
    }

    @Test
    public void post_account_update_should_return_not_found_if_updating_a_non_existent_account() throws Exception {
        final UUID nonExistingAccountUuid = UUID.randomUUID();
        final long newAccountMmaId = 783561234L;
        final String newAccountName = this.testName.getMethodName();

        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(nonExistingAccountUuid)
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels("SIMSme", "USSD")
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void post_account_update_should_return_error_response_if_updated_account_has_empty_name() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final long newAccountMmaId = 77000888L;
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode tree = mapper.createObjectNode();
        tree.put("uuid", accountToUpdate.uuid.toString()).put("name", "").put("mmaId", newAccountMmaId).putArray("allowedOutChannels").add("NewOutChannel1");
        final String request = mapper.writeValueAsString(tree);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final ResponseEntity<String> entity = this.restClient.exchange("http://localhost:" + this.port + "/accountupdates",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }

    @Test
    public void post_account_update_should_return_error_response_if_updated_account_has_non_positive_mma_id() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 0L;
        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels(RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS)
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
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
    public void post_account_update_should_return_error_response_if_updated_account_has_no_allowed_out_channels() throws Exception {
        final Account accountToUpdate = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final String newAccountName = this.testName.getMethodName();
        final long newAccountMmaId = 1234166734222L;
        final AccountUpdate request = AccountUpdate.newBuilder()
                .withUuid(accountToUpdate.uuid)
                .withName(newAccountName)
                .withMmaId(newAccountMmaId)
                .withAllowedOutChannels()
                .withReferenceToNewSimsmeAccount("New SIMSme account", "78ehhggdd")
                .build();

        final ResponseEntity<String> entity = this.restClient.postForEntity("http://localhost:" + this.port + "/accountupdates",
                request,
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
    public void post_account_update_should_return_error_response_if_account_to_update_contains_malformed_uuid() throws Exception {
        final long newAccountMmaId = 77000888L;
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode tree = mapper.createObjectNode();
        tree.put("uuid", "a nonsense UUID").put("name", "New Name").put("mmaId", newAccountMmaId).putArray("allowedOutChannels").add("NewOutChannel1");
        final String request = mapper.writeValueAsString(tree);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final ResponseEntity<String> entity = this.restClient.exchange("http://localhost:" + this.port + "/accountupdates",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        final JsonNode jsonResponse = mapper.readTree(entity.getBody());
        final JsonNode responseStatus = jsonResponse.path("status");
        final JsonNode responseCode = jsonResponse.path("code");

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseStatus.asInt());
        assertEquals(ApiErrorCode.MALFORMED_REQUEST, responseCode.asText());
    }
}
