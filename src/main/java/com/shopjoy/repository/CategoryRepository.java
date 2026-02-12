package com.shopjoy.repository;

import com.shopjoy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    List<Category> findByParentCategoryIdIsNull();
    
    List<Category> findByParentCategoryId(Integer parentCategoryId);
    
    boolean existsByParentCategoryId(int categoryId);
}
