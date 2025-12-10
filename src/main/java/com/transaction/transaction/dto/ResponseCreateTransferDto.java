package com.transaction.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseCreateTransferDto {
    @JsonProperty("transfer_id")
    private Long transferId;
    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;

}
