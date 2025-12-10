package com.transaction.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateAccountDto {
    @JsonProperty("user_name")
    private String userName;
}
