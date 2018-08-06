/**
 * 
 */
package com.bronzeage.babq.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author andrew
 * 
 */
public class BabqBillingSpreadsheet {
	private static final String TEMPLATE_SHEET = "Template";
	private static final short FIRST_ROW_TO_FILL_IN = 7;
	private static final short MAX_ROWS_PER_SHEET = 12;

	private HSSFWorkbook wb_m;
	private int templateSheetIndex_m;
	private int currentSheetCounter_m;
	private int currentRecordInSheetCounter_m;
	private HSSFSheet currentSheet_m;
	private File spreadsheetName_m;
	private String lastDayOfMonth_m;
	private int recordsInSheet_m;
	private double totalInSheet_m;

	public BabqBillingSpreadsheet(File templateFile, File outputFile,
			Date lastBillingDate) throws IOException {
		spreadsheetName_m = outputFile;
		FileInputStream in = new FileInputStream(templateFile);
		wb_m = new HSSFWorkbook(in);
		templateSheetIndex_m = wb_m.getSheetIndex(TEMPLATE_SHEET);
		if (templateSheetIndex_m == -1)
			throw new IOException(
					"No sheet called \"Template\" found in billing template file "
							+ templateFile.getName());
		currentSheetCounter_m = 0;
		currentRecordInSheetCounter_m = Integer.MAX_VALUE;

		Calendar calExpiry = GregorianCalendar.getInstance();
		calExpiry.setTime(lastBillingDate);
		calExpiry.set(Calendar.DAY_OF_MONTH, calExpiry
				.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		SimpleDateFormat sf = new SimpleDateFormat("yyyy MM dd");

		lastDayOfMonth_m = sf.format(calExpiry.getTime());

		recordsInSheet_m = 0;
		totalInSheet_m = 0;
	}

	public void addRecord(String healthCardNum, Date expDate, String surname,
			String firstName, Date dateOfBirth, String sex, Date dateOfService,
			double costOfService, boolean usingParentNumber) {

		int limit = MAX_ROWS_PER_SHEET;
		if (usingParentNumber)
			limit = limit - 1; // Need to leave room for annotation

		if (currentRecordInSheetCounter_m >= limit) {
			if (currentSheetCounter_m > 0)
				touchUpCurrentSheetAfterDataInput();
			currentSheetCounter_m++;
			int currentNumberOfSheets = wb_m.getNumberOfSheets();
			currentSheet_m = wb_m.cloneSheet(templateSheetIndex_m);
			wb_m
					.setSheetName(currentNumberOfSheets, ""
							+ currentSheetCounter_m);
			currentRecordInSheetCounter_m = 0;

			// Fix date
			HSSFRow r = currentSheet_m.getRow(4);
			HSSFCell c = r.getCell((short) 8);
			c.setCellValue(new HSSFRichTextString("Period ending : "
					+ lastDayOfMonth_m));

		}

		HSSFRow r = currentSheet_m.getRow(currentRecordInSheetCounter_m
				+ FIRST_ROW_TO_FILL_IN);
		String healthCardNumWithSpaces = healthCardNum.substring(0, 4) + " "
				+ healthCardNum.substring(4, 8) + " "
				+ healthCardNum.substring(8, 12);
		r.getCell((short) 1).setCellValue(
				new HSSFRichTextString(healthCardNumWithSpaces));
		r.getCell((short) 2).setCellValue(expDate);
		r.getCell((short) 3).setCellValue(new HSSFRichTextString(surname));
		r.getCell((short) 4).setCellValue(new HSSFRichTextString(firstName));
		r.getCell((short) 5).setCellValue(dateOfBirth);
		r.getCell((short) 6).setCellValue(new HSSFRichTextString(sex));
		r.getCell((short) 7).setCellValue(dateOfService);
		r.getCell((short) 8).setCellValue(new HSSFRichTextString("01"));
		r.getCell((short) 10).setCellValue(costOfService);

		recordsInSheet_m++;
		totalInSheet_m += costOfService;
		currentRecordInSheetCounter_m++;
		if (usingParentNumber) {
			r = currentSheet_m.getRow(currentRecordInSheetCounter_m
					+ FIRST_ROW_TO_FILL_IN);
			r.getCell((short) 3).setCellValue(
					new HSSFRichTextString("Baby using parent number"));
			r.getCell((short) 10).setCellValue(new HSSFRichTextString(""));
			r.getCell((short) 8).setCellValue(new HSSFRichTextString(""));

			currentRecordInSheetCounter_m++;
		}
	}

	public void touchUpCurrentSheetAfterDataInput() {
		HSSFRow r = currentSheet_m.getRow(21);
		r.getCell((short) 3).setCellValue(recordsInSheet_m);
		r.getCell((short) 5).setCellValue(recordsInSheet_m);

		r = currentSheet_m.getRow(19);
		r.getCell((short) 10).setCellValue(totalInSheet_m);

		r = currentSheet_m.getRow(27);
		r.getCell((short) 5).setCellValue(recordsInSheet_m);

		// Clear the values
		totalInSheet_m = 0.0;
		recordsInSheet_m = 0;
	}

	public void closeAndSave() throws IOException {
		// Update the page counts on each page
		int pageCount = wb_m.getNumberOfSheets();
		for (int i = 0; i < pageCount; i++) {
			HSSFSheet s = wb_m.getSheetAt(i);
			s.getRow(3).getCell((short) 10)
					.setCellValue(
							new HSSFRichTextString("Page " + i + "/"
									+ (pageCount - 1)));
		}

		wb_m.removeSheetAt(templateSheetIndex_m);
		FileOutputStream out = new FileOutputStream(spreadsheetName_m);
		wb_m.write(out);
	}
}
