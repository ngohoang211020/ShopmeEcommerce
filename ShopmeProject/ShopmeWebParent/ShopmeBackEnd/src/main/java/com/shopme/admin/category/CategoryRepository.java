package com.shopme.admin.category;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Category;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>{
	@Query("SELECT c FROM Category c where c.parent is NULL")
	public List<Category> findRootCategories();
	
	
	public Category findByName(String name);
	
	public Category findByAlias(String alias);
}
