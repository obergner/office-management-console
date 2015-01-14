package io.obergner.office.accounts.impl;

import io.obergner.office.accounts.AbstractAccountModification;
import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountCreation;
import io.obergner.office.accounts.AccountDao;
import io.obergner.office.accounts.AccountManager;
import io.obergner.office.accounts.AccountUpdate;
import io.obergner.office.accounts.subaccounts.simsme.CreateNewSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.ExistingSimsmeAccountRefCreation;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManager;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountRefModification;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

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
    public Account createAccount(final AccountCreation accountCreation) {
        notNull(accountCreation, "Argument 'accountCreation' must not be null");
        this.log.info("Creating account using {} ...", accountCreation);
        final SimsmeGuid simsmeAccountRef = modifySimsmeSubaccountIfNecessary(accountCreation);
        final Account accountToStore = Account.newAccountWithReferenceToExistingSimsmeAccount(accountCreation.name, accountCreation.mmaId, accountCreation.allowedOutChannels, simsmeAccountRef);
        final Account createdAccount = this.accountDao.createAccount(accountToStore);
        this.log.info("Successfully created {}", createdAccount);
        return createdAccount;
    }

    private SimsmeGuid modifySimsmeSubaccountIfNecessary(final AbstractAccountModification accountModification) {
        if (accountModification.simsmeAccountRefModification == null) {
            return null;
        }
        final SimsmeAccountRefModification simsmeAccountRefModification = accountModification.simsmeAccountRefModification;
        final SimsmeGuid simsmeAccountRef;
        switch (simsmeAccountRefModification.action) {
            case none:
            case deleteReference:
                simsmeAccountRef = null;
                break;
            case referenceExisting:
                simsmeAccountRef = ExistingSimsmeAccountRefCreation.class.cast(simsmeAccountRefModification).existingSimsmeGuid;
                break;
            case createNew:
                final CreateNewSimsmeAccountRefCreation createNewSimsmeAccountRefCreation = CreateNewSimsmeAccountRefCreation.class.cast(simsmeAccountRefModification);
                final String simsmeAccountName = StringUtils.hasText(createNewSimsmeAccountRefCreation.name) ? createNewSimsmeAccountRefCreation.name : accountModification.name;
                simsmeAccountRef = this.simsmeAccountManager.createAccount(simsmeAccountName, createNewSimsmeAccountRefCreation.imageBase64Jpeg).guid;
                break;
            default:
                throw new IllegalArgumentException("Unsupported SIMSme account creation action type: " + simsmeAccountRefModification.action);
        }
        return simsmeAccountRef;
    }

    @Override
    public Account updateAccount(final AccountUpdate accountUpdate) {
        notNull(accountUpdate, "Argument 'accountUpdate' must not be null");
        this.log.info("Updating account using {} ...", accountUpdate);
        final SimsmeGuid simsmeAccountRef = modifySimsmeSubaccountIfNecessary(accountUpdate);
        final Account accountToStore = new Account(accountUpdate.uuid, accountUpdate.name, accountUpdate.mmaId, 0L, accountUpdate.allowedOutChannels, simsmeAccountRef);
        final Account updatedAccount = this.accountDao.updateAccount(accountToStore);
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
