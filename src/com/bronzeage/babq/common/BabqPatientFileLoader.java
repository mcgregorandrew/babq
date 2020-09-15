package com.bronzeage.babq.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * @author Wang
 * 
 */
public class BabqPatientFileLoader extends BabqFileLoader {

	public BabqPatientFileLoader(File f, IBabqProgress progressTracker,
			boolean useTabs) throws FileNotFoundException {
		super(f, progressTracker, useTabs);
	}

	@Override
	public String[] readRecord() throws IOException {
		lineNumber_m++;
		String strLine = null;

		boolean isValidLine = false;

		String[] retVal = null;
		// Read File Line By Line. If it hits invalid line, keep reading.
		while (!isValidLine) {
			strLine = br_m.readLine();
			if (((lineNumber_m % 100) == 0) && (progressTracker_m != null))
				progressTracker_m.setProgressString(String.format(
						"Processing record %06d in %s", lineNumber_m, file_m
								.getName()));

			if (strLine == null)
				return null;

			if (useTabs_m)
				retVal = strLine.split("\t", -1);
			else
				retVal = strLine.split(",", -1);

			// Sometimes fields are quoted. Remove all quotes
			if (strLine.contains("\"")) {
				for (int i = 0; i < retVal.length; i++)
					retVal[i] = retVal[i].replace("\"", "");
			}
			if (retVal[0] != null && !retVal[0].isEmpty()) {
				try {
					Integer.parseInt(retVal[0]);
					isValidLine = true;
				} catch (Exception e) {

				}

			}

		}
		
		return retVal;

	}

}
