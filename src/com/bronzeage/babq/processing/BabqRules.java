/**
 * 
 */
package com.bronzeage.babq.processing;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.common.BabqUtils;
import com.bronzeage.babq.common.BabqWarningList;

/**
 * @author andrew
 * 
 */
public class BabqRules {

	public enum BabqValErrorType {
		NO_ERROR, DATE_ERROR, NAME_ERROR, OTHER_ERROR,
	}

	static Pattern healthNumPatter_m = Pattern.compile("[A-Z]{4}[0-9]{8}?");

	public static boolean validateBasicFields(String sourceFile,
			int patientLineNumber, String healthNumber, String surname,
			String firstName, String province, String sex, Date dateOfBirth,
			BabqWarningList warningList, Integer patientNum) {

		boolean recordOk = true;
		// Skip non-Quebec records
		if (!province.equals("QC"))
			return true;

		if (healthNumber == null) {
			warningList.addWarning(sourceFile, patientLineNumber,
					"PatientNum: " + patientNum //
							+ " Health number not present");
			return false;
		}

		// Check health number size
		if (healthNumber.length() > 0) {
			if (healthNumber.length() < 12) {
				warningList
						.addWarning(
								sourceFile,
								patientLineNumber,
								"PatientNum: "
										+ patientNum //
										+ " Health number must have 12 character, found: "
										+ healthNumber);
				return false;
			}

			// Check that the first 4 characters are all letters and the
			// next 8 are all numbers
			if (!healthNumPatter_m.matcher(healthNumber).matches()) {
				warningList
						.addWarning(
								sourceFile,
								patientLineNumber,
								"PatientNum: "
										+ patientNum //
										+ " Health number must be 4 letters followed by 8 numbers."
										+ "  Found : " + healthNumber);
				return false;
			}
		}
		return recordOk;

	}

	public static boolean validateExpiryDate(String sourceFile,
			int patientLineNumber, Date dateOfExpiry, Date dateOfService,
			BabqWarningList warningList, Integer patientNum) {
		boolean recordOk = true;
		if (!BabqConfig.ignoreExpiryDates()) {
			if (dateOfExpiry == null) {
				warningList.addWarning(sourceFile, patientLineNumber,
						"PatientNum: " + patientNum //
								+ " Expiry date is not valid.");
				return false;
			}
			long expTimeLong = dateOfExpiry.getTime();

			// Check that date of service is less than date of expiry
			if (dateOfService.getTime() > dateOfExpiry.getTime()) {
				warningList
						.addWarning(
								sourceFile,
								patientLineNumber,
								"PatientNum: "
										+ patientNum //
										+ " Expiry date must be after the date of service.  dateOfExpiry is: "
										+ dateOfExpiry + " dateOfService is: "
										+ dateOfService);
				recordOk = false;
			}

			// Check - is expiry date last day in the month
			if (!BabqUtils.isLastDayOfMonth(expTimeLong)) {
				warningList
						.addWarning(
								sourceFile,
								patientLineNumber,
								"PatientNum: "
										+ patientNum //
										+ " Expiry date must be the last day in the month.  It is: "
										+ new Date(expTimeLong));
				recordOk = false;
			}
		}
		return recordOk;
	}

	public static BabqValErrorType validateHealthNum(String sourceFile,
			int patientLineNumber, String healthNumber, String surname,
			String firstName, String sex, Date dateOfBirth,
			boolean okToUseParentHealthNum, BabqWarningList warningList,
			Integer patientNum) {
		try {
			System.out.println("--------validateHealthNum ");
			BabqValErrorType recordOk = BabqValErrorType.NO_ERROR;
			
			// Trim names in case they have leading spaces
			surname = surname.trim();
			firstName = firstName.trim();
			
			if (healthNumber.length() == 0)
				return BabqValErrorType.OTHER_ERROR;

			// Check - is name part of number right
			String namePart = null;
			// Remove ' characters (e.g. O'Malley)
			String filteredSurname = surname.replaceAll("[\' \\-]", "");
			// For short names extend with X's
			if (filteredSurname.length() == 1)
				namePart = filteredSurname + "XX";
			else if (filteredSurname.length() == 2)
				namePart = filteredSurname + "X";
			else
				namePart = filteredSurname.substring(0, 3);

			// Now add first letter of first name
			System.out.println("--------firstName "+firstName);
			namePart += firstName.substring(0, 1);
			namePart = namePart.toUpperCase();
			if (!namePart.equals(healthNumber.substring(0, 4))) {
				if (!okToUseParentHealthNum)
					warningList
							.addWarning(
									sourceFile,
									patientLineNumber,
									"PatientNum: "
											+ patientNum //
											+ " Surname/firstname part of health card number is wrong.  Expected(from name): "
											+ namePart + " but found(from health card): "
											+ healthNumber.substring(0, 4));
				recordOk = BabqValErrorType.NAME_ERROR;
			}
			

			// Check - is birth date part of number right (incl sex)
			Calendar calDateOfBirth = Calendar.getInstance();
			calDateOfBirth.setTime(dateOfBirth);
			int monthOfBirth = calDateOfBirth.get(Calendar.MONTH) + 1
					+ (sex.equals("F") ? 50 : 0);
			String dateCode = String.format("%02d%02d%02d", calDateOfBirth
					.get(Calendar.YEAR) % 100, monthOfBirth, calDateOfBirth
					.get(Calendar.DAY_OF_MONTH));
			System.out.println("--------dateCode "+dateCode);
			if (!dateCode.equals(healthNumber.substring(4, 10))) {
				if (!okToUseParentHealthNum)
					warningList
							.addWarning(
									sourceFile,
									patientLineNumber,
									"PatientNum: "
											+ patientNum //
											+ " Date/gender part of health card number is wrong.  Expected: "
											+ dateCode + " but found: "
											+ healthNumber.substring(4, 10));
				recordOk = BabqValErrorType.DATE_ERROR;
			}
			return recordOk;
		} catch (Throwable t) {
			if (!okToUseParentHealthNum)
				warningList
						.addWarning(sourceFile, patientLineNumber,
								"PatientNum: "
										+ patientNum //
										+ " Other error in health card number:"
										+ healthNumber + " Error info: "
										+ t.toString());
			return BabqValErrorType.OTHER_ERROR;
		}
	}
 
	public static boolean isAllowedToUseParentHealthNum(Date dateOfService,
			Date dateOfBirth, String healthNumber) {
		// Check - is it a baby (less than 1 year of age skips checks because it
		// may be using the parent's card)
		Calendar calDateOfBirthPlus1Years = GregorianCalendar.getInstance();
		calDateOfBirthPlus1Years.setTime(dateOfBirth);
		calDateOfBirthPlus1Years.add(Calendar.YEAR, 1);
		if (calDateOfBirthPlus1Years.getTimeInMillis() > dateOfService
				.getTime()) {
			
			// If the year of birth in the health card is the year
			// of birth in the health card then we cannot use the
			// parent's card - this must be the baby's card
			// (possibly with errors).
			if (healthNumber.length() < 6)
				return false;
			String birthYearString = healthNumber
							.substring(4, 6);
			if (birthYearString == null)
				return false;
			int birthYearInHealthCard = Integer.parseInt(birthYearString);
			Calendar calDateOfBirth = GregorianCalendar.getInstance();
			calDateOfBirth.setTime(dateOfBirth);
			if ((calDateOfBirth.get(Calendar.YEAR) % 100) == birthYearInHealthCard)
				return false;

			return true;
		}
		return false;
	}

	static public boolean validateAllPatientsFound(Connection conn_m,
			BabqWarningList warningList) throws SQLException {
		boolean returnValue = true;
		// Check that every patient number referred to in an appt exists
		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt
				.executeQuery("select apptTbl.ApptDate, apptTbl.PatientNum , patientTbl.PatientNum "
						+ "from apptTbl left join patientTbl "
						+ "on patientTbl.PatientNum = apptTbl.PatientNum "
						+ "where patientTbl.PatientNum is null "
						+ "and apptTbl.PatientNum != 0"
						/**@Since 2015 Xmas Project*/
						+ "and LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'"); 
		while (rs.next()) {
			returnValue = false;
			warningList.addWarning("noFile", -1, "An appointment on "
					+ rs.getDate("apptTbl.ApptDate") + " refers to patient "
					+ rs.getInt("PatientNum")
					+ " but no patient with this number " + "was found in the "
					+ "list of patients");
		}
		return returnValue;
	}

	public static boolean validateAllProvidersFound(Connection conn_m,
			BabqWarningList warningList) throws SQLException {
		boolean returnValue = true;

		// Check that every patient number referred to in an appt exists
		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt
				.executeQuery("select apptTbl.ApptDate, apptTbl.ProviderID, providerTbl.ProviderID "
						+ "from apptTbl left join providerTbl "
						+ "on providerTbl.ProviderID = apptTbl.ProviderID "
						+ "where providerTbl.ProviderID is null "
						+ "and apptTbl.PatientNum != 0"
						/**@Since 2015 Xmas Project*/
						+ "and LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'");
		while (rs.next()) {
			returnValue = false;
			warningList.addWarning("", -1, "An appointment on "
					+ rs.getDate("apptTbl.ApptDate") + " refers to provider "
					+ rs.getInt("apptTbl.ProviderID")
					+ " but no provider with this number "
					+ "was found in the " + "list of providers");
		}
		rs.close();

		if (!BabqConfig.ignoreFamilyMdErrors()) {
			// Check that every MD number referred to in an patient record
			// exists
			rs = stmt
					.executeQuery("select patientTbl.PatientNum, patientTbl.HealthNumber, "
							+ "patientTbl.FirstName, patientTbl.Surname, "
							+ "patientTbl.FamilyDoctor "
							+ "from patientTbl left join providerTbl "
							+ "on providerTbl.OhipId = patientTbl.FamilyDoctor "
							+ "where providerTbl.OhipId is null "
							+ "AND patientTbl.FamilyDoctor != 0 ");
			while (rs.next()) {

				returnValue = false;
				warningList
						.addWarning(
								"",
								-1,
								"The patient "
										+ rs.getString("patientTbl.PatientNum")
										+ "-"
										+ rs.getString("FirstName")
										+ " "
										+ rs.getString("Surname")
										+ "/"
										+ rs
												.getString("patientTbl.HealthNumber")
										+ " has FamilyMD "
										+ rs.getInt("patientTbl.FamilyDoctor")
										+ " but no provider was found with this OHIP number in "
										+ "the list of providers");
			}
			rs.close();
		}
		stmt.close();
		return returnValue;
	}
}
