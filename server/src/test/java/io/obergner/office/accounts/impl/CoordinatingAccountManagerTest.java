package io.obergner.office.accounts.impl;

import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountCreation;
import io.obergner.office.accounts.redis.AccountSchema;
import io.obergner.office.accounts.redis.RedisAccountDao;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
import io.obergner.office.accounts.subaccounts.simsme.impl.DummySimsmeAccountManager;
import io.obergner.office.test.EmbeddedRedisServer;
import io.obergner.office.test.RedisTestAccounts;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoordinatingAccountManagerTest {

    private static final int PORT = 7379;

    @ClassRule
    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnPort(PORT);

    private static final RedisAccountDao REDIS_ACCOUNT_DAO = new RedisAccountDao("127.0.0.1", PORT);

    private static final CoordinatingAccountManager OBJECT_UNDER_TEST = new CoordinatingAccountManager(REDIS_ACCOUNT_DAO, new DummySimsmeAccountManager());

    @BeforeClass
    public static void initializeObjectUnderTest() throws IOException {
        REDIS_ACCOUNT_DAO.initialize();
    }

    @AfterClass
    public static void destroyObjectUnderTest() {
        REDIS_ACCOUNT_DAO.destroy();
    }

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void prepareRedis() {
        EMBEDDED_REDIS_SERVER.client().flushDB();
        RedisTestAccounts.storeUsing(REDIS_ACCOUNT_DAO);
    }

    @Test
    public void create_account_should_store_account_without_simsme_account_ref() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 17866534277L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel_1", "Allowed_Out_Channel_2"};
        final AccountCreation accountCreationWithSimsmeAccountRef = AccountCreation.newBuilder()
                .withName(accountName)
                .withMmaId(mmaId)
                .withAllowedOutChannels(allowedOutChannels)
                .build();

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountCreationWithSimsmeAccountRef);
        assertNotNull(newAccount);

        final boolean accountHasBeenStored = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String storedAccountUuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertEquals(newAccount.uuid.toString(), storedAccountUuid);

        final String storedAccountName = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(newAccount.name, storedAccountName);

        final String storedAccountMmaId = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(newAccount.mmaId, Long.parseLong(storedAccountMmaId));

        final String[] storedAccountAllowedOutChannels = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(newAccount.allowedOutChannels, storedAccountAllowedOutChannels);

        final String secondaryMmaIdIndex = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(newAccount.mmaId));
        assertEquals(newAccount.uuid.toString(), secondaryMmaIdIndex);
    }

    @Test
    public void create_account_should_store_account_with_reference_to_existing_simsme_account() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 17866534288L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel_1", "Allowed_Out_Channel_2"};
        final SimsmeGuid existingSimsmeAccountGuid = new SimsmeGuid(0, UUID.randomUUID());
        final AccountCreation accountCreationWithSimsmeAccountRef = AccountCreation.newBuilder()
                .withName(accountName)
                .withMmaId(mmaId)
                .withAllowedOutChannels(allowedOutChannels)
                .withReferenceToExistingSimsmeAccount(existingSimsmeAccountGuid)
                .build();

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountCreationWithSimsmeAccountRef);
        assertNotNull(newAccount);

        final boolean accountHasBeenStored = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String storedAccountUuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertEquals(newAccount.uuid.toString(), storedAccountUuid);

        final String storedAccountName = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(newAccount.name, storedAccountName);

        final String storedAccountMmaId = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(newAccount.mmaId, Long.parseLong(storedAccountMmaId));

        final String[] storedAccountAllowedOutChannels = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(newAccount.allowedOutChannels, storedAccountAllowedOutChannels);

        final String storedAccountSimsmeAccountGuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(newAccount.simsmeAccountRef.simsmeGuid, SimsmeGuid.parse(storedAccountSimsmeAccountGuid));

        final String secondaryMmaIdIndex = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(newAccount.mmaId));
        assertEquals(newAccount.uuid.toString(), secondaryMmaIdIndex);
    }

    @Test
    public void create_account_should_store_account_with_reference_to_new_simsme_account() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 17866534288L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel_1", "Allowed_Out_Channel_2"};
        final String simsmeAccountName = "newSimsmeAccount";
        final String simsmeAccountImage = "new simsme account imgage";
        final AccountCreation accountCreationWithSimsmeAccountRef = AccountCreation.newBuilder()
                .withName(accountName)
                .withMmaId(mmaId)
                .withAllowedOutChannels(allowedOutChannels)
                .withReferenceToNewSimsmeAccount(simsmeAccountName, simsmeAccountImage)
                .build();

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountCreationWithSimsmeAccountRef);
        assertNotNull(newAccount);

        final boolean accountHasBeenStored = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String storedAccountUuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertEquals(newAccount.uuid.toString(), storedAccountUuid);

        final String storedAccountName = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(newAccount.name, storedAccountName);

        final String storedAccountMmaId = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(newAccount.mmaId, Long.parseLong(storedAccountMmaId));

        final String[] storedAccountAllowedOutChannels = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(newAccount.allowedOutChannels, storedAccountAllowedOutChannels);

        final String storedAccountSimsmeAccountGuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(newAccount.simsmeAccountRef.simsmeGuid, SimsmeGuid.parse(storedAccountSimsmeAccountGuid));

        final String secondaryMmaIdIndex = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(newAccount.mmaId));
        assertEquals(newAccount.uuid.toString(), secondaryMmaIdIndex);
    }
}
