package io.obergner.office.accounts.redis;

import io.obergner.office.ApiErrorCode;
import io.obergner.office.accounts.Account;
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

public class RedisAccountManagerTest {

    private static final int PORT = 6379;

    @ClassRule
    public static final EmbeddedRedisServer EMBEDDED_REDIS_SERVER = EmbeddedRedisServer.listenOnPort(PORT);

    private static final RedisAccountManager OBJECT_UNDER_TEST = new RedisAccountManager("127.0.0.1", PORT);

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
    public void accountByUuidShouldReturnExistingAccount() throws Exception {
        final Account existingAccount = RedisTestAccounts.existingAccount();
        final Account storedAccount = OBJECT_UNDER_TEST.accountByUuid(existingAccount.uuid);
        assertEquals(existingAccount, storedAccount);
    }

    @Test
    public void accountByUuidShouldThrowExceptionWithProperCodeIfMatchingAccountDoesNotExist() throws Exception {
        try {
            OBJECT_UNDER_TEST.accountByUuid(UUID.randomUUID());
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void createAccountShouldStoreAccountInRedis() throws Exception {
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
    public void createAccountShouldThrowExceptionWithProperCodeIfDuplicateAccountUuid() throws Exception {
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
    public void createAccountShouldThrowExceptionWithProperCodeIfDuplicateMMA() throws Exception {
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
    public void updateAccountShouldUpdateAccountInRedis() throws Exception {
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
    public void updateAccountShouldCorrectlyUpdateMmaIndexIfMmaChanges() throws Exception {
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
    public void updateAccountShouldThrowExceptionWithProperCodeIfUpdatingNonExistingAccount() throws Exception {
        try {
            final Account updatedAccount = Account.newAccount("Updated name", 51234009873477L, RedisTestAccounts.ALL_ALLOWED_OUT_CHANNELS);

            OBJECT_UNDER_TEST.updateAccount(updatedAccount);
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void updateAccountShouldThrowExceptionWithProperCodeIfAnotherAccountHasSameMmaId() throws Exception {
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
    public void deleteAccountShouldRemoveMatchingAccountFromRedis() throws Exception {
        final Account existingAccount = RedisTestAccounts.existingAccount();
        OBJECT_UNDER_TEST.deleteAccount(existingAccount.uuid);

        final boolean accountStillStoredInRedis = EMBEDDED_REDIS_SERVER.client().hexists(AccountSchema.Keys.accountUuid(existingAccount.uuid), AccountSchema.Fields.UUID);
        assertFalse(accountStillStoredInRedis);

        final String oldMmaMapping = EMBEDDED_REDIS_SERVER.client().hget(AccountSchema.Keys.ACCOUNT_MMA_INDEX, String.valueOf(existingAccount.mmaId));
        assertNull(oldMmaMapping);
    }

    @Test
    public void deleteAccountShouldThrowExceptionWithProperCodeIdDeletingNonExistingAccount() throws Exception {
        try {
            OBJECT_UNDER_TEST.deleteAccount(UUID.randomUUID());
            fail("Expected JedisDataException to be thrown");
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_UUID_FOUND));
        }
    }

    @Test
    public void accountByMmaIdShouldReturnExistingAccount() throws Exception {
        final String accountName = this.testName.getMethodName();
        final long mmaId = 2347812399675L;
        final String[] allowedOutChannels = new String[]{"Allowed_Out_Channel"};

        final Account newAccount = OBJECT_UNDER_TEST.createAccount(accountName, mmaId, allowedOutChannels);

        final Account storedAccount = OBJECT_UNDER_TEST.accountByMmaId(mmaId);
        assertEquals(newAccount, storedAccount);
    }

    @Test
    public void accountByMmaIdShouldThrowExceptionWithProperCodeIfMatchingAccountDoesNotExist() throws Exception {
        try {
            final long mmaId = 73478123336721334L;
            OBJECT_UNDER_TEST.accountByMmaId(mmaId);
        } catch (final JedisDataException jde) {
            assertTrue(jde.getMessage().startsWith(ApiErrorCode.NO_ACCOUNT_WITH_MATCHING_MMAID_FOUND));
        }
    }

    @Test
    public void allAccountsShouldReturnAllAccountsStoredInRedis() throws Exception {
        final List<Account> storedAccounts = OBJECT_UNDER_TEST.allAccounts();
        assertEquals(RedisTestAccounts.ALL_ACCOUNTS.size(), storedAccounts.size());
    }
}
