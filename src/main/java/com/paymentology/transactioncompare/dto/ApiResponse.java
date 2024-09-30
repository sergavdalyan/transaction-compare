package com.paymentology.transactioncompare.dto;

public record ApiResponse<T>(String status, String message, T data) {}
