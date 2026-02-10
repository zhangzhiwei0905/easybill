package com.easybill.repository;

import com.easybill.entity.Transaction;
import com.easybill.entity.TransactionStatus;
import com.easybill.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    
    List<Transaction> findByUser_IdOrderByTransactionTimeDesc(Long userId);
    
    List<Transaction> findByUser_IdAndStatus(Long userId, TransactionStatus status);
    
    List<Transaction> findByUser_IdAndType(Long userId, TransactionType type);
    
    List<Transaction> findByUser_IdAndTransactionTimeBetween(
        Long userId, LocalDateTime start, LocalDateTime end
    );
    
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    boolean existsByIdempotencyKey(String idempotencyKey);
}
