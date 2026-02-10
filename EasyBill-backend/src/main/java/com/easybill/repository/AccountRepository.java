package com.easybill.repository;

import com.easybill.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    List<Account> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<Account> findByUserIdAndLastFourDigits(Long userId, String lastFourDigits);
}
