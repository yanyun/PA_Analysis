import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

class DoExcel {
	public Map<Integer, ArrayList<Object>> map;
	public ArrayList<String> titleList = new ArrayList<String>();
	
	public DoExcel(Map<Integer, ArrayList<Object>> map){
		this.map = map;
	}
	
	public void getTitle(ArrayList<String> titleList){
		this.titleList = titleList;
	}
	
	public void createExcel(){
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Test_01");
		
		//set Font Bold
		HSSFFont bold_font = workbook.createFont();
		bold_font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		HSSFCellStyle style_bold_left = workbook.createCellStyle();
		HSSFCellStyle style_bold_right = workbook.createCellStyle();
		HSSFCellStyle style_bold_lr = workbook.createCellStyle();
		//style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		style_bold_left.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
		style_bold_right.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		style_bold_lr.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
		style_bold_lr.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		//style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
	    sheet.createFreezePane(0,1);

		
		
		HSSFRow row0 = sheet.createRow(0);		
		int cellnum0 = 0;
		for(String str : titleList){
			HSSFCell cell = row0.createCell((short) cellnum0);
			/*HSSFCellStyle cellColor = workbook.createCellStyle();
			cellColor.setFillBackgroundColor(new HSSFColor.BLUE().getIndex());
			
			cell.setCellStyle(cellColor);*/
			cell.setCellValue(new HSSFRichTextString((String) str));
			if(cellnum0 > titleList.size() - 2){
				cell.setCellStyle(style_bold_right);
			}

			if(str == " "){
				cell.setCellStyle(style_bold_lr);
			}
			cellnum0++;
		}
		
		Set<Integer> keySet = map.keySet();
		int rownum = 1;
	    for(Object key : keySet){
	    	HSSFRow row = sheet.createRow(rownum++);
	    	ArrayList<Object> arr = map.get(key);
	    	int cellnum = 0;
	    	for(Object str : arr){
	    		if(str instanceof String){	    			
	    			HSSFCell cell = row.createCell((short) cellnum);
	    			cell.setCellValue(new HSSFRichTextString((String) str));
	    			if(cellnum > arr.size() - 2){
	    				cell.setCellStyle(style_bold_right);
	    			}

	    			if(str == " "){
	    				cell.setCellStyle(style_bold_lr);
	    			}
	    			
	    			cellnum++;
	    		}else if(str instanceof Integer){
	    			int a = (Integer) str;
	    			HSSFCell cell = row.createCell((short) cellnum);
	    			cell.setCellValue(a);
	    			if(cellnum > arr.size() - 2){
	    				cell.setCellStyle(style_bold_right);
	    			}
	    			
	    			cellnum++;
	    		}
	    	}
	    }
	    
	    for(int i = 0; i < titleList.size(); i++){
	    	sheet.autoSizeColumn((short) i);
	    }
	    

	    
	    
	    try{
	    	FileOutputStream fileOut =  new FileOutputStream("C:\\temp\\" + Parameters.secondJobId +".xls");
	        workbook.write(fileOut);
	        fileOut.close();
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
	
}
