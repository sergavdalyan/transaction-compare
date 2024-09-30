package com.paymentology.transactioncompare.service;

import com.paymentology.transactioncompare.dto.CsvComparisonResult;
import com.paymentology.transactioncompare.exception.CsvFileProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CsvReconciliationServiceTest {

    @InjectMocks
    private CsvReconciliationService csvReconciliationService;

    @Mock
    private MultipartFile mockFile1;

    @Mock
    private MultipartFile mockFile2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReconcileFiles_success() throws IOException {
        String file1Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n" +
                "Card Campaign,2023-09-28,-10000,Sample Narrative 1,Sample Description 1,ID123,1,WR123\n" +
                "Card Campaign,2023-09-28,-5000,Sample Narrative 2,Sample Description 2,ID124,1,WR124\n";
        String file2Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n" +
                "Card Campaign,2023-09-28,-10000,Sample Narrative 1,Sample Description 1,ID123,1,WR123\n" +
                "Card Campaign,2023-09-28,-7000,Sample Narrative 3,Sample Description 3,ID125,1,WR125\n";

        when(mockFile1.getInputStream()).thenReturn(new ByteArrayInputStream(file1Content.getBytes()));
        when(mockFile2.getInputStream()).thenReturn(new ByteArrayInputStream(file2Content.getBytes()));

        // Run the method to be tested
        CsvComparisonResult result = csvReconciliationService.reconcileFiles(mockFile1, mockFile2);

        assertNotNull(result);
        assertEquals(2, result.file1().totalRecords());
        assertEquals(2, result.file2().totalRecords());
        assertEquals(1, result.file1().matchingRecords());
        assertEquals(1, result.file1().unmatchedRecords());
        assertEquals(1, result.file2().unmatchedRecords());
    }


    @Test
    void testReconcileFiles_withException() throws IOException {
        when(mockFile1.getInputStream()).thenThrow(new IOException("Test IOException"));

        CsvFileProcessingException exception = assertThrows(CsvFileProcessingException.class, () -> {
            csvReconciliationService.reconcileFiles(mockFile1, mockFile2);
        });

        assertEquals("Error processing CSV files", exception.getMessage());
    }
}
