package com.transaction.transaction.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseHistoryTransactionDto {
    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("transactions")
    private List<ResponseTransactionDTO> transactions;
}
