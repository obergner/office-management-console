package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class CreateNewSimsmeAccountRefCreation extends SimsmeAccountRefCreation {

    @JsonCreator
    public CreateNewSimsmeAccountRefCreation() {
        super(Action.createNew);
    }

    @Override
    public String toString() {
        return "CreateNewSimsmeAccountRefCreation[]";
    }
}
