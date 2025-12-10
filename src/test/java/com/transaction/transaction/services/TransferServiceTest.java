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
