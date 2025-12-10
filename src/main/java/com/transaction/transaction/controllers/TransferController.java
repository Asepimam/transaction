package com.transaction.transaction.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.transaction.transaction.dto.CreateTransferDto;
import com.transaction.transaction.dto.ResponseCreateTransferDto;
import com.transaction.transaction.services.TransferService;

import jakarta.validation.Valid;

@RestController
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ResponseCreateTransferDto> createTransfer(@Valid @RequestBody CreateTransferDto createTransferDto) {
        Long result = transferService.createTransfer(createTransferDto);
        ResponseCreateTransferDto response = new ResponseCreateTransferDto();
        response.setTransferId(result);
        response.setMessage("Transfer successful");
        response.setStatus("success");
        return ResponseEntity.ok(response);
    }
}
