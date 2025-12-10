package com.transaction.transaction.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transaction.transaction.entities.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);   
    Boolean existsByUserId(Long userId);
    
    @Query("SELECT a.balance FROM Account a WHERE a.id = :userId")
    Long getBalanceByUserId(@Param("userId") Long userId);
}
