package com.transaction.transaction.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transaction.transaction.dto.ResponseHistoryTransactionDto;
import com.transaction.transaction.services.TransactionService;

@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<ResponseHistoryTransactionDto> getHistoryTransaction(@RequestParam("account_id") Long accountId, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "10") int size) {
        var response = transactionService.getHistoryTransaction(accountId, page, size);

        return ResponseEntity.ok(response);
    }
}
