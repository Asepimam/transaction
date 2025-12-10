package com.transaction.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseAccountBalanceDto {
    
    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("balance")
    private Double balance;
}
