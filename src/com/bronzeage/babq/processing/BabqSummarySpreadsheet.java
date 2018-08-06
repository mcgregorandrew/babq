/**
 * 
 */
package com.bronzeage.babq.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.bronzeage.babq.common.BabqUtils;

/**
 * @author andrew
 * 
 */
public class BabqSummarySpreadsheet {

	private static final String SUMMARY_SHEET = "SummarySheet";
	private File spreadsheetName_m;
	private File prevSpreadsheetName_m;

	public BabqSummarySpreadsheet(File spreadsheetName) throws IOException {
		spreadsheetName_m = spreadsheetName;
		prevSpreadsheetName_m = new File(spreadsheetName.getAbsolutePath()
				.replace(".xls", "-prev.xls"));
		if (!spreadsheetName.exists()) {
			if (!prevSpreadsheetName_m.exists())
				makeNewFile(spreadsheetName);
			else
				BabqUtils.copyFile(prevSpreadsheetName_m, spreadsheetName);
		}

	}

	private void makeNewFile(File spreadsheetName) throws IOException {
		FileOutputStream out = new FileOutputStream(spreadsheetName);
		// create a new workbook
		HSSFWorkbook wb = new HSSFWorkbook();
		// create a new sheet
		HSSFSheet s = wb.createSheet();
		// declare a row object reference
		HSSFRow r = null;
		// declare a cell object reference
		HSSFCell c = null;
		wb.setSheetName(0, SUMMARY_SHEET);
		r = s.createRow(0);
		HSSFFont f = wb.createFont();
		f.setColor((short) 0xc);
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		String[] headings = new String[] { "Date of Run", "Min Appt Date",
				"Max Appt Date", "Bruyere Visits", "Primrose Visits",
				"Allied Health Visits", "QC Bruyere Visits",
				"QC Primrose Visits", "QC Allied Health Visits", "EBxAH",
				"PxAH" };
		short[] widths = new short[] { 4000, 4000, 4000, 4000, 4000, 5000,
				5000, 5000, 6000, 4000, 4000 };
		for (short i = 0; i < headings.length; i++) {
			s.setColumnWidth(i, widths[i]);
			c = r.createCell(i);

			HSSFCellStyle cs = wb.createCellStyle();
			cs.setFont(f);
			cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
			c.setCellValue(new HSSFRichTextString(headings[i]));
			c.setCellStyle(cs);
		}

		wb.write(out);
		out.close();
	}

	void updateSpreadsheet(Date minDate, Date maxDate, int bruyereVisits,
			int primroseVisits, int alliedHealthVisits, int qcBruyereVisits,
			int qcPrimroseVisits, int qcAlliedHealthVisits, int qcBruyereInclAh, int qcPrimroseInclAh) throws IOException {
		// Move previous spreadsheet to old
		prevSpreadsheetName_m.delete();
		spreadsheetName_m.renameTo(prevSpreadsheetName_m);

		// Open the previous spreadsheet
		FileInputStream in = new FileInputStream(prevSpreadsheetName_m);
		// create a new workbook
		HSSFWorkbook wb = new HSSFWorkbook(in);
		// declare a row object reference
		HSSFSheet s = wb.getSheet(SUMMARY_SHEET);

		HSSFRow r = null;
		HSSFCell c = null;

		short rowToUse = 0; // skips row 1 with headers via ++ below
		boolean found = false;
		do {
			rowToUse++;
			r = s.getRow(rowToUse);
			if (r != null) {
				c = r.getCell((short) 0);
				if (c == null)
					found = true;
				else {
					Date d = c.getDateCellValue();
					if (d == null)
						found = true;
				}
			} else
				found = true;
		} while (!found);

		// Add the new row
		r = s.createRow(rowToUse);

		short column = 0;

		// Put data in the new row
		c = r.createCell(column++);
		// we style the second cell as a date (and time). It is important to
		// create a new cell style from the workbook otherwise you can end up
		// modifying the built in style and effecting not only this cell but
		// other cells.
		// HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFCellStyle cellStyle2 = wb.createCellStyle();
		HSSFDataFormat fmt = wb.createDataFormat();

		// This did not work in Excel
		// cellStyle.setDataFormat(fmt.getFormat("NNNNMMMM DD, YYYY"));
		cellStyle2.setDataFormat(fmt.getFormat("YYYY-mm-DD"));
		c.setCellValue(new Date());
		c.setCellStyle(cellStyle2);

		c = r.createCell(column++);
		c.setCellValue(minDate);
		c.setCellStyle(cellStyle2);

		c = r.createCell(column++);
		c.setCellValue(maxDate);
		c.setCellStyle(cellStyle2);

		c = r.createCell(column++);
		c.setCellValue(bruyereVisits);
		c = r.createCell(column++);
		c.setCellValue(primroseVisits);
		c = r.createCell(column++);
		c.setCellValue(alliedHealthVisits);


		c = r.createCell(column++);
		c.setCellValue(qcBruyereVisits);
		c = r.createCell(column++);
		c.setCellValue(qcPrimroseVisits);
		c = r.createCell(column++);
		c.setCellValue(qcAlliedHealthVisits);

		// Create 2 columns with all QC visits assigned to Bruyere or Primrose
		// (unspecified allied health visits are assigned proportionally
		// to each clinic).
		c = r.createCell(column++);
		c.setCellValue(qcBruyereInclAh);
		c = r.createCell(column++);
		c.setCellValue(qcPrimroseInclAh);

		// Close and save
		FileOutputStream out = new FileOutputStream(spreadsheetName_m);
		wb.write(out);
		out.close();
	}
}
