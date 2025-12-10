package com.transaction.transaction.services;


import com.transaction.transaction.dto.ResponseHistoryTransactionDto;

public interface TransactionService {
    
    ResponseHistoryTransactionDto getHistoryTransaction(Long accountId, int page, int size);
}
