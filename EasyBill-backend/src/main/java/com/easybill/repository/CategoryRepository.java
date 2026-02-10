package com.easybill.repository;

import com.easybill.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserIdOrUserIdIsNullOrderBySortOrder(Long userId);
    
    List<Category> findByUserIdAndType(Long userId, String type);
    
    List<Category> findByParentIdIsNullAndUserIdOrUserIdIsNull(Long userId);
}
