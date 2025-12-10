package com.transaction.transaction.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.entities.Account;
import com.transaction.transaction.entities.User;

@SpringBootTest
class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    private Account account;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUserName("John Doe");

        account = new Account();
        account.setId(100L);
        account.setUser(user);
        account.setBalance(1000.0);
    }

    // mvn test -Dtest=AccountMapperTest#testToResponseAccountDto
    // Test: map Account entity to ResponseAccountDto
    // Expected: All fields correctly mapped (userId, userName, balance)
    @Test
    void testToResponseAccountDto() {
        // Act
        ResponseAccountDto result = accountMapper.toResponseAccountDto(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals(1000.0, result.getBalance());
    }

    // mvn test -Dtest=AccountMapperTest#testToResponseAccountDtoWithDifferentUsers
    // Test: map Account with different users and balances
    // Expected: Correct mapping for all user/balance combinations
    @ParameterizedTest(name = "User {0} with userId {1} and balance {2}")
    @CsvSource({
        "John Doe, 1, 1000.0",
        "Jane Smith, 2, 500.0",
        "Bob Wilson, 3, 0.0",
        "Alice Brown, 4, 999999.99"
    })
    void testToResponseAccountDtoWithDifferentUsers(String userName, long userId, double balance) {
        // Arrange
        user.setId(userId);
        user.setUserName(userName);
        account.setBalance(balance);

        // Act
        ResponseAccountDto result = accountMapper.toResponseAccountDto(account);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(userName, result.getUserName());
        assertEquals(balance, result.getBalance());
    }

    // mvn test -Dtest=AccountMapperTest#testToResponseAccountBalanceDtoWithVariousBalances
    // Test: map Account to ResponseAccountBalanceDto with different balance values
    // Expected: Correct mapping including userId and various balance amounts
    @ParameterizedTest(name = "Balance {0}")
    @CsvSource({
        "0.0",
        "100.0",
        "123.456",
        "999999.99"
    })
    void testToResponseAccountBalanceDtoWithVariousBalances(double balance) {
        // Arrange
        account.setBalance(balance);

        // Act
        ResponseAccountBalanceDto result = accountMapper.toResponseAccountBalanceDto(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(balance, result.getBalance());
    }
}
