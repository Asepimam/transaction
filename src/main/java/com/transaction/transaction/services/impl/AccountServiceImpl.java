package com.transaction.transaction.services.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transaction.transaction.dto.CreateAccountDto;
import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;
import com.transaction.transaction.dto.UpdateBalanceDto;
import com.transaction.transaction.entities.Account;
import com.transaction.transaction.entities.User;
import com.transaction.transaction.exceptions.ResouceNotFoundException;
import com.transaction.transaction.repositories.AccountRepository;
import com.transaction.transaction.repositories.UserRepository;
import com.transaction.transaction.services.AccountService;


@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        
    }

    @Override
    @Transactional
    public ResponseAccountDto createAccount(CreateAccountDto createAccountDto) {

        User user = new User();
        user.setUserName(createAccountDto.getUserName());
        
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setBalance(0.0);


        Account savedAccount = accountRepository.save(account);
        ResponseAccountDto responseAccountDto = new ResponseAccountDto();
        responseAccountDto.setUserId(savedUser.getId());
        responseAccountDto.setUserName(savedUser.getUserName());
        responseAccountDto.setBalance(savedAccount.getBalance());
        return  responseAccountDto;
    }


    @Override
    public void updateBalance(UpdateBalanceDto updateBalanceDto) {
        Account account = accountRepository.findByUserId(updateBalanceDto.getUserId())
                .orElseThrow(() -> new ResouceNotFoundException("Account not found"));

        account.setBalance(updateBalanceDto.getBalance());
        accountRepository.save(account);
    }

    @Override
    public ResponseAccountBalanceDto getAccountBalance(long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResouceNotFoundException("Account not found"));
        
        ResponseAccountBalanceDto responseAccountBalanceDto = new ResponseAccountBalanceDto();
        responseAccountBalanceDto.setBalance(account.getBalance());
        responseAccountBalanceDto.setUserId(account.getUser().getId());
        return responseAccountBalanceDto;
    }
    
}
