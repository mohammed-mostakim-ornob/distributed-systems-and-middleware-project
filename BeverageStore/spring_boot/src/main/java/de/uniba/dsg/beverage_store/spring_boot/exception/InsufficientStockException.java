package de.uniba.dsg.beverage_store.spring_boot.exception;

public class InsufficientStockException extends Exception {
    public InsufficientStockException(String msg) {
        super(msg);
    }
}
