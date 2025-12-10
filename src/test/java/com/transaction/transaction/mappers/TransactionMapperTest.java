package com.transaction.transaction.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.transaction.transaction.dto.ResponseTransactionDTO;
import com.transaction.transaction.entities.Transaction;

@SpringBootTest
class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountId(100L);
        transaction.setAmount(500.0);
        transaction.setType("debit");
        transaction.setTransferId(10L);
        transaction.setCategory("transfer_out");
        transaction.setStatus("success");
        transaction.setDescription("Test transaction");
        transaction.setDate(LocalDateTime.of(2025, 12, 10, 10, 30));
    }

    @Test
    void testToResponseTransactionDTO() {
        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getAccountId());
        assertEquals(500.0, result.getAmount());
        assertEquals("debit", result.getType());
        assertEquals(10L, result.getTransferId());
        assertEquals("transfer_out", result.getCategory());
        assertEquals("success", result.getStatus());
        assertEquals("Test transaction", result.getDescription());
        assertEquals(LocalDateTime.of(2025, 12, 10, 10, 30), result.getDate());
    }

    @ParameterizedTest(name = "Type: {0}, Category: {1}")
    @CsvSource({
        "debit, transfer_out",
        "credit, transfer_in",
        "debit, withdrawal",
        "credit, deposit"
    })
    void testToResponseTransactionDTOWithTypesAndCategories(String type, String category) {
        // Arrange
        transaction.setType(type);
        transaction.setCategory(category);

        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(type, result.getType());
        assertEquals(category, result.getCategory());
    }

    @ParameterizedTest(name = "Amount: {0}")
    @CsvSource({
        "0.0",
        "100.0",
        "123.456",
        "500.0",
        "999999.99"
    })
    void testToResponseTransactionDTOWithVariousAmounts(double amount) {
        // Arrange
        transaction.setAmount(amount);

        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(amount, result.getAmount());
    }

    @Test
    void testToResponseTransactionDTOWithNullTransferId() {
        // Arrange
        transaction.setTransferId(null);

        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNull(result.getTransferId());
    }

    @Test
    void testToResponseTransactionDTOWithNullDescription() {
        // Arrange
        transaction.setDescription(null);

        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNull(result.getDescription());
    }

    @Test
    void testToResponseTransactionDTOAllFieldsMapping() {
        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert - Verify all fields are mapped correctly
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getAccountId());
        assertEquals(500.0, result.getAmount());
        assertNotNull(result.getType());
        assertNotNull(result.getTransferId());
        assertNotNull(result.getCategory());
        assertNotNull(result.getStatus());
        assertNotNull(result.getDescription());
        assertNotNull(result.getDate());
    }

    @ParameterizedTest(name = "Category: {0}")
    @ValueSource(strings = {"withdrawal", "deposit", "transfer", "transfer_in", "transfer_out"})
    void testToResponseTransactionDTOWithDifferentCategories(String category) {
        // Arrange
        transaction.setCategory(category);

        // Act
        ResponseTransactionDTO result = transactionMapper.toResponseTransactionDTO(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(category, result.getCategory());
    }
}
