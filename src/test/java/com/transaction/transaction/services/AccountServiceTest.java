package com.transaction.transaction.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.dto.UpdateBalanceDto;
import com.transaction.transaction.entities.Account;
import com.transaction.transaction.entities.User;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.repositories.AccountRepository;
import com.transaction.transaction.repositories.UserRepository;
import com.transaction.transaction.services.impl.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;
    private CreateAccountDto createAccountDto;
    private UpdateBalanceDto updateBalanceDto;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(10L);
        user.setUserName("John Doe");

        // Setup account
        account = new Account();
        account.setId(100L);
        account.setUser(user);
        account.setBalance(500.0);

        // Setup DTOs
        createAccountDto = new CreateAccountDto();
        createAccountDto.setUserName("John Doe");

        updateBalanceDto = new UpdateBalanceDto();
        updateBalanceDto.setUserId(10L);
        updateBalanceDto.setBalance(1000.0);
    }

    @Test
    void testCreateAccountSuccess() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setId(100L);
        newAccount.setUser(user);
        newAccount.setBalance(0.0);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

        // Act
        ResponseAccountDto result = accountService.createAccount(createAccountDto);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getUserName(), result.getUserName());
        assertEquals(0.0, result.getBalance());
        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @ParameterizedTest(name = "Create account for {0}")
    @CsvSource({
        "John Doe",
        "Jane Smith",
        "Bob Wilson",
        "Alice Brown"
    })
    void testCreateAccountWithDifferentUserNames(String userName) {
        // Arrange
        createAccountDto.setUserName(userName);
        User newUser = new User();
        newUser.setId(20L);
        newUser.setUserName(userName);

        Account newAccount = new Account();
        newAccount.setId(200L);
        newAccount.setUser(newUser);
        newAccount.setBalance(0.0);

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

        // Act
        ResponseAccountDto result = accountService.createAccount(createAccountDto);

        // Assert
        assertNotNull(result);
        assertEquals(userName, result.getUserName());
        assertEquals(0.0, result.getBalance());
    }

    @Test
    void testCreateAccountInitialBalanceIsZero() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            assertEquals(0.0, acc.getBalance());
            return acc;
        });

        // Act
        ResponseAccountDto result = accountService.createAccount(createAccountDto);

        // Assert
        assertEquals(0.0, result.getBalance());
    }

    @ParameterizedTest(name = "Get balance with {0}")
    @CsvSource({
        "10, 500.0",
        "20, 750.0",
        "30, 0.0",
        "40, 999999999.99"
    })
    void testGetBalanceWithVariousAmounts(long userId, double balance) {
        // Arrange
        User testUser = new User();
        testUser.setId(userId);
        testUser.setUserName("User " + userId);

        Account testAccount = new Account();
        testAccount.setId(userId * 10);
        testAccount.setUser(testUser);
        testAccount.setBalance(balance);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(testAccount));

        // Act
        ResponseAccountBalanceDto result = accountService.getAccountBalance(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(balance, result.getBalance());
    }

    @Test
    void testGetBalanceUserNotFound() {
        // Arrange
        when(accountRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResouceNotFoundException exception = assertThrows(ResouceNotFoundException.class, () -> {
            accountService.getAccountBalance(99L);
        });
        
        assertEquals("Account not found", exception.getMessage());
    }

    @ParameterizedTest(name = "Update balance to {0}")
    @ValueSource(doubles = {0.0, 100.0, 500.0, 1000.0, 123.456, -500.0, 999999.99})
    void testUpdateBalanceWithVariousAmounts(double newBalance) {
        // Arrange
        updateBalanceDto.setBalance(newBalance);
        when(accountRepository.findByUserId(10L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        accountService.updateBalance(updateBalanceDto);

        // Assert
        assertEquals(newBalance, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testUpdateBalanceUserNotFound() {
        // Arrange
        updateBalanceDto.setUserId(99L);
        when(accountRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResouceNotFoundException exception = assertThrows(ResouceNotFoundException.class, () -> {
            accountService.updateBalance(updateBalanceDto);
        });
        
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(0)).save(any(Account.class));
    }

    @Test
    void testUpdateBalanceMultipleTimes() {
        // Arrange
        when(accountRepository.findByUserId(10L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert - Update 1
        updateBalanceDto.setBalance(1000.0);
        accountService.updateBalance(updateBalanceDto);
        assertEquals(1000.0, account.getBalance());

        // Act & Assert - Update 2
        updateBalanceDto.setBalance(1500.0);
        accountService.updateBalance(updateBalanceDto);
        assertEquals(1500.0, account.getBalance());

        // Act & Assert - Update 3
        updateBalanceDto.setBalance(500.0);
        accountService.updateBalance(updateBalanceDto);
        assertEquals(500.0, account.getBalance());

        // Verify all three saves
        verify(accountRepository, times(3)).save(account);
    }

    @Test
    void testCreateAccountVerifyUserSavedFirst() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            assertNotNull(acc.getUser());
            assertNotNull(acc.getUser().getId());
            return account;
        });

        // Act
        accountService.createAccount(createAccountDto);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
