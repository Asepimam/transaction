package com.transaction.transaction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.transaction.transaction.dto.ResponseTransactionDTO;
import com.transaction.transaction.entities.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    ResponseTransactionDTO toResponseTransactionDTO(Transaction transaction);
}
