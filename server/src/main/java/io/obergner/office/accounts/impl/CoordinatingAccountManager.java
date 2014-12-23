package io.obergner.office.accounts.impl;

import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountCreation;
import io.obergner.office.accounts.AccountDao;
import io.obergner.office.accounts.AccountManager;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.ExistingSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManager;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

public class CoordinatingAccountManager implements AccountManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountDao accountDao;

    private final SimsmeAccountManager simsmeAccountManager;

    public CoordinatingAccountManager(final AccountDao accountDao, final SimsmeAccountManager simsmeAccountManager) {
        notNull(accountDao, "Argument 'accountDao' must not be null");
        notNull(simsmeAccountManager, "Argument 'simsmeAccountManager' must not be null");
        this.accountDao = accountDao;
        this.simsmeAccountManager = simsmeAccountManager;
    }

    @Override
    public Account accountByUuid(final UUID uuid) {
        notNull(uuid, "Argument 'uuid' must not be null");
        this.log.info("Looking up account by [uuid:{}] ...", uuid);
        final Account account = this.accountDao.accountByUuid(uuid);
        this.log.info("Successfully looked up {}", account);
        return account;
    }

    @Override
    public Account createAccount(final String name, final long mmaId, final String[] allowedOutChannels) {
        hasText(name, "Argument 'name' must neither be null nor blank");
        notEmpty(allowedOutChannels, "Argument 'allowedOutChannels' must not be empty");
        this.log.info("Creating account using [name:{}|mmaId:{}|allowedOutChannels:{}] ...", name, mmaId, allowedOutChannels);
        final Account account = this.accountDao.createAccount(name, mmaId, allowedOutChannels);
        this.log.info("Successfully created {}", account);
        return account;
    }

    @Override
    public Account createAccount(final Account account) {
        notNull(account, "Argument 'account' must not be null");
        this.log.info("Creating {} ...", account);
        final Account createdAccount = this.accountDao.createAccount(account);
        this.log.info("Successfully created {}", createdAccount);
        return createdAccount;
    }

    @Override
    public Account createAccount(final AccountCreation accountCreation) {
        notNull(accountCreation, "Argument 'accountCreation' must not be null");
        this.log.info("Creating account using {} ...", accountCreation);
        final SimsmeGuid simsmeAccountRef = createOrReferenceSimsmeSubaccountIfNecessary(accountCreation);
        final Account accountToStore = Account.newAccountWithReferenceToExistingSimsmeAccount(accountCreation.name, accountCreation.mmaId, accountCreation.allowedOutChannels, simsmeAccountRef);
        final Account createdAccount = this.accountDao.createAccount(accountToStore);
        this.log.info("Successfully created {}", createdAccount);
        return createdAccount;
    }

    private SimsmeGuid createOrReferenceSimsmeSubaccountIfNecessary(final AccountCreation accountCreation) {
        if (!accountCreation.createsSimsmeAccountRef()) {
            return null;
        }
        final SimsmeAccountRefCreation simsmeAccountRefCreation = accountCreation.simsmeAccountRefCreation;
        final SimsmeGuid simsmeAccountRef;
        switch (simsmeAccountRefCreation.action) {
            case referenceExisting:
                simsmeAccountRef = ExistingSimsmeAccountRefCreation.class.cast(simsmeAccountRefCreation).existingSimsmeGuid;
                break;
            case createNew:
                final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = CreateNewSimsmeAccountRefCreation.class.cast(simsmeAccountRefCreation);
                final String simsmeAccountName = StringUtils.hasText(createNewSimsmeAccountRefCreation.name) ? createNewSimsmeAccountRefCreation.name : accountCreation.name;
                simsmeAccountRef = this.simsmeAccountManager.createAccount(simsmeAccountName, createNewSimsmeAccountRefCreation.imageBase64Jpeg).guid;
                break;
            default:
                throw new IllegalArgumentException("Unsupported SIMSme account creation action type: " + simsmeAccountRefCreation.action);
        }
        return simsmeAccountRef;
    }

    @Override
    public Account updateAccount(final Account account) {
        notNull(account, "Argument 'account' must not be null");
        this.log.info("Updating {} ...", account);
        final Account updatedAccount = this.accountDao.updateAccount(account);
        this.log.info("Successfully updated {}", updatedAccount);
        return updatedAccount;
    }

    @Override
    public void deleteAccount(final UUID accountUuid) {
        notNull(accountUuid, "Argument 'accountUuid' must not be null");
        this.log.info("Deleting account [uuid:{}] ...", accountUuid);
        this.accountDao.deleteAccount(accountUuid);
        this.log.info("Successfully deleted account [uuid:{}]", accountUuid);
    }

    @Override
    public Account accountByMmaId(final long mmaId) {
        this.log.info("Looking up account by [mmaId:{}] ...", mmaId);
        final Account account = this.accountDao.accountByMmaId(mmaId);
        this.log.info("Successfully looked up {}", account);
        return account;
    }

    @Override
    public List<Account> allAccounts() {
        this.log.info("Looking up all accounts ...");
        final List<Account> allAccounts = this.accountDao.allAccounts();
        this.log.info("Successfully looked up [{}] accounts", allAccounts.size());
        return allAccounts;
    }
}
