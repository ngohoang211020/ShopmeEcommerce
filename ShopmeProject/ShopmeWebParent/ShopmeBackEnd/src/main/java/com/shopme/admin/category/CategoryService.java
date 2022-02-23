package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.shopme.common.entity.Category;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepo;

	public List<Category> listAll() {
		List<Category> rootCategories= categoryRepo.findRootCategories();
		return listHierarchicalCategories(rootCategories);
	}
	private List<Category> listHierarchicalCategories(List<Category> rootCategories){
		List<Category> hierarchicalCategories=new ArrayList<>();
		
		for(Category rootCategory: rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children= rootCategory.getChildren();
			
			for(Category subCategory: children) {
				String name="--"+ subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory, name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,Category parent, int subLevel) {
		int newSubLevel= subLevel +1;
		Set<Category> children=parent.getChildren();
		for(Category subCategory: children) {
			String name="";
			for(int i=0;i<newSubLevel;i++) {
				name+="--";
			}
			name+=subCategory.getName();
			subCategory.setName(name);
			hierarchicalCategories.add(Category.copyIdAndName(subCategory));
		
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel);
		}
	}
	
	public List<Category> listCategoriesUsedInForm(){
		List<Category> categoriesUsedInForm=new ArrayList<>();
		Iterable<Category> categories=categoryRepo.findAll();
		for(Category category: categories) {
			if(category.getParent()==null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				Set<Category> children= category.getChildren();
				for(Category subCategory: children) {
					String name="--"+ subCategory.getName();
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(),name));
					printChildren(categoriesUsedInForm,subCategory, 1);
				}
			}
		}
		return categoriesUsedInForm;
	}
	
	public Category save(Category category) {
		return categoryRepo.save(category);
	}
	
	private void printChildren(List<Category> categoriesUsedInForm,Category parent, int subLevel) {
		int newSubLevel= subLevel +1;
		Set<Category> children=parent.getChildren();
		for(Category subCategory: children) {
			String name="";
			for(int i=0;i<newSubLevel;i++) {
				name+="--";
			}
			name+=subCategory.getName();
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(),name));
			
			printChildren(categoriesUsedInForm,subCategory, newSubLevel);
		}
	}
}
