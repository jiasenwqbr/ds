package com.jason.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;
import org.springframework.util.StringUtils;

public class ExcelParser {

	public static final String EXTENSION_XLS = "xls";
	public static final String EXTENSION_XLSX = "xlsx";

	public  List<String> getSheets(Workbook workbook){
		int sheetNo = workbook.getNumberOfSheets();
		List<String> sheetnames = new ArrayList<String>();
		for (int i=0;i<sheetNo;i++){
			sheetnames.add(workbook.getSheetName(i));
		}
		return sheetnames;
	}
	

	public List<String> getHeaders(Workbook workbook, String sheetName) {
		List<String> headers = new ArrayList<String>();
		// 获取workbook对象
		// 读文件 一个sheet一个sheet地读取
		// for (int numSheet = 0; numSheet < workbook.getNumberOfSheets();
		// numSheet++) {
		Sheet sheet = null;
		if (!StringUtils.isEmpty(sheetName)) {
			sheet = workbook.getSheet(sheetName);

		} else {
			sheet = workbook.getSheetAt(0);
		}
		if (sheet == null) {
			return null;
		}

		int firstRowIndex = sheet.getFirstRowNum();
		int lastRowIndex = sheet.getLastRowNum();
		// sheet页如果没有内容跳过
		if (0 == lastRowIndex) {
			return null;
		}
		// 读取首行 即,表头
		Row firstRow = sheet.getRow(firstRowIndex);
		for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
			Cell cell = firstRow.getCell(i);
			String cellValue = this.getCellValue(cell);
			headers.add(cellValue);
		}
		return headers;
	}
    
    /**
     * 读取excel文件内容
     * @param file
     * @throws FileNotFoundException
     * @throws FileFormatException
     */
    public List<Map<String,String>> readExcel(Workbook workbook,String sheetName) throws FileNotFoundException, FileFormatException {

        // 获取workbook对象
        List<Map<String,String>> list = null;
//        InputStream is = null;
        try {
//        	is = new FileInputStream(file);
//            if (file.getName().endsWith(EXTENSION_XLS)) {
//                workbook = new HSSFWorkbook(is);
//            } else if (file.getName().endsWith(EXTENSION_XLSX)) {
//                workbook = new XSSFWorkbook(is);
//            }
            // 读文件 一个sheet一个sheet地读取
//            for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
        	Sheet sheet = null;
        	if (!StringUtils.isEmpty(sheetName)) {
    			sheet = workbook.getSheet(sheetName);

    		} else {
    			sheet = workbook.getSheetAt(0);
    		}
            if (sheet == null) {
                return null;
            }

            int firstRowIndex = sheet.getFirstRowNum();
            int lastRowIndex = sheet.getLastRowNum();
            // sheet页如果没有内容跳过
            if (0 == lastRowIndex) {
                return null;
            }
            List<String> headers = new ArrayList<String>();
            // 读取首行 即,表头
            Row firstRow = sheet.getRow(firstRowIndex);
            for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
                Cell cell = firstRow.getCell(i);
                String cellValue = this.getCellValue(cell);
                headers.add(cellValue);
            }


            // 读取数据行
            list = new ArrayList<>();
            for (int rowIndex = firstRowIndex + 1; rowIndex <= lastRowIndex; rowIndex++) {
                Row currentRow = sheet.getRow(rowIndex);// 当前行
                if (currentRow == null) {
                    return null;
                }

                Map<String,String> map = new HashMap<String,String>();
                for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                    if(!"".equals(headers.get(columnIndex))){
                        Cell currentCell = currentRow.getCell(columnIndex);// 当前单元格
                        String currentCellValue = this.getCellValue(currentCell);// 当前单元格的值
                        if(!"".equals(currentCellValue)){
                            map.put(headers.get(columnIndex), currentCellValue);
                        }
                    }

                }
                if(!map.isEmpty()){
                    list.add(map);
                }

            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 取单元格的值
     * @param cell 单元格对象
     * @return
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");

        String cellValue = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_STRING: // 文本
                cellValue = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC: // 数字、日期
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = fmt.format(cell.getDateCellValue()); // 日期型
                } else {
                    cellValue = String.valueOf(cell.getNumericCellValue()); // 数字
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN: // 布尔型
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_BLANK: // 空白
                cellValue = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_ERROR: // 错误
                cellValue = "错误";
                break;
            case Cell.CELL_TYPE_FORMULA: // 公式
                try {
		            /*
		             * 此处判断使用公式生成的字符串有问题，因为HSSFDateUtil.isCellDateFormatted(cell)判断过程中cell
		             * .getNumericCellValue();方法会抛出java.lang.NumberFormatException异常
		             */
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 公式类型中的内容为日期类型的转为yyyy/MM/dd
                        Date date = cell.getDateCellValue();
                        cellValue = (date.getYear() + 1900) + "/" + (date.getMonth() + 1) +"/" + date.getDate();
                        break;
                    } else {
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                } catch (IllegalStateException e) {
                    cellValue = String.valueOf(cell.getRichStringCellValue());
                }
                break;
            default:
                cellValue = "错误";
        }
        return cellValue;
    }
    
}
