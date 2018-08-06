/**
 * 
 */
package com.bronzeage.babq.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author andrew
 * 
 */
public class BabqUtils {

	public static void copyFile(File oldFile, File newFile)
	// TODO Auto-generated method stub
			throws IOException {
		final FileInputStream fis = new FileInputStream(oldFile);
		try {
			final FileOutputStream fos = new FileOutputStream(newFile);
			try {
				copyStream(fis, fos);
			} finally {
				fos.close();
			}
		} finally {
			fis.close();
		}
	}

	public static boolean isLastDayOfMonth(long timeInMillis) {
		if (timeInMillis == BabqConfig.getSupressExpTimeInMillis())
			return true;

		Calendar calExpiry = GregorianCalendar.getInstance();
		calExpiry.setTimeInMillis(timeInMillis);
		return calExpiry.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) == calExpiry
				.get(GregorianCalendar.DAY_OF_MONTH);
	}

	public static long getDateAtLastDayOfMonth(long timeInMillis) {
		Calendar calExpiry = GregorianCalendar.getInstance();
		calExpiry.setTimeInMillis(timeInMillis);
		calExpiry.set(GregorianCalendar.DAY_OF_MONTH, calExpiry
				.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
		return calExpiry.getTimeInMillis();
	}

	public static void copyStream(final InputStream inStream,
			final OutputStream outStream) throws IOException {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(inStream);
			bos = new BufferedOutputStream(outStream);

			final byte[] buf = new byte[1024];
			int numRead = 0;
			while ((numRead = bis.read(buf)) != -1) {
				bos.write(buf, 0, numRead);
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}

	public static void copyFromResource(String resourceName, File localFile)
			throws Exception {
		final InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resourceName);
		if (in == null)
			throw new Exception("Resource " + resourceName
					+ " not found - contact support");
		final FileOutputStream fout = new FileOutputStream(localFile);
		int b;
		while ((b = in.read()) != -1) {
			fout.write(b);
		}
		fout.close();
		in.close();

	}

}
