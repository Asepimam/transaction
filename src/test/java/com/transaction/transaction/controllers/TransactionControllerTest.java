package com.transaction.transaction.controllers;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.transaction.transaction.dto.ResponseHistoryTransactionDto;
import com.transaction.transaction.dto.ResponseTransactionDTO;
import com.transaction.transaction.exceptions.GlobalExceptionHandler;
import com.transaction.transaction.services.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // tests successful get history transaction
    @Test
    void testGetHistoryTransactionSuccess() throws Exception {
        // Arrange
        Long accountId = 100L;
        int page = 0;
        int size = 10;

        ResponseTransactionDTO transaction1 = new ResponseTransactionDTO();
        transaction1.setId(1L);
        transaction1.setAccountId(accountId);
        transaction1.setAmount(500.0);
        transaction1.setType("debit");
        transaction1.setCategory("withdrawal");
        transaction1.setStatus("completed");
        transaction1.setDescription("ATM Withdrawal");
        transaction1.setDate(LocalDateTime.of(2025, 12, 10, 10, 0));

        ResponseTransactionDTO transaction2 = new ResponseTransactionDTO();
        transaction2.setId(2L);
        transaction2.setAccountId(accountId);
        transaction2.setAmount(1000.0);
        transaction2.setType("credit");
        transaction2.setCategory("transfer");
        transaction2.setStatus("completed");
        transaction2.setDescription("Transfer received");
        transaction2.setDate(LocalDateTime.of(2025, 12, 9, 15, 30));

        List<ResponseTransactionDTO> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(transactions);

        when(transactionService.getHistoryTransaction(accountId, page, size))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].id").value(1L))
                .andExpect(jsonPath("$.transactions[0].type").value("debit"))
                .andExpect(jsonPath("$.transactions[1].id").value(2L))
                .andExpect(jsonPath("$.transactions[1].type").value("credit"));
    }

    // test get history transaction with default pagination
    @Test
    void testGetHistoryTransactionWithDefaultPagination() throws Exception {
        // Arrange
        Long accountId = 200L;
        int defaultPage = 0;
        int defaultSize = 10;

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(new ArrayList<>());

        when(transactionService.getHistoryTransaction(accountId, defaultPage, defaultSize))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.transactions").isArray());
    }
    
    // tests get history transaction with empty result
    @Test
    void testGetHistoryTransactionWithEmptyResult() throws Exception {
        // Arrange
        Long accountId = 300L;
        int page = 0;
        int size = 10;

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(new ArrayList<>());

        when(transactionService.getHistoryTransaction(accountId, page, size))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(0));
    }

    // test get history transaction with custom pagination
    @Test
    void testGetHistoryTransactionWithCustomPagination() throws Exception {
        // Arrange
        Long accountId = 100L;
        int page = 2;
        int size = 5;

        ResponseTransactionDTO transaction = new ResponseTransactionDTO();
        transaction.setId(10L);
        transaction.setAccountId(accountId);
        transaction.setAmount(250.0);
        transaction.setType("debit");

        List<ResponseTransactionDTO> transactions = new ArrayList<>();
        transactions.add(transaction);

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(transactions);

        when(transactionService.getHistoryTransaction(accountId, page, size))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.transactions.length()").value(1));
    }

    // test get history transaction with invalid account id
    @Test
    void testGetHistoryTransactionWithInvalidAccountId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", "invalid"))
                .andExpect(status().isBadRequest());
    }

    // test get history transaction without account id
    @Test
    void testGetHistoryTransactionWithoutAccountId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isBadRequest());
    }

    // test get history transaction verifies response structure
    @Test
    void testGetHistoryTransactionVerifyResponseStructure() throws Exception {
        // Arrange
        Long accountId = 100L;

        ResponseTransactionDTO transaction = new ResponseTransactionDTO();
        transaction.setId(1L);
        transaction.setAccountId(accountId);
        transaction.setAmount(500.0);
        transaction.setType("debit");
        transaction.setCategory("withdrawal");
        transaction.setStatus("success");
        transaction.setDescription("Test transaction");
        transaction.setTransferId(10L);
        transaction.setDate(LocalDateTime.now());

        List<ResponseTransactionDTO> transactions = new ArrayList<>();
        transactions.add(transaction);

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(transactions);

        when(transactionService.getHistoryTransaction(accountId, 0, 10))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").exists())
                .andExpect(jsonPath("$.transactions").exists())
                .andExpect(jsonPath("$.transactions[0].id").exists())
                .andExpect(jsonPath("$.transactions[0].account_id").exists())
                .andExpect(jsonPath("$.transactions[0].amount").exists())
                .andExpect(jsonPath("$.transactions[0].type").exists())
                .andExpect(jsonPath("$.transactions[0].category").exists())
                .andExpect(jsonPath("$.transactions[0].status").exists());
    }

    // test get history transaction with large page number
    @Test
    void testGetHistoryTransactionWithLargePage() throws Exception {
        // Arrange
        Long accountId = 100L;
        int page = 100;
        int size = 10;

        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(new ArrayList<>());

        when(transactionService.getHistoryTransaction(accountId, page, size))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("account_id", accountId.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray());
    }
}
