package io.obergner.office.accounts;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountManager accountManager;

    public AccountController(final AccountManager accountManager) {
        notNull(accountManager, "Argument 'accountDao' must not be null");
        this.accountManager = accountManager;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Account> index() {
        return this.accountManager.allAccounts();
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteAccount(@PathVariable("uuid") final UUID uuid) {
        this.accountManager.deleteAccount(uuid);
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Account accountByUuid(@PathVariable("uuid") final UUID uuid) {
        return this.accountManager.accountByUuid(uuid);
    }

    @RequestMapping(value = "/mma/{mma}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Account accountByMmaId(@PathVariable("mma") final long mma) {
        return this.accountManager.accountByMmaId(mma);
    }
}
