package com.shopme.admin.exportcsv;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.admin.util.AbstractExporter;
import com.shopme.common.entity.Category;

public class CategoryCsvExporter extends AbstractExporter{
	
	public void export(List<Category> listCategories, HttpServletResponse response) throws IOException {
			
			super.setResponseHeader(response, "text/csv", ".csv", "categories_");
			
			ICsvBeanWriter csvWriter= new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			
			String[] csvHeader= {"CATEGORY ID","CATEGORY NAME"};
			String[] fieldMapping= {"id","name"};
				
	 		csvWriter.writeHeader(csvHeader);
	 		
	 		for(Category cat:listCategories) {
	 			cat.setName(cat.getName().replace("--","  "));
	 			csvWriter.write(cat, fieldMapping);
	 		}
			
			csvWriter.close();
		}
}
