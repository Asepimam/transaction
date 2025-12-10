package com.transaction.transaction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.transaction.transaction.dto.ResponseAccountBalanceDto;
import com.transaction.transaction.dto.ResponseAccountDto;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userName", target = "userName")
    ResponseAccountDto toResponseAccountDto(com.transaction.transaction.entities.Account account);
    
    @Mapping(source = "user.id", target = "userId")
    ResponseAccountBalanceDto toResponseAccountBalanceDto(com.transaction.transaction.entities.Account account);

    
}
