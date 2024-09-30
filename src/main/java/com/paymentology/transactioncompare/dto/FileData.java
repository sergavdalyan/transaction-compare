package com.paymentology.transactioncompare.dto;

import java.util.List;

public record FileData(
    int totalRecords,
    int matchingRecords,
    int unmatchedRecords,
    List<Transaction> unmatchedTransactions
) {}
