package com.transaction.transaction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transaction.transaction.entities.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    
}
