package com.transaction.transaction.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.transaction.transaction.dto.ResponseHistoryTransactionDto;
import com.transaction.transaction.dto.ResponseTransactionDTO;
import com.transaction.transaction.entities.Transaction;
import com.transaction.transaction.mappers.TransactionMapper;
import com.transaction.transaction.repositories.TransactionRepository;
import com.transaction.transaction.services.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public ResponseHistoryTransactionDto getHistoryTransaction(Long accountId, int page, int size) {
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, Pageable.ofSize(size).withPage(page));
        
        List<ResponseTransactionDTO> transactionDTOs = transactions
            .map(transactionMapper::toResponseTransactionDTO)
            .getContent();
        
        ResponseHistoryTransactionDto response = new ResponseHistoryTransactionDto();
        response.setAccountId(accountId);
        response.setTransactions(transactionDTOs);
        
        return response;
    }
     
}
