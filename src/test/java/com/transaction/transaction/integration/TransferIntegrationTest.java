package com.transaction.transaction.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.CreateTransferDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.dto.UpdateBalanceDto;
import com.transaction.transaction.services.AccountService;
import com.transaction.transaction.services.TransferService;

@SpringBootTest
@Transactional
class TransferIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    private Long fromUserId;
    private Long toUserId;

    @BeforeEach
    void setUp() {
        // Create two accounts for testing
        CreateAccountDto fromAccountDto = new CreateAccountDto();
        fromAccountDto.setUserName("From User");
        ResponseAccountDto fromAccount = accountService.createAccount(fromAccountDto);
        fromUserId = fromAccount.getUserId();

        CreateAccountDto toAccountDto = new CreateAccountDto();
        toAccountDto.setUserName("To User");
        ResponseAccountDto toAccount = accountService.createAccount(toAccountDto);
        toUserId = toAccount.getUserId();

        // Set initial balance for from account
        UpdateBalanceDto updateBalance = new UpdateBalanceDto();
        updateBalance.setUserId(fromUserId);
        updateBalance.setBalance(1000.0);
        accountService.updateBalance(updateBalance);
    }

    @Test
    void testCompleteTransferFlow() {
        // 1. Check initial balances
        ResponseAccountBalanceDto fromBalanceBefore = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalanceBefore = accountService.getAccountBalance(toUserId);
        
        assertEquals(1000.0, fromBalanceBefore.getBalance());
        assertEquals(0.0, toBalanceBefore.getBalance());

        // 2. Perform transfer
        CreateTransferDto transferDto = new CreateTransferDto();
        transferDto.setFromAccountId(fromUserId);
        transferDto.setToAccountId(toUserId);
        transferDto.setAmount(300.0);

        Long transferId = transferService.createTransfer(transferDto);
        assertNotNull(transferId);

        // 3. Check balances after transfer
        ResponseAccountBalanceDto fromBalanceAfter = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalanceAfter = accountService.getAccountBalance(toUserId);

        assertEquals(700.0, fromBalanceAfter.getBalance());
        assertEquals(300.0, toBalanceAfter.getBalance());

        // 4. Verify transaction history (need to get account IDs first)
        // Note: We're using userId here, but the transaction uses accountId
        // This test assumes the relationship works correctly
    }

    @Test
    void testMultipleTransfers() {
        // Perform multiple transfers
        CreateTransferDto transfer1 = new CreateTransferDto();
        transfer1.setFromAccountId(fromUserId);
        transfer1.setToAccountId(toUserId);
        transfer1.setAmount(200.0);
        transferService.createTransfer(transfer1);

        CreateTransferDto transfer2 = new CreateTransferDto();
        transfer2.setFromAccountId(fromUserId);
        transfer2.setToAccountId(toUserId);
        transfer2.setAmount(150.0);
        transferService.createTransfer(transfer2);

        CreateTransferDto transfer3 = new CreateTransferDto();
        transfer3.setFromAccountId(fromUserId);
        transfer3.setToAccountId(toUserId);
        transfer3.setAmount(100.0);
        transferService.createTransfer(transfer3);

        // Check final balances
        ResponseAccountBalanceDto fromBalance = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalance = accountService.getAccountBalance(toUserId);

        assertEquals(550.0, fromBalance.getBalance()); // 1000 - 200 - 150 - 100
        assertEquals(450.0, toBalance.getBalance());   // 0 + 200 + 150 + 100
    }

    @Test
    void testTransferWithExactBalance() {
        // Transfer exact balance
        CreateTransferDto transferDto = new CreateTransferDto();
        transferDto.setFromAccountId(fromUserId);
        transferDto.setToAccountId(toUserId);
        transferDto.setAmount(1000.0);

        transferService.createTransfer(transferDto);

        // Check balances
        ResponseAccountBalanceDto fromBalance = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalance = accountService.getAccountBalance(toUserId);

        assertEquals(0.0, fromBalance.getBalance());
        assertEquals(1000.0, toBalance.getBalance());
    }

    @Test
    void testBidirectionalTransfer() {
        // Set balance for toUser as well
        UpdateBalanceDto updateBalance = new UpdateBalanceDto();
        updateBalance.setUserId(toUserId);
        updateBalance.setBalance(500.0);
        accountService.updateBalance(updateBalance);

        // Transfer from User1 to User2
        CreateTransferDto transfer1 = new CreateTransferDto();
        transfer1.setFromAccountId(fromUserId);
        transfer1.setToAccountId(toUserId);
        transfer1.setAmount(300.0);
        transferService.createTransfer(transfer1);

        // Transfer from User2 to User1
        CreateTransferDto transfer2 = new CreateTransferDto();
        transfer2.setFromAccountId(toUserId);
        transfer2.setToAccountId(fromUserId);
        transfer2.setAmount(100.0);
        transferService.createTransfer(transfer2);

        // Check final balances
        ResponseAccountBalanceDto fromBalance = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalance = accountService.getAccountBalance(toUserId);

        assertEquals(800.0, fromBalance.getBalance());  // 1000 - 300 + 100
        assertEquals(700.0, toBalance.getBalance());    // 500 + 300 - 100
    }

    @Test
    void testCreateMultipleAccounts() {
        // Create additional accounts
        CreateAccountDto account3 = new CreateAccountDto();
        account3.setUserName("User 3");
        ResponseAccountDto acc3 = accountService.createAccount(account3);

        CreateAccountDto account4 = new CreateAccountDto();
        account4.setUserName("User 4");
        ResponseAccountDto acc4 = accountService.createAccount(account4);

        // Verify they were created
        assertTrue(acc3.getUserId() > 0);
        assertTrue(acc4.getUserId() > 0);
        assertEquals(0.0, acc3.getBalance());
        assertEquals(0.0, acc4.getBalance());
    }

    @Test
    void testUpdateBalanceMultipleTimes() {
        // Update balance multiple times
        UpdateBalanceDto update1 = new UpdateBalanceDto();
        update1.setUserId(toUserId);
        update1.setBalance(100.0);
        accountService.updateBalance(update1);

        ResponseAccountBalanceDto balance1 = accountService.getAccountBalance(toUserId);
        assertEquals(100.0, balance1.getBalance());

        UpdateBalanceDto update2 = new UpdateBalanceDto();
        update2.setUserId(toUserId);
        update2.setBalance(500.0);
        accountService.updateBalance(update2);

        ResponseAccountBalanceDto balance2 = accountService.getAccountBalance(toUserId);
        assertEquals(500.0, balance2.getBalance());

        UpdateBalanceDto update3 = new UpdateBalanceDto();
        update3.setUserId(toUserId);
        update3.setBalance(0.0);
        accountService.updateBalance(update3);

        ResponseAccountBalanceDto balance3 = accountService.getAccountBalance(toUserId);
        assertEquals(0.0, balance3.getBalance());
    }

    @Test
    void testAccountCreationWithDifferentUserNames() {
        // Create accounts with various usernames
        String[] userNames = {"Alice", "Bob", "Charlie", "David", "Eve"};
        
        for (String userName : userNames) {
            CreateAccountDto accountDto = new CreateAccountDto();
            accountDto.setUserName(userName);
            ResponseAccountDto account = accountService.createAccount(accountDto);
            
            assertEquals(userName, account.getUserName());
            assertEquals(0.0, account.getBalance());
            assertTrue(account.getUserId() > 0);
        }
    }

    @Test
    void testTransferWithDecimalAmount() {
        // Transfer with decimal amount
        CreateTransferDto transferDto = new CreateTransferDto();
        transferDto.setFromAccountId(fromUserId);
        transferDto.setToAccountId(toUserId);
        transferDto.setAmount(123.45);

        transferService.createTransfer(transferDto);

        // Check balances
        ResponseAccountBalanceDto fromBalance = accountService.getAccountBalance(fromUserId);
        ResponseAccountBalanceDto toBalance = accountService.getAccountBalance(toUserId);

        assertEquals(876.55, fromBalance.getBalance(), 0.01);
        assertEquals(123.45, toBalance.getBalance(), 0.01);
    }
}
