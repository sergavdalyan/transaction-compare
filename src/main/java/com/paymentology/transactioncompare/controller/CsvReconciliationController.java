package com.paymentology.transactioncompare.controller;

import com.paymentology.transactioncompare.dto.ApiResponse;
import com.paymentology.transactioncompare.dto.CsvComparisonResult;
import com.paymentology.transactioncompare.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reconcile")
public class CsvReconciliationController {

    private final CsvReconciliationService csvReconciliationService;

    @Autowired
    public CsvReconciliationController(CsvReconciliationService csvReconciliationService) {
        this.csvReconciliationService = csvReconciliationService;
    }

    @PostMapping("/files")
    public ResponseEntity<ApiResponse<CsvComparisonResult>> reconcileCsvFiles(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2) {
        CsvComparisonResult result = csvReconciliationService.reconcileFiles(file1, file2);
        ApiResponse<CsvComparisonResult> response = new ApiResponse<>("success", "Files reconciled successfully", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
