package io.obergner.office.test;

import io.obergner.office.accounts.Account;
import io.obergner.office.accounts.AccountManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RedisTestAccounts {

    public static final String[] ALL_ALLOWED_OUT_CHANNELS = new String[]{"USSD", "FlashSMS", "SIMSme"};

    public static final List<Account> ALL_ACCOUNTS = IntStream.range(0, 10)
            .mapToObj((idx) -> accountForIdx(idx))
            .collect(Collectors.toList());

    private static Account accountForIdx(final int idx) {
        return Account.newAccount("Existing Account " + idx, 100000L + idx, ALL_ALLOWED_OUT_CHANNELS);
    }

    public static Account existingAccount() {
        return ALL_ACCOUNTS.get(0);
    }

    public static void storeUsing(final AccountManager accountManager) {
        ALL_ACCOUNTS.forEach((account) -> accountManager.createAccount(account));
    }
}
