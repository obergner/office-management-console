package io.obergner.office.accounts.subaccounts.simsme.impl;

import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccount;
import io.obergner.office.accounts.subaccounts.simsme.SimsmeAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.util.Assert.hasText;

public class DummySimsmeAccountManager implements SimsmeAccountManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public SimsmeAccount createAccount(final String name, final String imageBase64Jpeg) {
        hasText(name, "Argument 'name' must neither be null nor blank");
        this.log.info("Creating new SIMSme account using [name:{}|image:{} (base64/jpeg)] ...", name, imageBase64Jpeg);
        final SimsmeAccount result = new SimsmeAccount(name, imageBase64Jpeg);
        this.log.info("Created new SIMSme account {}", result);
        return result;
    }
}
