package io.obergner.office.accounts.subaccounts.simsme;

import io.obergner.office.accounts.subaccounts.simsme.impl.DummySimsmeAccountManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimsmeAccountManagementConfiguration {

    @Bean
    public SimsmeAccountManager simsmeAccountManager() {
        return new DummySimsmeAccountManager();
    }
}
