package com.shopme.admin.category.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.category.CategoryPageInfo;
import com.shopme.admin.category.CategoryService;
import com.shopme.admin.exportcsv.CategoryCsvExporter;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.common.entity.Category;
@Controller
public class CategoryController {
	@Autowired
	private CategoryService service;
	
	@GetMapping("/categories")
	public String listFirstPage(@Param("sortDir") String sortDir,Model model) {		
		return listByPage(sortDir, model, 1,null);
	}
	
	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@Param("sortDir") String sortDir,Model model, @PathVariable("pageNum") Integer pageNum,@Param("keyword") String keyword) {		
		if(sortDir == null || sortDir.isEmpty()) {
			sortDir="asc";
		}
		CategoryPageInfo categoryPageInfo=new CategoryPageInfo();
		
		List<Category> listCategories=service.listByPage(categoryPageInfo, pageNum ,sortDir,keyword);
		
		long startCount = (pageNum - 1) * CategoryService.CATEGORIES_PER_PAGE + 1;
		long endCount = startCount + CategoryService.CATEGORIES_PER_PAGE - 1;
		
		if (endCount > categoryPageInfo.getTotalElements()) {
			endCount=categoryPageInfo.getTotalElements();
		}
		
		String reserveSortDir=sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("keyword",keyword);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalPages",categoryPageInfo.getTotalPages());
		model.addAttribute("totalItems",categoryPageInfo.getTotalElements());
		model.addAttribute("currentPage",pageNum);
		model.addAttribute("listCategories",listCategories);		
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("sortField", "name");
		model.addAttribute("reserveSortDir", reserveSortDir);
		return "categories/categories";
	}
	
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> listCategories=service.listCategoriesUsedInForm();
		
		model.addAttribute("pageTitle", "Create New Category");
		model.addAttribute("category",new Category());
		model.addAttribute("listCategories",listCategories);

		return "categories/category_form";
	}
	@PostMapping("/categories/save")
	public String saveCategory(Category category,RedirectAttributes redirectAttributes,
			@RequestParam("fileImage") MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName=StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			
			Category savedCategory=service.save(category);
			String uploadDir="../category-images/" + savedCategory.getId();
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			if(category.getImage()==null) category.setImage(null);
			service.save(category);
		}
		
		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");;

		return "redirect:/categories";
	}
	
	@GetMapping("/categories/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			List<Category> listCategories=service.listAll("asc");
			Category category=service.findById(id);
			
			model.addAttribute("category", category);
			model.addAttribute("pageTitle", "Edit Category (ID: " + id + ")");
			model.addAttribute("listCategories",listCategories);

			return "categories/category_form";
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			return "redirect:/categories";
		}
	}

	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateEnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled,
			RedirectAttributes redirectAttributes) {
		String status = enabled ? "enabled" : "disabled";
		String message = "The Category ID " + id + " has been " + status;
		service.updateCategoryEnabledStatus(id, enabled);
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			service.delete(id);
			redirectAttributes.addFlashAttribute("message", "The category ID " + id + " has been deleted successfully");
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());

		}
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<Category> listCategories=service.listCategoriesUsedInForm();
		CategoryCsvExporter exporter=new CategoryCsvExporter();
		exporter.export(listCategories, response);
		
	}
}
