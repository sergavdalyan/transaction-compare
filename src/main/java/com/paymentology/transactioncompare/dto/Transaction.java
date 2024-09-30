package com.paymentology.transactioncompare.dto;

public record Transaction(
        String profileName,
        String transactionDate,
        double transactionAmount,
        String transactionNarrative,
        String transactionDescription,
        String transactionID,
        int transactionType,
        String walletReference
) {}
