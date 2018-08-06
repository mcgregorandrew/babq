/**
 * 
 */
package com.bronzeage.babq.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author andrew
 */
public class BabqWarningList {

	public class WarningComparator implements Comparator<BabqLogLine> {

		public int compare(BabqLogLine o1, BabqLogLine o2) {
			int result = o1.fileName_m.compareTo(o2.fileName_m);
			if (result != 0) {
				return result;
			}

			result = o1.lineNumber_m - o2.lineNumber_m;
			if (result != 0) {
				return result;
			}

			return o1.description_m.compareTo(o2.description_m);
		}

	}

	public static class BabqLogLine {
		String fileName_m;
		int lineNumber_m;
		String description_m;
		boolean warning_m;

		public BabqLogLine(String fileName_m, int lineNumber_m,
				String description, boolean warning) {
			this.fileName_m = fileName_m;
			this.lineNumber_m = lineNumber_m;
			this.description_m = description;
			warning_m = warning;
		}

	}

	private ArrayList<BabqLogLine> warningList_m;
	private String defaultFile_m = "noFile";
	private static String eol = String.format("%n");
	private int maxErrors_m = 300;

	public BabqWarningList() {
		warningList_m = new ArrayList<BabqLogLine>();
	}

	public void addWarning(String file, int lineNumber, String warningText) {
		if (warningList_m.size() >= maxErrors_m) {
			if (warningList_m.size() == maxErrors_m) {
				warningList_m
						.add(new BabqLogLine(
								file,
								lineNumber,
								"Too many errors.  Supressing further errors...",
								true));
			}
		} else {
			warningList_m.add(new BabqLogLine(file, lineNumber, warningText,
					true));
		}
	}

	public void addLog(String file, int lineNumber, String logText) {
		if (warningList_m.size() >= maxErrors_m) {
			if (warningList_m.size() == maxErrors_m) {
				warningList_m
						.add(new BabqLogLine(
								file,
								lineNumber,
								"Too many errors.  Supressing further errors...",
								true));
			}
		} else {
			warningList_m
					.add(new BabqLogLine(file, lineNumber, logText, false));
		}
	}

	public void addWarning(int lineNumber, String warningText) {
		addWarning(defaultFile_m, lineNumber, warningText);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		WarningComparator warningComparator = new WarningComparator();
		Collections.sort(warningList_m, warningComparator);
		BabqLogLine prevWarn = null;

		for (BabqLogLine w : warningList_m) {

			if ((prevWarn == null)
					|| (warningComparator.compare(w, prevWarn) != 0)) {
				sb.append(w.fileName_m);
				if (w.lineNumber_m != -1)
					sb.append(":" + w.lineNumber_m);
				sb.append(" - " + w.description_m);
				sb.append(eol);
			}
			prevWarn = w;
		}
		return sb.toString();
	}

	public void setFile(String fileName) {
		defaultFile_m = fileName;
	}

	public void writeWarningsToFile(File fileToWrite) throws IOException {
		fileToWrite.delete();
		if (!fileToWrite.getParentFile().isDirectory())
			fileToWrite.getParentFile().mkdirs();
		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(fileToWrite));
		PrintWriter pw = new PrintWriter(os);
		pw.println(toString());
		pw.close();
		os.close();
	}

	public void clear() {
		warningList_m.clear();
	}

	public void addExc(Throwable e1) {
		StringBuilder sb = new StringBuilder(e1.toString());
		sb.append(eol);
		StackTraceElement[] trace = e1.getStackTrace();
		for (StackTraceElement e : trace)
			sb.append("   " + e.getFileName() + ":" + e.getLineNumber() + " "
					+ e.getClassName() + "." + e.getMethodName() + eol);
		sb.append("   Trace back done" + eol);

		addWarning(" Exception:", -1, sb.toString());
	}

	public int getWarningCount() {
		int count = 0;
		for (BabqLogLine w : warningList_m) {
			if (w.warning_m)
				count++;
		}
		return count;
	}

	public int getLineCount() {
		return warningList_m.size();
	}
}