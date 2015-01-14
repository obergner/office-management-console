package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class NoneSimsmeAccountRefCreation extends SimsmeAccountRefModification {

    private static final NoneSimsmeAccountRefCreation INSTANCE = new NoneSimsmeAccountRefCreation();

    private static final long serialVersionUID = 8453541742422389850L;

    @JsonCreator
    public static NoneSimsmeAccountRefCreation instance() {
        return INSTANCE;
    }

    private NoneSimsmeAccountRefCreation() {
        super(Action.none);
    }

    @Override
    public String toString() {
        return "NoneSimsmeAccountRefCreation[]";
    }
}
