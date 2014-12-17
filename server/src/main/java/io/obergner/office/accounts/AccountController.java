package io.obergner.office.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountManager accountManager;

    @Autowired
    public AccountController(final AccountManager accountManager) {
        notNull(accountManager, "Argument 'accountManager' must not be null");
        this.accountManager = accountManager;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Account> index() {
        return this.accountManager.allAccounts();
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@RequestBody @Valid final CreateAccount newAccount) {
        return this.accountManager.createAccount(newAccount.name, newAccount.mmaId, newAccount.allowedOutChannels);
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Account updateAccount(@PathVariable("uuid") final UUID uuid,
                                 @RequestBody @Valid final Account accountToUpdate) {
        return this.accountManager.updateAccount(accountToUpdate);
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
