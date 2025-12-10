package com.transaction.transaction.services;

import com.transaction.transaction.dto.CreateTransferDto;

public interface TransferService {
    Long createTransfer(CreateTransferDto createTransferDto);
}
