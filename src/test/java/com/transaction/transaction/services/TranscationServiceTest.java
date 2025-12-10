package com.transaction.transaction.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.transaction.transaction.dto.ResponseHistoryTransactionDto;
import com.transaction.transaction.dto.ResponseTransactionDTO;
import com.transaction.transaction.entities.Transaction;
import com.transaction.transaction.mappers.TransactionMapper;
import com.transaction.transaction.repositories.TransactionRepository;
import com.transaction.transaction.services.impl.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction1;
    private Transaction transaction2;
    private ResponseTransactionDTO responseTransactionDTO1;
    private ResponseTransactionDTO responseTransactionDTO2;

    @BeforeEach
    void setUp() {
        // Setup transaction 1
        transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAccountId(100L);
        transaction1.setAmount(500.0);
        transaction1.setType("debit");
        transaction1.setTransferId(null);
        transaction1.setCategory("withdrawal");
        transaction1.setStatus("completed");
        transaction1.setDescription("ATM Withdrawal");
        transaction1.setDate(LocalDateTime.of(2025, 12, 10, 10, 0));

        // Setup transaction 2
        transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAccountId(100L);
        transaction2.setAmount(1000.0);
        transaction2.setType("credit");
        transaction2.setTransferId(5L);
        transaction2.setCategory("transfer");
        transaction2.setStatus("completed");
        transaction2.setDescription("Transfer from friend");
        transaction2.setDate(LocalDateTime.of(2025, 12, 9, 15, 30));

        // Setup response DTO 1
        responseTransactionDTO1 = new ResponseTransactionDTO();
        responseTransactionDTO1.setId(1L);
        responseTransactionDTO1.setAccountId(100L);
        responseTransactionDTO1.setAmount(500.0);
        responseTransactionDTO1.setType("debit");
        responseTransactionDTO1.setTransferId(null);
        responseTransactionDTO1.setCategory("withdrawal");
        responseTransactionDTO1.setStatus("completed");
        responseTransactionDTO1.setDescription("ATM Withdrawal");
        responseTransactionDTO1.setDate(LocalDateTime.of(2025, 12, 10, 10, 0));

        // Setup response DTO 2
        responseTransactionDTO2 = new ResponseTransactionDTO();
        responseTransactionDTO2.setId(2L);
        responseTransactionDTO2.setAccountId(100L);
        responseTransactionDTO2.setAmount(1000.0);
        responseTransactionDTO2.setType("credit");
        responseTransactionDTO2.setTransferId(5L);
        responseTransactionDTO2.setCategory("transfer");
        responseTransactionDTO2.setStatus("completed");
        responseTransactionDTO2.setDescription("Transfer from friend");
        responseTransactionDTO2.setDate(LocalDateTime.of(2025, 12, 9, 15, 30));
    }

    // mvn test -Dtest=TransactionServiceTest#testGetHistoryTransactionSuccess
    // Test: retrieve transaction history for account with multiple transactions
    // Expected: All transactions returned in correct format and order
    @Test
    void testGetHistoryTransactionSuccess() {
        // Arrange
        Long accountId = 100L;
        int page = 0;
        int size = 10;

        Page<Transaction> transactionPage = new PageImpl<>(
            List.of(transaction1, transaction2)
        );

        when(transactionRepository.findByAccountId(eq(accountId), any(Pageable.class)))
            .thenReturn(transactionPage);
        when(transactionMapper.toResponseTransactionDTO(transaction1))
            .thenReturn(responseTransactionDTO1);
        when(transactionMapper.toResponseTransactionDTO(transaction2))
            .thenReturn(responseTransactionDTO2);

        // Act
        ResponseHistoryTransactionDto result = transactionService.getHistoryTransaction(accountId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(2, result.getTransactions().size());
        assertEquals(1L, result.getTransactions().get(0).getId());
        assertEquals(2L, result.getTransactions().get(1).getId());
        assertEquals("debit", result.getTransactions().get(0).getType());
        assertEquals("credit", result.getTransactions().get(1).getType());
    }

    // mvn test -Dtest=TransactionServiceTest#testGetHistoryTransactionWithEmptyResult
    // Test: retrieve transaction history for account with no transactions
    // Expected: Empty transaction list returned for the account
    @Test
    void testGetHistoryTransactionWithEmptyResult() {
        // Arrange
        Long accountId = 200L;
        int page = 0;
        int size = 10;

        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(transactionRepository.findByAccountId(eq(accountId), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act
        ResponseHistoryTransactionDto result = transactionService.getHistoryTransaction(accountId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(0, result.getTransactions().size());
    }

    // mvn test -Dtest=TransactionServiceTest#testGetHistoryTransactionWithPagination
    // Test: retrieve transaction history with different pagination parameters
    // Expected: Correct pagination applied (page 0-2, size 5-20)
    @ParameterizedTest(name = "Pagination: page={0}, size={1}")
    @CsvSource({
        "0, 5",
        "1, 5",
        "0, 10",
        "2, 20"
    })
    void testGetHistoryTransactionWithPagination(int page, int size) {
        // Arrange
        Long accountId = 100L;

        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction1));

        when(transactionRepository.findByAccountId(eq(accountId), any(Pageable.class)))
            .thenReturn(transactionPage);
        when(transactionMapper.toResponseTransactionDTO(transaction1))
            .thenReturn(responseTransactionDTO1);

        // Act
        ResponseHistoryTransactionDto result = transactionService.getHistoryTransaction(accountId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(1, result.getTransactions().size());
        assertEquals(1L, result.getTransactions().get(0).getId());
    }

    // mvn test -Dtest=TransactionServiceTest#testGetHistoryTransactionVerifyTransactionDetails
    // Test: verify all transaction fields are correctly mapped from entity to DTO
    // Expected: All fields (id, accountId, amount, type, category, status, description, date) correctly set
    @Test
    void testGetHistoryTransactionVerifyTransactionDetails() {
        // Arrange
        Long accountId = 100L;
        int page = 0;
        int size = 10;

        Page<Transaction> transactionPage = new PageImpl<>(
            List.of(transaction1)
        );

        when(transactionRepository.findByAccountId(eq(accountId), any(Pageable.class)))
            .thenReturn(transactionPage);
        when(transactionMapper.toResponseTransactionDTO(transaction1))
            .thenReturn(responseTransactionDTO1);

        // Act
        ResponseHistoryTransactionDto result = transactionService.getHistoryTransaction(accountId, page, size);

        // Assert
        assertNotNull(result);
        ResponseTransactionDTO transactionDTO = result.getTransactions().get(0);
        assertEquals(1L, transactionDTO.getId());
        assertEquals(100L, transactionDTO.getAccountId());
        assertEquals(500.0, transactionDTO.getAmount());
        assertEquals("debit", transactionDTO.getType());
        assertEquals("withdrawal", transactionDTO.getCategory());
        assertEquals("completed", transactionDTO.getStatus());
        assertEquals("ATM Withdrawal", transactionDTO.getDescription());
        assertNotNull(transactionDTO.getDate());
    }
}

