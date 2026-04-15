package com.paybridge.recon.settlement;

public class SettlementImportException extends RuntimeException {

    public SettlementImportException(String message) {
        super(message);
    }

    public SettlementImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
