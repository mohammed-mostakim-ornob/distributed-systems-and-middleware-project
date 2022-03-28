package de.uniba.dsg.beverage_store.spring_boot.exception;

public class InvalidOperationException extends Exception {
    public InvalidOperationException(String msg) {
        super(msg);
    }
}
