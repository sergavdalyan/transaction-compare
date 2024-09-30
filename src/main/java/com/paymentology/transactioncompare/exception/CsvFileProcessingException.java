package com.paymentology.transactioncompare.exception;

public class CsvFileProcessingException extends RuntimeException {
    public CsvFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}