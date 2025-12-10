package com.transaction.transaction.services;

import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.dto.UpdateBalanceDto;

public interface AccountService {
    ResponseAccountDto createAccount(CreateAccountDto createAccountDto);
    void updateBalance(UpdateBalanceDto updateBalanceDto);
    ResponseAccountBalanceDto getAccountBalance(long userId);
}
