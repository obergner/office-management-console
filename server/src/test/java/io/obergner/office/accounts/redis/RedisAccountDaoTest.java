package io.obergner.office.accounts.redis;

import io.obergner.office.ApiErrorCode;
import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
import io.obergner.office.test.EmbeddedRedisServer;
import io.obergner.office.test.RedisTestAccounts;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RedisAccountDaoTest {

    private static final int PORT = 6379;

    @ClassRule
    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnPort(PORT);

    private static final RedisAccountDao OBJECT_UNDER_TEST = new RedisAccountDao("127.0.0.1", PORT);

    @BeforeClass
    public static void initializeObjectUnderTest() throws IOException {
        OBJECT_UNDER_TEST.initialize();
    }

    @AfterClass
    public static void destroyObjectUnderTest() {
        OBJECT_UNDER_TEST.destroy();
    }

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void prepareRedis() {
        EMBEDDED_REDIS_SERVER.client().flushDB();
        RedisTestAccounts.storeUsing(OBJECT_UNDER_TEST);
    }

    @Test
    public void account_by_uuid_should_return_existing_account_without_simsme_acccount_ref() throws Exception {
        final Account existingAccount = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();
        final Account storedAccount = OBJECT_UNDER_TEST.accountByUuid(existingAccount.uuid);
        assertEquals(existingAccount, storedAccount);
    }

    @Test
    public void account_by_uuid_should_return_existing_account_with_simsme_account_ref() throws Exception {
        final Account existingAccount = RedisTestAccounts.existingAccountWithSimsmeAccountRef();
        final Account storedAccount = OBJECT_UNDER_TEST.accountByUuid(existingAccount.uuid);
        assertEquals(existingAccount, storedAccount);
    }

    @Test
    public void account_by_uuid_should_throw_exception_with_proper_code_if_matching_account_does_not_exist() throws Exception {
        try {
            OBJECT_UNDER_TEST.accountByUuid(UUID.randomUUID());
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void create_account_should_store_account_in_redis() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 17866534277L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel_1", "Allowed_Out_Channel_2"};

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountName, mmaId, allowedOutChannels);
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
    public void create_account_should_store_reference_to_existing_simsme_account_in_redis() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 17866534277L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel_1", "Allowed_Out_Channel_2"};
        final SimsmeGuid simsmeGuid = new SimsmeGuid(0, UUID.randomUUID());

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountName, mmaId, allowedOutChannels, simsmeGuid);
        assertNotNull(newAccount);

        final boolean accountHasBeenStored = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.UUID);
        assertTrue(accountHasBeenStored);

        final String storedSimsmeGuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(newAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(newAccount.simsmeAccountRef.simsmeGuid, SimsmeGuid.parse(storedSimsmeGuid));
    }

    @Test
    public void create_account_should_throw_exception_with_proper_code_if_duplicate_account_uuid() throws Exception {
        try {
            final UUID duplicateUuid = UUID.randomUUID();

            final String firstAccountName = this.testName.getMethodName() + "_1";
            final long firstMmaId = 4566789L;
            final String[] firstAllowedOutChannels = new String[]{"first_channel", "second_channel"};
            final Account firstAccount = new Account(duplicateUuid, firstAccountName, firstMmaId, System.currentTimeMillis(), firstAllowedOutChannels);

            final String secondAccountName = this.testName.getMethodName() + "_2";
            final long secondMmaId = 78234567L;
            final String[] secondAllowedOutChannels = new String[]{"a_channel", "b_channel"};
            final Account secondAccount = new Account(duplicateUuid, secondAccountName, secondMmaId, System.currentTimeMillis(), secondAllowedOutChannels);

            OBJECT_UNDER_TEST.createAccount(firstAccount);
            OBJECT_UNDER_TEST.createAccount(secondAccount);
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.DUPLICATE_ACCOUNT_UUID));
        }
    }

    @Test
    public void create_account_should_throw_exception_with_proper_code_if_duplicate_mma() throws Exception {
        try {
            final String firstAccountName = this.testName.getMethodName() + "_1";
            final String secondAccountName = this.testName.getMethodName() + "_2";
            final long duplicateMmaId = 17866534277L;

            OBJECT_UNDER_TEST.createAccount(firstAccountName, duplicateMmaId, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);
            OBJECT_UNDER_TEST.createAccount(secondAccountName, duplicateMmaId, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.DUPLICATE_ACCOUNT_MMAID));
        }
    }

    @Test
    public void update_account_should_update_account_in_redis() throws Exception {
        final String originalAccountName = this.testName.getMethodName();
        final long originalMmaId = 4671234786554L;
        final String[] originalAllowedOutChannels = new String[]{"An_Out_Channel", "Another_Out_Channel"};

        final Account originalAccount = OBJECT_UNDER_TEST.createAccount(originalAccountName, originalMmaId, originalAllowedOutChannels);

        final String[] updatedAllowedOutChannels = new String[]{"An_Updated_Out_Channel", "Another_Updated_Out_Channel"};
        final Account updatedAccount = new Account(originalAccount.uuid, "Updated name", originalAccount.mmaId, originalAccount.createdAt, updatedAllowedOutChannels);

        OBJECT_UNDER_TEST.updateAccount(updatedAccount);

        final String updatedAccountUuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.UUID);
        assertEquals(updatedAccount.uuid.toString(), updatedAccountUuid);

        final String updatedAccountName = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(updatedAccount.name, updatedAccountName);

        final String updatedAccountMmaId = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(updatedAccount.mmaId, Long.parseLong(updatedAccountMmaId));

        final String[] updatedAccountAllowedOutChannels = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(updatedAccount.allowedOutChannels, updatedAccountAllowedOutChannels);

        final String secondaryMmaIdIndex = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(updatedAccount.mmaId));
        assertEquals(updatedAccount.uuid.toString(), secondaryMmaIdIndex);
    }

    @Test
    public void update_account_should_correctly_update_mma_index_if_mma_changes() throws Exception {
        final String originalAccountName = this.testName.getMethodName();
        final long originalMmaId = 779561271234786554L;
        final String[] originalAllowedOutChannels = new String[]{"An_Out_Channel"};

        final Account originalAccount = OBJECT_UNDER_TEST.createAccount(originalAccountName, originalMmaId, originalAllowedOutChannels);

        final Account updatedAccount = new Account(originalAccount.uuid, "Updated name", 51234009873425L, originalAccount.createdAt, originalAccount.allowedOutChannels);

        OBJECT_UNDER_TEST.updateAccount(updatedAccount);

        final String updatedAccountUuid = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.UUID);
        assertEquals(updatedAccount.uuid.toString(), updatedAccountUuid);

        final String updatedAccountName = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.NAME);
        assertEquals(updatedAccount.name, updatedAccountName);

        final String updatedAccountMmaId = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.MMA_ID);
        assertEquals(updatedAccount.mmaId, Long.parseLong(updatedAccountMmaId));

        final String[] updatedAccountAllowedOutChannels = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.ALLOWED_OUT_CHANNELS).split(",");
        assertArrayEquals(updatedAccount.allowedOutChannels, updatedAccountAllowedOutChannels);

        final String secondaryMmaIdIndex = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(updatedAccount.mmaId));
        assertEquals(updatedAccount.uuid.toString(), secondaryMmaIdIndex);

        final String oldMmaMapping = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(originalAccount.mmaId));
        assertNull(oldMmaMapping);
    }

    @Test
    public void update_account_should_add_reference_to_simsme_account() throws Exception {
        final SimsmeGuid simsmeGuidToAdd = new SimsmeGuid(0, UUID.randomUUID());

        final Account originalAccount = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();

        final Account updatedAccount = new Account(originalAccount.uuid, originalAccount.name, 51234009873425L, originalAccount.createdAt, originalAccount.allowedOutChannels, simsmeGuidToAdd);

        OBJECT_UNDER_TEST.updateAccount(updatedAccount);

        final String updatedAccountSimsmeRef = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(updatedAccount.simsmeAccountRef.simsmeGuid, SimsmeGuid.parse(updatedAccountSimsmeRef));
    }

    @Test
    public void update_account_should_update_reference_to_simsme_account() throws Exception {
        final SimsmeGuid updatedSimsmeGuid = new SimsmeGuid(0, UUID.randomUUID());

        final Account originalAccount = RedisTestAccounts.existingAccountWithSimsmeAccountRef();

        final Account updatedAccount = new Account(originalAccount.uuid, originalAccount.name, 51234009873425L, originalAccount.createdAt, originalAccount.allowedOutChannels, updatedSimsmeGuid);

        OBJECT_UNDER_TEST.updateAccount(updatedAccount);

        final String updatedAccountSimsmeRef = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID);
        assertEquals(updatedAccount.simsmeAccountRef.simsmeGuid, SimsmeGuid.parse(updatedAccountSimsmeRef));
    }

    @Test
    public void update_account_should_delete_reference_to_simsme_account() throws Exception {
        final Account originalAccount = RedisTestAccounts.existingAccountWithSimsmeAccountRef();

        final Account updatedAccount = new Account(originalAccount.uuid, originalAccount.name, 51234009873425L, originalAccount.createdAt, originalAccount.allowedOutChannels, (SimsmeGuid) null);

        OBJECT_UNDER_TEST.updateAccount(updatedAccount);

        assertFalse(EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(updatedAccount.uuid), AccountSchema.Fields.SIMSME_ACCOUNT_GUID));
    }

    @Test
    public void update_account_should_throw_exception_with_proper_code_if_updating_non_existing_account() throws Exception {
        try {
            final Account updatedAccount = Account.newAccount("Updated name", 51234009873477L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

            OBJECT_UNDER_TEST.updateAccount(updatedAccount);
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void update_account_should_throw_exception_with_proper_code_if_another_account_has_same_mma_id() throws Exception {
        try {
            final long mmaUsedByAnotherAccount = 8916652434985633L;

            final String anotherAccountName = this.testName.getMethodName() + "_ANOTHER";
            final Account anotherAccount = Account.newAccount(anotherAccountName, mmaUsedByAnotherAccount, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);
            OBJECT_UNDER_TEST.createAccount(anotherAccount);

            final Account accountToUpdate = Account.newAccount("Account to update", 51234009873477L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);
            OBJECT_UNDER_TEST.createAccount(accountToUpdate);

            final Account updatedAccount = new Account(accountToUpdate.uuid, "Updated name", anotherAccount.mmaId, accountToUpdate.createdAt, accountToUpdate.allowedOutChannels);
            OBJECT_UNDER_TEST.updateAccount(updatedAccount);
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.DUPLICATE_ACCOUNT_MMAID));
        }
    }

    @Test
    public void delete_account_should_remove_matching_account_from_redis() throws Exception {
        final Account existingAccount = RedisTestAccounts.existingAccountWithoutSimsmeAccountRef();
        OBJECT_UNDER_TEST.deleteAccount(existingAccount.uuid);

        final boolean accountStillStoredInRedis = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(existingAccount.uuid), AccountSchema.Fields.UUID);
        assertFalse(accountStillStoredInRedis);

        final String oldMmaMapping = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(existingAccount.mmaId));
        assertNull(oldMmaMapping);
    }

    @Test
    public void delete_account_should_throw_exception_with_proper_code_id_deleting_non_existing_account() throws Exception {
        try {
            OBJECT_UNDER_TEST.deleteAccount(UUID.randomUUID());
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void account_by_mma_id_should_return_existing_account() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 2347812399675L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel"};

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountName, mmaId, allowedOutChannels);

        final Account storedAccount = OBJECT_UNDER_TEST.accountByMmaId(mmaId);
        assertEquals(newAccount, storedAccount);
    }

    @Test
    public void account_by_mma_id_should_throw_exception_with_proper_code_if_matching_account_does_not_exist() throws Exception {
        try {
            final long mmaId = 73478123336721334L;
            OBJECT_UNDER_TEST.accountByMmaId(mmaId);
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_MMAID_FOUND));
        }
    }

    @Test
    public void all_accounts_should_return_all_accounts_stored_in_redis() throws Exception {
        final List<Account> storedAccounts = OBJECT_UNDER_TEST.allAccounts();
        assertEquals(RedisTestAccounts.ALL_ACCOUNTS.size(), storedAccounts.size());
    }
}
