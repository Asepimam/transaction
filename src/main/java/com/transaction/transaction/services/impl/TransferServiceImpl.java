package com.transaction.transaction.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transaction.transaction.dto.CreateTransferDto;
import com.transaction.transaction.entities.Account;
import com.transaction.transaction.entities.Transaction;
import com.transaction.transaction.entities.Transfer;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.repositories.AccountRepository;
import com.transaction.transaction.repositories.TransactionRepository;
import com.transaction.transaction.repositories.TransferRepository;
import com.transaction.transaction.services.TransferService;

import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;

    public TransferServiceImpl(AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(CreateTransferDto createTransferDto) {

        // validasi account
        Account fromAccount = accountRepository.findByUserId(createTransferDto.getFromAccountId())
                .orElseThrow(() -> new ResouceNotFoundException("From account not found"));

        Account toAccount = accountRepository.findByUserId(createTransferDto.getToAccountId())
                .orElseThrow(() -> new ResouceNotFoundException("To account not found"));

        //Validasi amount
        if (createTransferDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (fromAccount.getBalance() < createTransferDto.getAmount()) {
            throw new IllegalArgumentException("Insufficient balance in the source account");
        }


        
       


        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccount.getId());
        transfer.setToAccountId(toAccount.getId());
        transfer.setAmount(createTransferDto.getAmount());
        transfer.setDate(LocalDateTime.now());
        transferRepository.save(transfer);

        fromAccount.setBalance(fromAccount.getBalance() - createTransferDto.getAmount());
        accountRepository.save(fromAccount);

        Transaction debitTx = new Transaction();
        debitTx.setAccountId(fromAccount.getId());
        debitTx.setType("debit");
        debitTx.setAmount(createTransferDto.getAmount());
        debitTx.setCategory("transfer_out");
        debitTx.setTransferId(transfer.getId());
        debitTx.setStatus("success");
        debitTx.setDate(LocalDateTime.now());
        transactionRepository.save(debitTx);

        toAccount.setBalance(toAccount.getBalance() + createTransferDto.getAmount());
        accountRepository.save(toAccount);

        Transaction creditTx = new Transaction();
        creditTx.setAccountId(toAccount.getId());
        creditTx.setType("credit");
        creditTx.setAmount(createTransferDto.getAmount());
        creditTx.setCategory("transfer_in");
        creditTx.setTransferId(transfer.getId());
        creditTx.setDate(LocalDateTime.now());
        creditTx.setStatus("success");
        transactionRepository.save(creditTx);

        Account fromCheck = accountRepository.findById(fromAccount.getId())
                .orElseThrow(() -> new ResouceNotFoundException("From account not found"));
        Account toCheck = accountRepository.findById(toAccount.getId())
                .orElseThrow(() -> new ResouceNotFoundException("To account not found"));
        if (!fromCheck.getBalance().equals(fromAccount.getBalance()) ||
            !toCheck.getBalance().equals(toAccount.getBalance())) {
            throw new IllegalStateException("Balance mismatch after transfer");
        }
        boolean debitExists = transactionRepository.existsById(debitTx.getId());
        boolean creditExists = transactionRepository.existsById(creditTx.getId());
        if (!debitExists || !creditExists) {
            throw new IllegalStateException("Transaction records missing after transfer");
        }

        return debitTx.getId();
    }
}
