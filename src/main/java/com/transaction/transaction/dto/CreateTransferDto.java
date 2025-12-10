package com.transaction.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTransferDto {
    @JsonProperty("from_account_id")
    @NotNull(message = "from_account_id is required")
    private Long fromAccountId;

    @NotNull(message = "to_account_id is required")
    @JsonProperty("to_account_id")
    private Long toAccountId;

    @NotNull(message = "amount is required")
    @JsonProperty("amount")
    private Double amount;
}
