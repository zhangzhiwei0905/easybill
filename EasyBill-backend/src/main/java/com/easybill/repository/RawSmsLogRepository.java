package com.easybill.repository;

import com.easybill.entity.RawSmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RawSmsLogRepository extends JpaRepository<RawSmsLog, Long> {
    
    List<RawSmsLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<RawSmsLog> findByUserIdAndParseStatus(Long userId, String parseStatus);
}
