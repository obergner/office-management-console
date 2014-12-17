package io.obergner.office.accounts.subaccounts.simsme;

public interface SimsmeAccountManager {

    SimsmeAccount createAccount(final String name, final String imageBase64Jpeg);
}
