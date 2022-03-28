package de.uniba.dsg.beverage_store.spring_boot.exception;

public class CredentialConflictException extends Exception {
    public CredentialConflictException(String msg) {
        super(msg);
    }
}
