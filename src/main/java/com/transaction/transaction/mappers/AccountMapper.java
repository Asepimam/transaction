package com.transaction.transaction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    ResponseAccountDto toResponseAccountDto(com.transaction.transaction.entities.Account account);
    
    ResponseAccountBalanceDto toResponseAccountBalanceDto(com.transaction.transaction.entities.Account account);

    
}
