package com.shopjoy.repository;

import com.shopjoy.entity.Category;

import java.util.List;

public interface ICategoryRepository extends GenericRepository<Category, Integer> {
    List<Category> findTopLevelCategories();
    List<Category> findSubcategories(Integer parentCategoryId);
    boolean hasSubcategories(int categoryId);
}
