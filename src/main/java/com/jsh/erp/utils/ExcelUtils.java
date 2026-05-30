package com.jsh.erp.utils;


/**
 * Excel 工具类
 * 提供 Excel 文件的导入导出功能，封装 POI 操作
 *
 * @author jishenghua
 */
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import jxl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import jxl.format.*;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ExcelUtils {

	public static InputStream getPathByFileName(String template, String tmpFileName) {
		File tmpFile = new File(template, tmpFileName);
		InputStream path = null;
		//鍒ゆ柇鏂囦欢鎴栨枃浠跺す鏄惁瀛樺湪
		if (tmpFile.exists()) {
			try {
				path = new FileInputStream(tmpFile);
			} catch (FileNotFoundException e) {
				log.error("", e);
			}
		}
		return path;
	}

	/**
	 * 瀵煎嚭excel锛屽甫澶歴heet
	 *
	 * @param wtwb
	 * @param tip
	 * @param names
	 * @param title
	 * @param index
	 * @param objects
	 * @return
	 * @throws Exception
	 */
	public static void exportObjectsManySheet(WritableWorkbook wtwb, String tip,
											  String[] names, String title, int index, List<String[]> objects) throws Exception {
		WritableSheet sheet = wtwb.createSheet(title, index);
		sheet.getSettings().setDefaultColumnWidth(12);

		// 鏍囬鐨勬牸寮?绾㈣壊
		WritableFont redWF = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		WritableCellFormat redWFFC = new WritableCellFormat(redWF);
		redWFFC.setVerticalAlignment(VerticalAlignment.CENTRE);
		redWFFC.setBorder(Border.ALL, BorderLineStyle.THIN);

		// 鏍囬鐨勬牸寮?榛戣壊
		WritableFont blackWF = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		WritableCellFormat blackWFFC = new WritableCellFormat(blackWF);
		blackWFFC.setVerticalAlignment(VerticalAlignment.CENTRE);
		blackWFFC.setBorder(Border.ALL, BorderLineStyle.THIN);

		// 璁剧疆瀛椾綋浠ュ強鍗曞厓鏍兼牸寮?		WritableFont wfont = new WritableFont(WritableFont.createFont("妤蜂功"), 12);
		WritableCellFormat format = new WritableCellFormat(wfont);
		format.setAlignment(Alignment.LEFT);
		format.setVerticalAlignment(VerticalAlignment.TOP);
		format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		// 绗竴琛屽啓鍏ユ彁绀?		if(com.jsh.erp.utils.StringUtil.isNotEmpty(tip) && tip.contains("*")) {
			sheet.addCell(new Label(0, 0, tip, redWFFC));
		} else {
			sheet.addCell(new Label(0, 0, tip, blackWFFC));
		}

		// 绗簩琛屽啓鍏ユ爣棰?		for (int i = 0; i < names.length; i++) {
			if(StringUtil.isNotEmpty(names[i]) && names[i].contains("*")) {
				sheet.addCell(new Label(i, 1, names[i], redWFFC));
			} else {
				sheet.addCell(new Label(i, 1, names[i], blackWFFC));
			}
		}

		// 鍏朵綑琛屼緷娆″啓鍏ユ暟鎹?		int rowNum = 2;
		for (int j = 0; j < objects.size(); j++) {
			String[] obj = objects.get(j);
			for (int h = 0; h < obj.length; h++) {
				sheet.addCell(new Label(h, rowNum, obj[h], format));
			}
			rowNum = rowNum + 1;
		}
	}

	/**
	 * 瀵煎嚭excel锛屽甫鍗曚釜sheet
	 *
	 * @param fileName
	 * @param names
	 * @param title
	 * @param objects
	 * @return
	 * @throws Exception
	 */

	public static File exportObjectsOneSheet(String fileName, String tip,
											 String[] names, String title, List<Object[]> objects) throws Exception {
		File excelFile = new File("/opt/"+ fileName);
		WritableWorkbook wtwb = Workbook.createWorkbook(excelFile);
		WritableSheet sheet = wtwb.createSheet(title, 0);
		sheet.getSettings().setDefaultColumnWidth(12);

		// 鏍囬鐨勬牸寮?绾㈣壊
		WritableFont redWF = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
		WritableCellFormat redWFFC = new WritableCellFormat(redWF);
		redWFFC.setVerticalAlignment(VerticalAlignment.CENTRE);
		redWFFC.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		// 鏍囬鐨勬牸寮?榛戣壊
		WritableFont blackWF = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
		WritableCellFormat blackWFFC = new WritableCellFormat(blackWF);
		blackWFFC.setVerticalAlignment(VerticalAlignment.CENTRE);
		blackWFFC.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		// 璁剧疆瀛椾綋浠ュ強鍗曞厓鏍兼牸寮?		WritableFont wfont = new WritableFont(WritableFont.createFont("妤蜂功"), 12);
		WritableCellFormat format = new WritableCellFormat(wfont);
		format.setAlignment(Alignment.LEFT);
		format.setVerticalAlignment(VerticalAlignment.TOP);
		format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		// 绗竴琛屽啓鍏ユ彁绀?		if(StringUtil.isNotEmpty(tip) && tip.contains("*")) {
			sheet.addCell(new Label(0, 0, tip, redWFFC));
		} else {
			sheet.addCell(new Label(0, 0, tip, blackWFFC));
		}

		// 绗簩琛屽啓鍏ユ爣棰?		for (int i = 0; i < names.length; i++) {
			if(StringUtil.isNotEmpty(names[i]) && names[i].contains("*")) {
				sheet.addCell(new Label(i, 1, names[i], redWFFC));
			} else {
				sheet.addCell(new Label(i, 1, names[i], blackWFFC));
			}
		}

		// 鍏朵綑琛屼緷娆″啓鍏ユ暟鎹?		int rowNum = 2;
		for (int j = 0; j < objects.size(); j++) {
			Object[] obj = objects.get(j);
			for (int h = 0; h < obj.length; h++) {
				if(obj[h] instanceof String) {
					sheet.addCell(new Label(h, rowNum, obj[h].toString(), format));
				} else if(obj[h] instanceof BigDecimal || obj[h] instanceof Double || obj[h] instanceof Integer || obj[h] instanceof Long) {
					sheet.addCell(new jxl.write.Number(h, rowNum, Double.parseDouble(obj[h].toString()), format));
				} else {
					String cont = obj[h]!=null?obj[h].toString():"";
					sheet.addCell(new Label(h, rowNum, cont, format));
				}
			}
			rowNum = rowNum + 1;
		}
		wtwb.write();
		wtwb.close();
		return excelFile;
	}

	public static String getContent(Sheet src, int rowNum, int colNum) {
		if(colNum < src.getRow(rowNum).length) {
			return src.getRow(rowNum)[colNum].getContents().trim();
		} else {
			return null;
		}
	}

	public static String getContentNumber(Sheet src, int rowNum, int colNum) {
		if(colNum < src.getRow(rowNum).length) {
			Cell cell = src.getCell(colNum, rowNum);
			if(cell.getType() == CellType.NUMBER) {
				NumberCell numCell = (NumberCell)cell;
				double value = numCell.getValue(); // 鑾峰彇瀹屾暣绮惧害鐨勬暟鍊?				DecimalFormat df = new DecimalFormat("#.######"); // 璁剧疆瓒冲澶氱殑灏忔暟浣?				return df.format(value);
			} else {
				return cell.getContents().trim(); // 鑾峰彇鍘熷瀛楃涓插唴瀹?			}
		} else {
			return null;
		}
	}

	/**
	 * 鑾峰彇鐪熷疄鐨勮鏁帮紝鍓旈櫎鎺夌┖鐧借
	 * @param src
	 * @return
	 */
	public static int getRightRows(Sheet src) {
		int rsRows = src.getRows(); //琛屾暟
		int rsCols = src.getColumns(); //鍒楁暟
		int nullCellNum;
		int rightRows = rsRows;
		for (int i = 1; i < rsRows; i++) { //缁熻琛屼腑涓虹┖鐨勫崟鍏冩牸鏁?			nullCellNum = 0;
			for (int j = 0; j < rsCols; j++) {
				String val = src.getCell(j, i).getContents().trim();
				if (StringUtils.isEmpty(val)) {
					nullCellNum++;
				}
			}
			if (nullCellNum >= rsCols) { //濡傛灉nullCellNum澶т簬鎴栫瓑浜庢€荤殑鍒楁暟
				rightRows--; //琛屾暟鍑忎竴
			}
		}
		return rightRows;
	}

	public static void downloadExcel(File excelFile, String fileName, HttpServletResponse response) throws Exception{
		response.setContentType("application/octet-stream");
		fileName = new String(fileName.getBytes("gbk"),"ISO8859_1");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + ".xls" + "\"");
		FileInputStream fis = new FileInputStream(excelFile);
		OutputStream out = response.getOutputStream();

		int SIZE = 1024 * 1024;
		byte[] bytes = new byte[SIZE];
		int LENGTH = -1;
		while((LENGTH = fis.read(bytes)) != -1){
			out.write(bytes,0,LENGTH);
		}
		out.flush();
		fis.close();
	}
}
