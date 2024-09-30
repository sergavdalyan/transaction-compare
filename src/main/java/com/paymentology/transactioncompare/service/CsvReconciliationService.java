package com.paymentology.transactioncompare.service;

import com.paymentology.transactioncompare.dto.CsvComparisonResult;
import com.paymentology.transactioncompare.dto.FileData;
import com.paymentology.transactioncompare.dto.Transaction;
import com.paymentology.transactioncompare.exception.CsvFileProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CsvReconciliationService {

    /**
     * Main method that orchestrates file reconciliation by loading file 1 into memory
     * and streaming file 2 to match against file 1.
     */
    public CsvComparisonResult reconcileFiles(MultipartFile file1, MultipartFile file2) {
        try {
            // Load transactions from file 1 into memory
            Map<String, List<Transaction>> file1TransactionMap = loadTransactions(file1);

            // Reconcile transactions between file 1 and file 2
            return reconcileWithFile2(file2, file1TransactionMap);
        } catch (IOException e) {
            throw new CsvFileProcessingException("Error processing CSV files", e);
        }
    }

    /**
     * Loads a CSV file into memory as a Map, where each TransactionID maps to a List of Transactions.
     */
    private Map<String, List<Transaction>> loadTransactions(MultipartFile file) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .map(this::mapToTransaction)
                    .collect(Collectors.groupingBy(Transaction::transactionID));
        }
    }

    /**
     * Reconcile transactions in file 2 against the transactions loaded in memory from file 1.
     */
    private CsvComparisonResult reconcileWithFile2(MultipartFile file2, Map<String, List<Transaction>> file1TransactionMap) throws IOException {
        List<Transaction> unmatchedInFile2 = new ArrayList<>();
        Map<String, List<Transaction>> unmatchedInFile1 = new HashMap<>(file1TransactionMap);
        int matchingCount = 0;

        // Stream file 2 and reconcile with file 1 transactions
        try (BufferedReader fileReader2 = new BufferedReader(new InputStreamReader(file2.getInputStream()));
             CSVParser csvParser2 = new CSVParser(fileReader2, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser2) {
                Transaction transaction2 = mapToTransaction(record);
                    List<Transaction> file1Transactions = unmatchedInFile1.get(transaction2.transactionID());

                if (file1Transactions != null && findAndRemoveMatchingTransaction(file1Transactions, transaction2)) {
                    matchingCount++;
                    if (file1Transactions.isEmpty()) {
                        unmatchedInFile1.remove(transaction2.transactionID()); // Remove if no transactions remain
                    }
                } else {
                    unmatchedInFile2.add(transaction2); // Add unmatched transaction from file 2
                }
            }
        }

        // Build and return reconciliation result
        return new CsvComparisonResult(
                new FileData(file1TransactionMap.size(), matchingCount, getTotalUnmatchedTransactions(unmatchedInFile1), flattenTransactions(unmatchedInFile1)),
                new FileData(file1TransactionMap.size(), matchingCount, unmatchedInFile2.size(), unmatchedInFile2)
        );
    }

    /**
     * Matches transaction2 with any transactions from file1 based on date and amount, and removes the matched one.
     */
    private boolean findAndRemoveMatchingTransaction(List<Transaction> file1Transactions, Transaction transaction2) {
        return file1Transactions.removeIf(transaction1 ->
                transaction1.transactionAmount() == transaction2.transactionAmount() &&
                        transaction1.transactionDate().equals(transaction2.transactionDate())
        );
    }

    /**
     * Maps a CSVRecord to a Transaction object.
     */
    private Transaction mapToTransaction(CSVRecord record) {
        try {
            return new Transaction(
                    record.get("ProfileName"),
                    record.get("TransactionDate"),
                    Double.parseDouble(record.get("TransactionAmount")),
                    record.get("TransactionNarrative"),
                    record.get("TransactionDescription"),
                    record.get("TransactionID"),
                    Integer.parseInt(record.get("TransactionType")),
                    record.get("WalletReference")
            );
        } catch (Exception e) {
            throw new CsvFileProcessingException("Error mapping CSV record to Transaction", e);
        }
    }

    /**
     * Helper method to calculate the total number of unmatched transactions from the map.
     */
    private int getTotalUnmatchedTransactions(Map<String, List<Transaction>> unmatchedInFile1) {
        return unmatchedInFile1.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Helper method to flatten the list of unmatched transactions from file 1.
     */
    private List<Transaction> flattenTransactions(Map<String, List<Transaction>> unmatchedInFile1) {
        return unmatchedInFile1.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
