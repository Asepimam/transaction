package com.transaction.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseAccountDto {
    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("balance")
    private Double balance;
}