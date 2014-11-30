package io.obergner.office.accounts;

import java.util.List;
import java.util.UUID;

public interface AccountManager {

    Account accountByUuid(final UUID uuid);

    Account createAccount(final String name,
                          final long mmaId,
                          final String[] allowedOutChannels);

    Account createAccount(final Account account);

    Account updateAccount(final Account account);

    void deleteAccount(final UUID accountUuid);

    Account accountByMmaId(final long mmaId);

    List<Account> allAccounts();
}
