package com.transaction.transaction.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.transaction.transaction.dto.CreateTransferDto;
import com.transaction.transaction.entities.Account;
import com.transaction.transaction.entities.Transaction;
import com.transaction.transaction.entities.Transfer;
import com.transaction.transaction.entities.User;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.repositories.AccountRepository;
import com.transaction.transaction.repositories.TransactionRepository;
import com.transaction.transaction.repositories.TransferRepository;
import com.transaction.transaction.services.impl.TransferServiceImpl;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TransferRepository transferRepository;
    
    @InjectMocks
    private TransferServiceImpl transferService;

    private Account fromAccount;
    private Account toAccount;
    private User fromUser;
    private User toUser;
    private CreateTransferDto createTransferDto;
    private Transfer transfer;
    private Transaction debitTransaction;
    private Transaction creditTransaction;

    @BeforeEach
    void setUp() {
        // Setup users
        fromUser = new User();
        fromUser.setId(1L);
        
        toUser = new User();
        toUser.setId(2L);

        // Setup from account
        fromAccount = new Account();
        fromAccount.setId(10L);
        fromAccount.setUser(fromUser);
        fromAccount.setBalance(1000.0);

        // Setup to account
        toAccount = new Account();
        toAccount.setId(20L);
        toAccount.setUser(toUser);
        toAccount.setBalance(500.0);

        // Setup transfer DTO
        createTransferDto = new CreateTransferDto();
        createTransferDto.setFromAccountId(1L); // user ID
        createTransferDto.setToAccountId(2L);   // user ID
        createTransferDto.setAmount(300.0);

        // Setup transfer entity
        transfer = new Transfer();
        transfer.setId(100L);
        transfer.setFromAccountId(fromAccount.getId());
        transfer.setToAccountId(toAccount.getId());
        transfer.setAmount(300.0);

        // Setup transactions
        debitTransaction = new Transaction();
        debitTransaction.setId(1000L);
        debitTransaction.setAccountId(fromAccount.getId());
        debitTransaction.setType("debit");
        debitTransaction.setAmount(300.0);
        debitTransaction.setCategory("transfer_out");
        debitTransaction.setTransferId(transfer.getId());
        debitTransaction.setStatus("success");

        creditTransaction = new Transaction();
        creditTransaction.setId(2000L);
        creditTransaction.setAccountId(toAccount.getId());
        creditTransaction.setType("credit");
        creditTransaction.setAmount(300.0);
        creditTransaction.setCategory("transfer_in");
        creditTransaction.setTransferId(transfer.getId());
        creditTransaction.setStatus("success");
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferSuccess
    // Test: successful money transfer between two accounts
    // Expected: Debit from source account, credit to destination account, transfer and transactions recorded
    @Test
    void testCreateTransferSuccess() {
        // Arrange
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                if (t.getType().equals("debit")) {
                    t.setId(debitTransaction.getId());
                } else {
                    t.setId(creditTransaction.getId());
                }
                return t;
            });
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.existsById(any(Long.class))).thenReturn(true);

        // Act
        Long result = transferService.createTransfer(createTransferDto);

        // Assert
        assertNotNull(result);
        assertEquals(700.0, fromAccount.getBalance());
        assertEquals(800.0, toAccount.getBalance());
        verify(transferRepository, times(1)).save(any(Transfer.class));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferFromAccountNotFound
    // Test: transfer from non-existent account
    // Expected: ResourceNotFoundException thrown with "From account not found" message
    @Test
    void testCreateTransferFromAccountNotFound() {
        // Arrange
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResouceNotFoundException exception = assertThrows(
            ResouceNotFoundException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("From account not found", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferToAccountNotFound
    // Test: transfer to non-existent account
    // Expected: ResourceNotFoundException thrown with "To account not found" message
    @Test
    void testCreateTransferToAccountNotFound() {
        // Arrange
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // Act & Assert
        ResouceNotFoundException exception = assertThrows(
            ResouceNotFoundException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("To account not found", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferWithNegativeAmount
    // Test: transfer with negative amount
    // Expected: IllegalArgumentException thrown
    @Test
    void testCreateTransferWithNegativeAmount() {
        // Arrange
        createTransferDto.setAmount(-100.0);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("Transfer amount must be positive", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferWithZeroAmount
    // Test: transfer with zero amount
    // Expected: IllegalArgumentException thrown
    @Test
    void testCreateTransferWithZeroAmount() {
        // Arrange
        createTransferDto.setAmount(0.0);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("Transfer amount must be positive", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferToSameAccount
    // Test: transfer to the same account
    // Expected: IllegalArgumentException thrown - cannot transfer to same account
    @Test
    void testCreateTransferToSameAccount() {
        // Arrange
        createTransferDto.setToAccountId(1L); // Same as fromAccountId
        fromAccount.setId(10L);
        toAccount.setId(10L); // Same account ID
        
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("Cannot transfer to the same account", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferInsufficientBalance
    // Test: transfer with amount greater than source balance
    // Expected: IllegalArgumentException thrown - insufficient balance
    @Test
    void testCreateTransferInsufficientBalance() {
        // Arrange
        fromAccount.setBalance(100.0);
        createTransferDto.setAmount(300.0);
        
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.createTransfer(createTransferDto)
        );
        
        assertEquals("Insufficient balance in the source account", exception.getMessage());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferExactBalance
    // Test: transfer amount equal to entire source balance
    // Expected: Transfer succeeds, source balance becomes 0.0, destination balance increased
    @Test
    void testCreateTransferExactBalance() {
        // Arrange
        fromAccount.setBalance(300.0);
        createTransferDto.setAmount(300.0);
        
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                if (t.getType().equals("debit")) {
                    t.setId(debitTransaction.getId());
                } else {
                    t.setId(creditTransaction.getId());
                }
                return t;
            });
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.existsById(any(Long.class))).thenReturn(true);

        // Act
        Long result = transferService.createTransfer(createTransferDto);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, fromAccount.getBalance());
        assertEquals(800.0, toAccount.getBalance());
        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferVerifyBalanceUpdate
    // Test: verify balance changes are correctly applied to both accounts
    // Expected: Source balance decreased, destination balance increased by transfer amount
    @Test
    void testCreateTransferVerifyBalanceUpdate() {
        // Arrange
        Double initialFromBalance = fromAccount.getBalance();
        Double initialToBalance = toAccount.getBalance();
        Double transferAmount = createTransferDto.getAmount();
        
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                if (t.getType().equals("debit")) {
                    t.setId(debitTransaction.getId());
                } else {
                    t.setId(creditTransaction.getId());
                }
                return t;
            });
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.existsById(any(Long.class))).thenReturn(true);

        // Act
        transferService.createTransfer(createTransferDto);

        // Assert
        assertEquals(initialFromBalance - transferAmount, fromAccount.getBalance());
        assertEquals(initialToBalance + transferAmount, toAccount.getBalance());
    }

    // mvn test -Dtest=TransferServiceTest#testCreateTransferVerifyTransactionRecords
    // Test: verify debit and credit transactions are recorded for transfer
    // Expected: Two transactions created (one debit for source, one credit for destination)
    @Test
    void testCreateTransferVerifyTransactionRecords() {
        // Arrange
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                if (t.getType().equals("debit")) {
                    t.setId(debitTransaction.getId());
                } else {
                    t.setId(creditTransaction.getId());
                }
                return t;
            });
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.existsById(any(Long.class))).thenReturn(true);

        // Act
        transferService.createTransfer(createTransferDto);

        // Assert - Verify both debit and credit transactions are saved
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
