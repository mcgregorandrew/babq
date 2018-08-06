/**
 * 
 */
package com.bronzeage.babq.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.bronzeage.babq.common.BabqUtils;

/**
 * @author andrew
 * 
 */
public class BabqTeamRoleSummarySpreadsheet {
	private static final String TEMPLATE_SHEET = "Template";

	private HSSFWorkbook wb_m;
	private int templateSheetIndex_m;
	private HSSFSheet currentSheet_m;
	private File spreadsheetName_m;
	private File prevSpreadsheetName_m;
	private Map<String, Integer> teamToRowMap_m = new HashMap<String, Integer>();
	private int rowForNextTeam_m;

	public BabqTeamRoleSummarySpreadsheet(File spreadsheetName,
			String sheetName, String resourceName) throws Exception {
		spreadsheetName_m = spreadsheetName;
		prevSpreadsheetName_m = new File(spreadsheetName.getAbsolutePath()
				.replace(".xls", "-prev.xls"));
		if (!spreadsheetName.exists()) {
			if (!prevSpreadsheetName_m.exists())
				BabqUtils.copyFromResource(resourceName, spreadsheetName);
			else
				BabqUtils.copyFile(prevSpreadsheetName_m, spreadsheetName);
		}

		prevSpreadsheetName_m.delete();
		spreadsheetName_m.renameTo(prevSpreadsheetName_m);

		// Open the previous spreadsheet
		FileInputStream in = new FileInputStream(prevSpreadsheetName_m);
		wb_m = new HSSFWorkbook(in);
		templateSheetIndex_m = wb_m.getSheetIndex(TEMPLATE_SHEET);
		if (templateSheetIndex_m == -1)
			throw new IOException(
					"No sheet called \"Template\" found in billing template file "
							+ spreadsheetName.getName());

		int prevIndex = wb_m.getSheetIndex(sheetName);
		if (prevIndex != -1)
			wb_m.removeSheetAt(prevIndex);

		currentSheet_m = wb_m.cloneSheet(templateSheetIndex_m);
		wb_m.setSheetName(wb_m.getSheetIndex(currentSheet_m), sheetName);

		rowForNextTeam_m = 0;
		boolean done = false;
		do {
			HSSFRow r = currentSheet_m.getRow(rowForNextTeam_m);
			if (r == null)
				done = true;
			else {
				HSSFCell c = r.getCell((short) 0);
				if ((c == null)
						|| ((c.getCellType() == HSSFCell.CELL_TYPE_BLANK) || (c
								.getCellType() == HSSFCell.CELL_TYPE_STRING)
								&& (c.getRichStringCellValue().toString()
										.trim().length() == 0)))
					done = true;
				else
					rowForNextTeam_m++;
			}
		} while (!done);

	}

	public void addTeam(String teamName, boolean intValue) {
		teamToRowMap_m.put(teamName, rowForNextTeam_m);
		HSSFRow r = currentSheet_m.getRow(rowForNextTeam_m);
		if (r == null)
			r = currentSheet_m.createRow(rowForNextTeam_m);

		HSSFCell c = r.getCell((short) 0);
		if (c == null)
			c = r.createCell((short) 0);

		if (intValue)
			c.setCellValue(Integer.parseInt(teamName));
		else
			c.setCellValue(new HSSFRichTextString(teamName));

		rowForNextTeam_m++;
	}

	public void addCount(String teamName, int column, int value)
			throws Exception {
		HSSFCell c = getCell(teamToRowMap_m.get(teamName), column);

		c.setCellValue(value);
	}
	

	public void addDouble(String teamName, int column, double d) throws Exception {
		HSSFCell c = getCell(teamToRowMap_m.get(teamName), column);

		c.setCellValue(d);
	
	}

	public void addString(String teamName, int column, String value)
			throws Exception {
		HSSFCell c = getCell(teamToRowMap_m.get(teamName), column);

		c.setCellValue(new HSSFRichTextString(value));
	}

	public void setCellText(int row, int column, String value) throws Exception {
		getCell(row, column).setCellValue(new HSSFRichTextString(value));
	}

	private HSSFCell getCell(Integer row, int column) throws Exception {

		if (row == null)
			throw new Exception("Row for team not found");
		HSSFRow r = currentSheet_m.getRow(row);
		if (r == null)
			r = currentSheet_m.createRow(row);

		HSSFCell c = r.getCell((short) column);
		if (c == null)
			c = r.createCell((short) column);
		return c;
	}

	public void closeAndSave() throws IOException {
		for (int i = wb_m.getNumberOfSheets() - 1; i >= 0; i--) {

			String sheetName = wb_m.getSheetName(i);
			if ((sheetName != null) && sheetName.contains("Sheet"))
				wb_m.removeSheetAt(i);
		}
		FileOutputStream out = new FileOutputStream(spreadsheetName_m);
		wb_m.write(out);
	}

}
