package com.bronzeage.babq.formatter;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Since version 6.0. Smart date parsing.
 * @author WangF
 *
 */
public class DateFormatter {
	
	static Logger logger_m = Logger.getLogger(DateFormatter.class.getPackage()
			.getName());
	
	private static List<SimpleDateFormat> dateFormatters = Arrays.asList(new SimpleDateFormat("MMM d, yyyy"), 
			new SimpleDateFormat("MMM d yyyy"),
			new SimpleDateFormat("dd-MMM-yy"), 
			new SimpleDateFormat("dd-MMM-yyyy"),
			new SimpleDateFormat("MM/dd/yyyy"),
			new SimpleDateFormat("yyyy-MM-dd"));
	
	//static DateFormat formatter_ms = new SimpleDateFormat("MMM d, yyyy");
	
	public static Date stringToDate(String string) throws ParseException {
		if ((string == null) || (string.length() < 3)) {
			return null;
		}
			
		if (string.equals("n/a")) {
			return null;
		}
		
		java.util.Date date = null;
		for (SimpleDateFormat formatter : dateFormatters) {
			if (date != null) {
				break;
			}
			
			try {
				date = formatter.parse(string);
			} catch (ParseException e) {
				
			}
			
		}
		if (date == null) {
			throw new ParseException("Unparseable date <"+string+">", 0);
		}
		return new Date(date.getTime());
	}

}
