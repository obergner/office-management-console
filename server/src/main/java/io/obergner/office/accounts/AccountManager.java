package io.obergner.office.accounts;

import java.util.List;
import java.util.UUID;

public interface AccountManager {

    Account accountByUuid(final UUID uuid);

    Account createAccount(final AccountCreation accountCreation);

    Account updateAccount(final AccountUpdate account);

    void deleteAccount(final UUID accountUuid);

    Account accountByMmaId(final long mmaId);

    List<Account> allAccounts();
}
