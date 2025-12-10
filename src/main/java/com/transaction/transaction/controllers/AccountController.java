package com.transaction.transaction.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.services.AccountService;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/create-account")
    public ResponseEntity<ResponseAccountDto> createAccount(@Valid @RequestBody CreateAccountDto createAccountDto) {
        ResponseAccountDto result =  accountService.createAccount(createAccountDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/balance")
    public ResponseEntity<ResponseAccountBalanceDto> getBalance(@RequestParam("userid") Long userId) {
        ResponseAccountBalanceDto result = accountService.getAccountBalance(userId);
        return ResponseEntity.ok(result);
    }
}
