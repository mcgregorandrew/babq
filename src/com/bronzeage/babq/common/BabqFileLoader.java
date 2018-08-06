/**
 * 
 */
package com.bronzeage.babq.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author andrew
 * 
 */
public class BabqFileLoader {

	protected int lineNumber_m;
	protected BufferedReader br_m;
	private DataInputStream in_m;
	protected IBabqProgress progressTracker_m;
	protected File file_m;
	protected boolean useTabs_m;

	public BabqFileLoader(File f, IBabqProgress progressTracker, boolean useTabs)
			throws FileNotFoundException {
		lineNumber_m = 0;
		FileInputStream fstream = new FileInputStream(f);
		// Get the object of DataInputStream
		in_m = new DataInputStream(fstream);
		br_m = new BufferedReader(new InputStreamReader(in_m));
		progressTracker_m = progressTracker;
		useTabs_m = useTabs;
		file_m = f;
	}

	public String[] readRecord() throws IOException {
		lineNumber_m++;
		String strLine;
		// Read File Line By Line
		strLine = br_m.readLine();

		if (((lineNumber_m % 100) == 0) && (progressTracker_m != null))
			progressTracker_m.setProgressString(String.format(
					"Processing record %06d in %s", lineNumber_m, file_m
							.getName()));

		if (strLine == null)
			return null;

		String[] retVal;
		if (useTabs_m)
			retVal = strLine.split("\t",-1);
		else
			retVal = strLine.split(",",-1);

		// Sometimes fields are quoted.  Remove all quotes
		if (strLine.contains("\""))
		{
			for (int i = 0; i < retVal.length; i++)
				retVal[i] = retVal[i].replace("\"", "");
		}
		return retVal;
	}

	public void close() throws IOException {
		// Close the input stream
		in_m.close();
	}

	public int getLineNumber() {
		return lineNumber_m;
	}
}
