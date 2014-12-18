package io.obergner.office.test;

import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountDao;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RedisTestAccounts {

    public static final String[] ALL_ALLOWED_OUT_CHANNELS = new String[]{"USSD", "FlashSMS", "SIMSme"};

    public static final List<Account> ACCOUNTS_WITHOUT_SIMSME_ACCOUNT_REF = IntStream.range(0, 10)
            .mapToObj(RedisTestAccounts::accountWithoutSimsmeAccountRefForIdx)
            .collect(Collectors.toList());

    private static Account accountWithoutSimsmeAccountRefForIdx(final int idx) {
        return Account.newAccount("Existing Account " + idx, 100000L + idx, ALL_ALLOWED_OUT_CHANNELS);
    }

    public static Account existingAccountWithoutSimsmeAccountRef() {
        return ACCOUNTS_WITHOUT_SIMSME_ACCOUNT_REF.get(0);
    }

    public static final List<Account> ACCOUNTS_WITH_SIMSME_ACCOUNT_REF = IntStream.range(0, 10)
            .mapToObj(RedisTestAccounts::accountWithSimsmeAccountRefForIdx)
            .collect(Collectors.toList());

    private static Account accountWithSimsmeAccountRefForIdx(final int idx) {
        return Account.newAccountWithReferenceToExistingSimsmeAccount("Existing Account with SIMSme Account ref " + idx, 200000L + idx, ALL_ALLOWED_OUT_CHANNELS, new SimsmeGuid(0, UUID.randomUUID()));
    }

    public static Account existingAccountWithSimsmeAccountRef() {
        return ACCOUNTS_WITH_SIMSME_ACCOUNT_REF.get(0);
    }

    public static final List<Account> ALL_ACCOUNTS;

    static {
        final List<Account> tmp = new ArrayList<>(20);
        tmp.addAll(ACCOUNTS_WITHOUT_SIMSME_ACCOUNT_REF);
        tmp.addAll(ACCOUNTS_WITH_SIMSME_ACCOUNT_REF);
        ALL_ACCOUNTS = Collections.unmodifiableList(tmp);
    }

    public static void storeUsing(final AccountDao accountDao) {
        ALL_ACCOUNTS.forEach(accountDao::createAccount);
    }
}
