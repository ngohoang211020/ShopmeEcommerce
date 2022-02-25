package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopme.admin.user.UserNotFoundException;
import com.shopme.common.entity.Category;

@Service
@Transactional
public class CategoryService {
	public static final int CATEGORIES_PER_PAGE=4;

	@Autowired
	private CategoryRepository categoryRepo;

	public List<Category> listAll(String sortDir){
		Sort sort=Sort.by("name");
		
		if (sortDir == null || sortDir.isEmpty()) {
			sort = sort.ascending(); 
		}
		
		else if(sortDir.equals("asc")) sort=sort.ascending();
		
		else if(sortDir.equals("desc")) sort=sort.descending();
		
		List<Category> rootCategories= categoryRepo.findRootCategories(sort);
		
		return listHierarchicalCategories(rootCategories,sortDir);
	}
	
	public List<Category> listByPage(CategoryPageInfo categoryPageInfo, Integer pageNum ,String sortDir) {
		Sort sort=Sort.by("name");
		
		if (sortDir == null || sortDir.isEmpty()) {
			sort = sort.ascending(); 
		}
		
		else if(sortDir.equals("asc")) sort=sort.ascending();
		
		else if(sortDir.equals("desc")) sort=sort.descending();
		
		Pageable pageable=PageRequest.of(pageNum-1, CATEGORIES_PER_PAGE,sort);
		Page<Category> pageCategories= categoryRepo.findRootCategories(pageable);
		
		categoryPageInfo.setTotalElements(pageCategories.getTotalElements());
		categoryPageInfo.setTotalPages(pageCategories.getTotalPages());
		
		List<Category> rootCategories=pageCategories.getContent();
		
		return listHierarchicalCategories(rootCategories,sortDir);
	}
	private List<Category> listHierarchicalCategories(List<Category> rootCategories,String sortDir){
		List<Category> hierarchicalCategories=new ArrayList<>();
		
		for(Category rootCategory: rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children=  sortSubCategories(rootCategory.getChildren(),sortDir);
			
			for(Category subCategory: children) {
				String name="--"+ subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory, name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1,sortDir);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,Category parent, int subLevel,String sortDir) {
		int newSubLevel= subLevel +1;
		Set<Category> children= sortSubCategories(parent.getChildren(),sortDir);
		for(Category subCategory: children) {
			String name="";
			for(int i=0;i<newSubLevel;i++) {
				name+="--";
			}
			name+=subCategory.getName();
			hierarchicalCategories.add(Category.copyFull(subCategory,name));
		
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel,sortDir);
		}
	}
	
	public List<Category> listCategoriesUsedInForm(){
		List<Category> categoriesUsedInForm=new ArrayList<>();
		Iterable<Category> categories=categoryRepo.findRootCategories(Sort.by("name").ascending());
		for(Category category: categories) {
			if(category.getParent()==null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				Set<Category> children=  sortSubCategories(category.getChildren());
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
		
		boolean isUpdatingCategory = (category.getId() != null);
		if (isUpdatingCategory) {
			Category existingCategory = categoryRepo.findById(category.getId()).get();
				
			if (category.getImage()==null) {
				category.setImage(existingCategory.getImage());
			}
		}
		return categoryRepo.save(category);
	}
	
	private void printChildren(List<Category> categoriesUsedInForm,Category parent, int subLevel) {
		int newSubLevel= subLevel +1;
		Set<Category> children= sortSubCategories(parent.getChildren());
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
	
	public Category findById(Integer id) throws UserNotFoundException {
		try {
			return categoryRepo.findById(id).get();
		} catch (Exception e) {
			throw new UserNotFoundException("Could not find any category with ID = " + id);
		}
	}
	
	public Category findByName(String name) {
		return categoryRepo.findByName(name);
	}
	
	public Category findByAlias(String alias) {
		return categoryRepo.findByAlias(alias);
	}

	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew =(id==null|| id==0);
		Category categoryFindByName= categoryRepo.findByName(name);
		
		if(isCreatingNew) {
			if(categoryFindByName != null) {
				return "DuplicateName";
			} else {
				Category categoryFindByAlias=categoryRepo.findByAlias(alias);
				if(categoryFindByAlias != null) {
					return "DuplicateAlias";
				}
			}
		} else {
			Category categoryFindByAlias=categoryRepo.findByAlias(alias);
			if(categoryFindByName != null && categoryFindByName.getId() != id) {
				return "DuplicateName";
			}
			else if(categoryFindByAlias != null && categoryFindByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}
		
		return "OK";
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children){
		return sortSubCategories(children,"asc");
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children,String sortDir){
		SortedSet<Category> sortedChildren= new TreeSet<>(new Comparator<Category>() {

			@Override
			public int compare(Category cat1, Category cat2) {
				// TODO Auto-generated method stub
				if(sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				}
				else {
					return cat2.getName().compareTo(cat1.getName());
				}
			}
		});
		sortedChildren.addAll(children);
		return sortedChildren;
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		categoryRepo.updateEnabledStatus(id, enabled);
	}
	public void delete(Integer id) throws UserNotFoundException {
		Integer countById = categoryRepo.countById(id);
		if (countById == null || countById == 0) {
			throw new UserNotFoundException("Could not find any category with ID = " + id);
		}
		categoryRepo.deleteById(id);
	}
	
}
