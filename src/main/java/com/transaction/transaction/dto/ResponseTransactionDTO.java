package com.transaction.transaction.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseTransactionDTO {
    @JsonProperty("id")
    private long id;

    @JsonProperty("account_id")
    private long accountId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("type")
    private String type;

    @JsonProperty("transfer_id")
    private Long transferId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("date")
    private LocalDateTime date;
}
