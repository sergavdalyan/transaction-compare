package com.paymentology.transactioncompare.exception.handler;

import com.paymentology.transactioncompare.dto.ApiResponse;
import com.paymentology.transactioncompare.exception.CsvFileProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvFileProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleCsvFileProcessingException(CsvFileProcessingException ex) {
        ApiResponse<String> response = new ApiResponse<>("error", ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
