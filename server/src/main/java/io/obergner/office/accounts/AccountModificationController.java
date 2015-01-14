package io.obergner.office.accounts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.util.Assert.notNull;

@RestController
public class AccountModificationController {

    private final AccountManager accountManager;

    public AccountModificationController(final AccountManager accountManager) {
        notNull(accountManager, "Argument 'accountDao' must not be null");
        this.accountManager = accountManager;
    }

    @RequestMapping(value = "/accountcreations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> postAccountCreation(@RequestBody @Valid final AccountCreation accountCreation) {
        final Account createdAccount = this.accountManager.createAccount(accountCreation);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("/accounts/uuid/" + createdAccount.uuid.toString()));
        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/accountupdates", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> postAccountUpdate(@RequestBody @Valid final AccountUpdate accountUpdate) {
        final Account updatedAccount = this.accountManager.updateAccount(accountUpdate);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("/accounts/uuid/" + updatedAccount.uuid.toString()));
        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }
}
