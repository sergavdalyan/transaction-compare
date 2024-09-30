package com.paymentology.transactioncompare.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionReconciliationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testReconcileFiles_success() throws Exception {
        String file1Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n" +
                "Card Campaign,2023-09-28,-10000,Sample Narrative 1,Sample Description 1,ID123,1,WR123\n" +
                "Card Campaign,2023-09-28,-5000,Sample Narrative 2,Sample Description 2,ID124,1,WR124\n";

        String file2Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n" +
                "Card Campaign,2023-09-28,-10000,Sample Narrative 1,Sample Description 1,ID123,1,WR123\n" +
                "Card Campaign,2023-09-28,-7000,Sample Narrative 3,Sample Description 3,ID125,1,WR125\n";

        MockMultipartFile file1 = new MockMultipartFile("file1", "file1.csv", "text/csv", file1Content.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", file2Content.getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/reconcile/files")
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Files reconciled successfully"))
                .andExpect(jsonPath("$.data.file1.totalRecords").value(2))
                .andExpect(jsonPath("$.data.file2.totalRecords").value(2))
                .andExpect(jsonPath("$.data.file1.matchingRecords").value(1))
                .andExpect(jsonPath("$.data.file1.unmatchedRecords").value(1))
                .andExpect(jsonPath("$.data.file2.unmatchedRecords").value(1))
                .andReturn();
    }

    @Test
    void testReconcileFiles_withEmptyFiles() throws Exception {
        String file1Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n";
        String file2Content = "ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference\n";

        MockMultipartFile file1 = new MockMultipartFile("file1", "file1.csv", "text/csv", file1Content.getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", file2Content.getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/reconcile/files")
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.file1.totalRecords").value(0))
                .andExpect(jsonPath("$.data.file2.totalRecords").value(0))
                .andExpect(jsonPath("$.data.file1.matchingRecords").value(0))
                .andExpect(jsonPath("$.data.file1.unmatchedRecords").value(0))
                .andExpect(jsonPath("$.data.file2.unmatchedRecords").value(0));
    }

}
