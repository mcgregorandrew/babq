/**
 * 
 */
package com.bronzeage.babq.processing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.common.BabqFileLoader;
import com.bronzeage.babq.common.BabqPatientFileLoader;
import com.bronzeage.babq.common.BabqUtils;
import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.common.IBabqProgress;
import com.bronzeage.babq.db.BabqDbAppt;
import com.bronzeage.babq.db.BabqDbBase;
import com.bronzeage.babq.db.BabqDbBillingCodes;
import com.bronzeage.babq.db.BabqDbBillingCodes.DateAndHealthNumber;
import com.bronzeage.babq.db.BabqDbBillings;
import com.bronzeage.babq.db.BabqDbNameExceptions;
import com.bronzeage.babq.db.BabqDbPatient;
import com.bronzeage.babq.db.BabqDbProvider;
import com.bronzeage.babq.processing.BabqRules.BabqValErrorType;


/**
 * @author andrew
 */
public class BabqProcessor implements IBabqProcessor {

	private static String eol = String.format("%n");
	Connection conn_m;
	private IBabqProgress progressTracker_m;
	BabqDbBillings billingDb_m;
	BabqDbAppt apptDb_m;
	BabqDbPatient patientDb_m;
	BabqDbProvider providerDb_m;
	BabqDbNameExceptions nameExcDb_m;
	BabqDbBillingCodes billingCodesDb_m;
	Set<String> badHealthNumList_m = new HashSet<String>();
	String initError_m = null;
	public static final String CANCEL_DELETED_COND = "details NOT LIKE 'cancelled%'"
			+ " AND details NOT LIKE '(deleted%' AND noShow='F' AND apptTbl.PatientNum != 0"
			/**@Since 2015 Xmas Project*/
			+ " AND LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'"; 

	/**
	 * @throws Exception
	 */
	public BabqProcessor() throws Exception {
		conn_m = BabqDbBase.makeConnection(BabqConfig.getJdbcInfo(), "babq");
		if (conn_m == null)
			initError_m = "Failed to connect to " + BabqConfig.getJdbcInfo();
			
		billingDb_m = new BabqDbBillings(conn_m);
		apptDb_m = new BabqDbAppt(conn_m);
		patientDb_m = new BabqDbPatient(conn_m);
		providerDb_m = new BabqDbProvider(conn_m);
		nameExcDb_m = new BabqDbNameExceptions(conn_m);
		billingCodesDb_m = new BabqDbBillingCodes(conn_m);
	}

	static Logger logger_m = Logger.getLogger(BabqProcessor.class.getPackage()
			.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#doLoadData(java.io.File,
	 * java.io.File, java.io.File, com.bronzeage.babq.common.BabqWarningList)
	 */
	public void doLoadData(BabqWarningList warningList) throws Exception {
		final String patientFileName = BabqConfig
				.getPref(BabqConfig.PATIENT_TBL_NAME);
		final String appointmentFileName = BabqConfig
				.getPref(BabqConfig.APPT_TBL_NAME);
		final String billingCodeFileName = BabqConfig
				.getPref(BabqConfig.BILLING_CODE_TBL_NAME);
		final String providerFileName = BabqConfig
				.getPref(BabqConfig.PROVIDER_TBL_NAME);
		File apptFile = new File(appointmentFileName);
		File patientFile = new File(patientFileName);
		File providerFile = new File(providerFileName);
		File billingCodeFile = new File(billingCodeFileName);
		int count;

		String[] strings;

		// Check file names
		int numWarningsAtStart = warningList.getWarningCount();
		if (!apptFile.isFile())
			warningList.addWarning(-1, "File " + apptFile.getAbsolutePath()
					+ " was not found");
		if (!patientFile.isFile())
			warningList.addWarning(-1, "File " + patientFile.getAbsolutePath()
					+ " was not found");
		if (!providerFile.isFile())
			warningList.addWarning(-1, "File " + providerFile.getAbsolutePath()
					+ " was not found");

		if (numWarningsAtStart != warningList.getWarningCount())
			return;

		apptDb_m.createTbl();

		logger_m.fine("Processing appt file " + apptFile);

		count = 0;
		warningList.setFile(apptFile.getName());
		BabqFileLoader loader = new BabqFileLoader(apptFile, progressTracker_m,
				true);
		strings = loader.readRecord(); // skip header
		strings = loader.readRecord(); // skip header
		while ((strings = loader.readRecord()) != null) {
			if (apptDb_m.validateRecord(loader.getLineNumber(), strings,
					warningList)) {
				apptDb_m
						.addRecord(loader.getLineNumber(), strings, warningList);
				count++;
			}
		}
		loader.close();

		warningList
				.addLog(apptFile.getName(), -1, "Read " + count + " records");
		String patientFileNameString = patientFile.getName();
		patientDb_m.createTbl();

		logger_m.fine("Processing patient file " + patientFile);
		long start = System.currentTimeMillis();
		warningList.addLog(patientFileNameString, -1, "Opened file");
		count = 0;
		warningList.setFile(patientFileNameString);
		loader = new BabqPatientFileLoader(patientFile, progressTracker_m, true);
		//strings = loader.readRecord(); // skip header
		List<String[]> l = new ArrayList<String[]> ();
		while ((strings = loader.readRecord()) != null) {
			l.add(strings);
			count++;
			if (l.size() > 100){ 
				patientDb_m.addRecords(patientFileNameString, loader.getLineNumber(), l, warningList);
				l.clear();
			}
		}
		if (l.size() > 0){ 
			patientDb_m.addRecords(patientFileNameString, loader.getLineNumber(), l, warningList);
		}
		loader.close();
		warningList.addLog(patientFileNameString, -1, "Read " + count
				+ " records");
		logger_m.warning("XXX DONE Processing patient file " + patientFile + " In " + (System.currentTimeMillis() - start));

		providerDb_m.createTbl();
		loader = new BabqFileLoader(providerFile, progressTracker_m, false);
		strings = loader.readRecord(); // skip header
		warningList.addLog(providerFile.getName(), -1, "Opened file");
		count = 0;
		warningList.setFile(providerFile.getName());
		while ((strings = loader.readRecord()) != null) {
			if (providerDb_m.validateRecord(loader.getLineNumber(), strings,
					warningList)) {
				providerDb_m.addRecord(loader.getLineNumber(), strings,
						warningList);
				count++;
			}
		}
		loader.close();
		warningList.addLog(providerFile.getName(), -1, "Read " + count
				+ " records");

		billingCodesDb_m.createTbl();
		if (billingCodeFile.length() > 0) {
			loader = new BabqFileLoader(billingCodeFile, progressTracker_m, true);
			strings = loader.readRecord(); // skip header
			warningList.addLog(billingCodeFile.getName(), -1, "Opened file");
			count = 0;
			warningList.setFile(billingCodeFile.getName());
			
			loader.readRecord(); // Skip first line
			while ((strings = loader.readRecord()) != null) {
				if (billingCodesDb_m.validateRecord(loader.getLineNumber(), strings,
						warningList)) {
					billingCodesDb_m.addRecord(loader.getLineNumber(), strings,
							warningList);
					count++;
				}
			}
			loader.close();
			
			warningList.addLog(billingCodeFile.getName(), -1, "Read " + count
					+ " records");
			
			// If there are multiple codes for a given patient and date we need to 
			// reduce this to 1 but preferentially dropping the excluded codes.
			removeDuplicateBillingCodeRecords(billingCodesDb_m, warningList);
			
		} else {
			warningList.addLog(billingCodeFile.getName(), -1, "No billing code file loaded");
			
		}
		loadNameExcTbl(warningList);

		BabqRules.validateAllPatientsFound(conn_m, warningList);
		BabqRules.validateAllProvidersFound(conn_m, warningList);

	}

	private void removeDuplicateBillingCodeRecords(BabqDbBillingCodes billingCodesDb, BabqWarningList warningList) throws Exception {
		Set<String> excludedBillingCodeSet = loadExcludedBillingCodesSet(warningList);
		
		// Get the list of records with more that one record for a healthnumber on a given day
		List<DateAndHealthNumber> listOfDuplCodeRecs = billingCodesDb.getListOfRecordsWithMoreThanOneCodeInDay(warningList);
		
		// For each of these healthnumber/apptdate, get the list of billing codes
		for (DateAndHealthNumber rec: listOfDuplCodeRecs) {
			// Read healthnumber and date
			String apptDate = rec.apptDate_m;
			String healthNumber = rec.healthNumber_m;
			// Make list of billing codes
			List<String> billingCodesFound = billingCodesDb.getBillingCodesForDateAndHn(apptDate, healthNumber, warningList);
			
			String firstOk = null;
			for (String billingCode: billingCodesFound) {
				if (!excludedBillingCodeSet.contains(billingCode)) {
					if (firstOk == null) {
						firstOk = billingCode;
					}
				}
			}
			
			String codeToKeep;
			if (firstOk != null) {
				// If there is a non-excluded billing code, keep that, delete all others
				codeToKeep = firstOk;
			} else {
				// If there is only excluded billing codes, keep the first and delete the others
				codeToKeep = billingCodesFound.get(0);
			}
			
			// Delete the code we want to keep from the list of code found
			billingCodesFound.remove(codeToKeep);
			billingCodesDb.deleteMatchingRecords(apptDate, healthNumber, billingCodesFound, warningList);
		}
	}

	public void loadNameExcTbl(BabqWarningList warningList) throws Exception {
		String[] strings;
		BabqFileLoader loader;
		int count = 0;
		File excFile = new File(BabqConfig
				.getPref(BabqConfig.EXC_NAME_FILE_NAME));
		nameExcDb_m.createTbl();
		if (excFile.exists()) {
			loader = new BabqFileLoader(excFile, progressTracker_m, false);
			strings = loader.readRecord(); // skip header
			warningList.setFile(excFile.getName());
			warningList.addLog(excFile.getName(), -1, "Opened file");
			while ((strings = loader.readRecord()) != null) {
				if (nameExcDb_m.validateRecord(loader.getLineNumber(), strings,
						warningList)) {
					nameExcDb_m.addRecord(loader.getLineNumber(), strings,
							warningList);
					count++;
				}
			}
			warningList.addLog(excFile.getName(), -1, "Billing exclusion file: read " + count
					+ " records");

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#doMakeQbBillingTbl(com.bronzeage
	 * .babq.common.BabqWarningList)
	 */
	public void doMakeQbBillingTbl(BabqWarningList warningList)
			throws Exception {

		logger_m.info("Checking Quebec babies without health card numbers... ");
		Collection<String> cardlessQcBabies = getQuebecBabiesWithoutNumbers();

		logger_m.info("cardlessQcBabies: " + cardlessQcBabies.size());
		checkQuebecBabiesWithoutNumbers(cardlessQcBabies, warningList);

		int count = 0;
		logger_m.info("Creating billing tbl... ");
		String patientFile = new File(BabqConfig
				.getPref(BabqConfig.PATIENT_TBL_NAME)).getName();
		billingDb_m.createTbl();
		Statement stmt = conn_m.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT  apptTbl.ApptDate,"
							+ "providerTbl.location,"
							+ "nameExcTbl.qcSurname, nameExcTbl.qcFirstName, "
							+ "patientTbl.FirstName, patientTbl.Surname,"
							+ "patientTbl.Province, patientTbl.HealthNumber,"
							+ "patientTbl.DateOfBirth, patientTbl.PatientNum, "
							+ "patientTbl.DateOfCardExpiry,"
							+ "patientTbl.DateOfCardExpiryCor,patientTbl.SourceFile, patientTbl.SourceLineNumber,"
							+ "patientTbl.Sex, "
							/**
							 * @Since version 3.1. Address address to clinic output data
							 */
							+ "patientTbl.MailAddrLine1,patientTbl.MailAddrLine2, "
							+ "patientTbl.MailCity,patientTbl.MailProvince, "
							+ "patientTbl.MailCountry,patientTbl.MailPostal, "
							
							/**
							 * @Since version 3.1. Address address to clinic output data
							 */
							+ "patientTbl.ResAddrLine1,patientTbl.ResAddrLine2, "
							+ "patientTbl.ResCity,patientTbl.ResProvince, "
							+ "patientTbl.ResCountry,patientTbl.ResPostal "
							
							+ " FROM apptTbl, providerTbl, patientTbl"
							+ " LEFT JOIN nameExcTbl"
							+ " ON nameExcTbl.HealthNumber = patientTbl.HealthNumber"
							+ " WHERE apptTbl.PatientNum=patientTbl.PatientNum"
							+ " AND providerTbl.ProviderId = apptTbl.ProviderId"
							+ " AND " + CANCEL_DELETED_COND
							//Old code = + " AND patientTbl.Province = 'QC'");
							+ " AND patientTbl.Province != 'ON'");

			badHealthNumList_m.clear();
			while (rs.next()) {
				String healthCard = rs.getString("patientTbl.HealthNumber");
				java.sql.Date dateOfExpiry;
				if (BabqConfig.fixExpiryDates()) {
					dateOfExpiry = rs.getDate("patientTbl.DateOfCardExpiryCor");
				} else {
					dateOfExpiry = rs.getDate("patientTbl.DateOfCardExpiry");
				}

				// For names, use the fields in the name exception database
				// if they exist otherwise use the fields from the patient table
				String surname;
				surname = rs.getString("nameExcTbl.QcSurname");
				if (surname == null) {
					surname = rs.getString("patientTbl.Surname");
				}
				//
				String firstName;
				firstName = rs.getString("nameExcTbl.QcFirstName");
				if (firstName == null) {
					firstName = rs.getString("patientTbl.FirstName");
				}

				java.sql.Date dateOfBirth = rs
						.getDate("patientTbl.DateOfBirth");
				int patientNum = rs.getInt("patientTbl.PatientNum");
				String sex = rs.getString("patientTbl.Sex");
				String province = rs.getString("patientTbl.Province");
				String location = rs.getString("providerTbl.location");
				String sourceFile = rs.getString("patientTbl.SourceFile");
				String billingCode = null;  // Filled in later
				int lineNumber = rs.getInt("patientTbl.SourceLineNumber");
				java.sql.Date dateOfService = rs.getDate("apptTbl.ApptDate");
				if (BabqRules.validateBasicFields(sourceFile, lineNumber,
						healthCard, surname, firstName, province, sex,
						dateOfBirth, warningList, patientNum)) {

					boolean okToUseParentHealthNum = BabqRules
							.isAllowedToUseParentHealthNum(dateOfService,
									dateOfBirth, healthCard);
					//TODO: Add project version number
					//Old Logic only checks health card number for QC 
					BabqValErrorType valResult = BabqValErrorType.NO_ERROR;;
					if (province.equals("QC") ) {
						valResult = BabqRules.validateHealthNum(
								sourceFile, lineNumber, healthCard, surname,
								firstName, sex, dateOfBirth,
								okToUseParentHealthNum, warningList, patientNum);
					} 
					boolean usingParentHealthNum = false;
					if ((valResult != BabqValErrorType.NO_ERROR)
							&& okToUseParentHealthNum) {
						valResult = BabqValErrorType.NO_ERROR;
						usingParentHealthNum = true;
					}
					
					/**
					 * @Since version 3.1. Address address to clinic output data
					 */
					String mailingAddress = rs.getString("MailAddrLine1")+", "+rs.getString("MailAddrLine2")
											+rs.getString("MailCity")+", "+rs.getString("MailProvince")+", "
											+rs.getString("MailCountry")+", "+rs.getString("MailPostal");
					
					/**
					 * @Since version 3.1. Address address to clinic output data
					 */
					String resAddress = rs.getString("ResAddrLine1")+", "+rs.getString("ResAddrLine2")
											+rs.getString("ResCity")+", "+rs.getString("ResProvince")+", "
											+rs.getString("ResCountry")+", "+rs.getString("ResPostal");

					boolean expiryDateOk = BabqRules.validateExpiryDate(
							sourceFile, lineNumber, dateOfExpiry,
							dateOfService, warningList, patientNum);
					if ((valResult == BabqValErrorType.NO_ERROR)
							&& (expiryDateOk) && (dateOfExpiry != null)) {
							billingDb_m.addRecord(patientFile, lineNumber,
									healthCard, dateOfExpiry, //
									surname, //
									firstName, //
									dateOfBirth, // 
									sex, // 
									location, dateOfService, usingParentHealthNum, province, mailingAddress, resAddress,
									billingCode);
						count++;
					}

					if (valResult == BabqValErrorType.NAME_ERROR) {
						badHealthNumList_m.add(healthCard);
					}
				}
			}
		} finally {
			stmt.close();
		}

		logger_m.warning("Creating records for Quebec babies without health card numbers... ");
		mergeQcBabiesToBillingTable(cardlessQcBabies, warningList);

		// Set the billing codes for the billing records
		Statement stmt2 = conn_m.createStatement();
		try {
			stmt2.executeUpdate("UPDATE " + billingDb_m.getTblName() + " bt SET bt.billingcode = "
					+ "(SELECT billingcode from " + billingCodesDb_m.getTblName() + " bct WHERE "
					+ "  bct.apptdate = bt.dateofservice AND "
					+ "  bct.healthnumber = bt.healthnumber)");

		} finally {
			stmt2.close();
		}
		
		// Remove the records with excluded billing codes
		try {
			Set<String> excludedBillingCodeSet = loadExcludedBillingCodesSet(warningList);
			if (!excludedBillingCodeSet.isEmpty()) {
				stmt2 = conn_m.createStatement();
				try {
					int numRec = stmt2.executeUpdate("DELETE FROM " + billingDb_m.getTblName() + " WHERE " + "  billingCode IN ('"
							+ String.join("','", excludedBillingCodeSet) + "')");
					warningList.addLog("", -1, "Billing tbl removed " + numRec + " records because the billing codes for these visits are not allowed");

				} finally {
					stmt2.close();
				}
			}
		} catch (IOException e) {
			warningList.addWarning("", -1, "Error loading list of excluded billing codes: " + e);
			return;
		}
		warningList.addLog("", -1, "Billing tbl contains " + count + " records");

	}

	public Collection<String> getQuebecBabiesWithoutNumbers()
			throws SQLException {
		Collection<String> babies = new HashSet<String>();
		Statement stmt = conn_m.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT * from apptTbl,patientTbl "
							+ " LEFT JOIN nameExcTbl ON nameExcTbl.BabyFirstName = patientTbl.FirstName"
							+ " AND nameExcTbl.BabySurname = patientTbl.Surname"
							+ " WHERE Province != 'QC' AND MailProvince = 'QC' "
							+ " AND ActiveStatus='Active' "
							+ " AND apptTbl.PatientNum = patientTbl.PatientNum"
							/**@Since 2015 Xmas Project*/
							+ " AND LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'"
							+ " AND DateOfBirth > (ApptDate - 365) ");

			while (rs.next()) {
				babies.add(rs.getString("patientTbl.PatientNum"));
			}
		} finally {
			stmt.close();
		}
		return babies;
	}

	/**
	 * 
	 * @param warningList
	 * @throws SQLException
	 */
	private void mergeQcBabiesToBillingTable(
			Collection<String> cardlessQcBabies, BabqWarningList warningList)
			throws SQLException {
		if (cardlessQcBabies.size() == 0) {
			logger_m.warning("No babies in list");
			return;
		}
		Statement stmt = conn_m.createStatement();
		try {
			StringBuffer query = new StringBuffer(
					"SELECT  apptTbl.ApptDate,"
							+ "providerTbl.location,"
							+ "nameExcTbl.HealthCardExpiry, nameExcTbl.HealthNumber, "
							+ "nameExcTbl.qcSurname, nameExcTbl.qcFirstName, "
							+ "patientTbl.FirstName, patientTbl.Surname,"
							+ "patientTbl.Province, patientTbl.HealthNumber,"
							+ "patientTbl.DateOfBirth, patientTbl.PatientNum, "
							+ "patientTbl.DateOfCardExpiry,"
							+ "patientTbl.DateOfCardExpiryCor,patientTbl.SourceFile, patientTbl.SourceLineNumber,"
							+ "patientTbl.Sex, "
							
							/**
							 * @Since version 3.1. Address address to clinic output data
							 */
							+ "patientTbl.MailAddrLine1,patientTbl.MailAddrLine2, "
							+ "patientTbl.MailCity,patientTbl.MailProvince, "
							+ "patientTbl.MailCountry,patientTbl.MailPostal, "
							
							/**
							 * @Since version 3.1. Address address to clinic output data
							 */
							+ "patientTbl.ResAddrLine1, patientTbl.ResAddrLine2, "
							+ "patientTbl.ResCity, patientTbl.ResProvince, "
							+ "patientTbl.ResCountry, patientTbl.ResPostal "
							
							+ " FROM apptTbl, providerTbl, patientTbl, nameExcTbl"
							+ " WHERE apptTbl.PatientNum=patientTbl.PatientNum "
							+ " AND nameExcTbl.BabySurname = patientTbl.Surname"
							+ " AND nameExcTbl.BabyFirstName = patientTbl.FirstName"
							+ " AND providerTbl.ProviderId = apptTbl.ProviderId"
							+ " AND " + CANCEL_DELETED_COND
							+ " AND apptTbl.PatientNum IN (");
			int count = 0;
			for (String ptNum : cardlessQcBabies) {
				if (count++ != 0)
					query.append(",");
				query.append(ptNum);
			}
			query.append(")");
			ResultSet rs = stmt.executeQuery(query.toString());

			while (rs.next()) {
				// For names, use the fields in the name exception database
				// if they exist otherwise use the fields from the patient table
				String surname = rs.getString("patientTbl.Surname");
				String firstName = rs.getString("patientTbl.FirstName");

				java.sql.Date dateOfBirth = rs
						.getDate("patientTbl.DateOfBirth");
				String sex = rs.getString("patientTbl.Sex");
				String location = rs.getString("providerTbl.location");
				String sourceFile = rs.getString("patientTbl.SourceFile");
				int lineNumber = rs.getInt("patientTbl.SourceLineNumber");
				java.sql.Date dateOfService = rs.getDate("apptTbl.ApptDate");
				java.sql.Date dateOfExpiry = rs
						.getDate("nameExcTbl.HealthCardExpiry");
				String healthCardNumber = rs
						.getString("nameExcTbl.HealthNumber");
				String province = rs.getString("patientTbl.Province");
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				String mailingAddress = rs.getString("MailAddrLine1")+", "+rs.getString("MailAddrLine2")
										+rs.getString("MailCity")+", "+rs.getString("MailProvince")+", "
										+rs.getString("MailCountry")+", "+rs.getString("MailPostal");
				
				/**
				 * @Since version 3.1. Address address to clinic output data
				 */
				String resAddress = rs.getString("ResAddrLine1")+", "+rs.getString("ResAddrLine2")
										+rs.getString("ResCity")+", "+rs.getString("ResProvince")+", "
										+rs.getString("ResCountry")+", "+rs.getString("ResPostal");
				String billingCode = rs.getString("billingCodeTbl.BillingCode");

				logger_m.warning("Added QC baby record for "
						+ rs.getString("patientTbl.FirstName") + " "
						+ rs.getString("patientTbl.Surname"));
				// For each baby, add a record to the table with their birth
				// date
				// and their mother's number and expiry date
				billingDb_m.addRecord(sourceFile, lineNumber, healthCardNumber,
						dateOfExpiry, //
						surname, //
						firstName, //
						dateOfBirth, // 
						sex, // 
						location, dateOfService, true, province, mailingAddress, resAddress,
						billingCode);
			}
		} finally {
			stmt.close();
		}
	}

	public void outputBillingTbl(BabqWarningList warningList)
			throws SQLException, IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		if (billingDb_m.getIntValue("SELECT COUNT(*) from billingTbl") == 0) {
			warningList.addWarning("", -1,
					"No records in the billingTbl - load data and merge!");
			return;
		}

		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT  * from billingTbl "
				+ " ORDER BY HealthNumber");
		billingDb_m.ouputRsToCsv(rs, new File(dirToOutputFilesTo,
				"allClinics.txt"), progressTracker_m, warningList);
		stmt.close();

		stmt = conn_m.createStatement();
		rs = stmt.executeQuery("SELECT  * from billingTbl WHERE Site='B'"
				+ " ORDER BY HealthNumber");
		billingDb_m.ouputRsToCsv(rs, new File(dirToOutputFilesTo,
				"bruyereClinic.txt"), progressTracker_m, warningList);
		stmt.close();

		stmt = conn_m.createStatement();
		rs = stmt.executeQuery("SELECT  * from billingTbl WHERE Site='P'"
				+ " ORDER BY HealthNumber");
		billingDb_m.ouputRsToCsv(rs, new File(dirToOutputFilesTo,
				"primroseClinic.txt"), progressTracker_m, warningList);
		stmt.close();

		stmt = conn_m.createStatement();
		rs = stmt
				.executeQuery("SELECT  * from billingTbl WHERE Site!='P' AND Site!='B'"
						+ " ORDER BY HealthNumber");
		billingDb_m.ouputRsToCsv(rs, new File(dirToOutputFilesTo,
				"alliedHealthClinic.txt"), progressTracker_m, warningList);
		rs.close();

		// Update summary table
		File spreadsheetName = new File(BabqConfig
				.getPref(BabqConfig.SUMMARY_FILE_NAME));
		BabqSummarySpreadsheet spreadsheet = new BabqSummarySpreadsheet(
				spreadsheetName);
		int qcBruyereVisits = billingDb_m
				.getIntValue("SELECT COUNT(*) from billingTbl WHERE Site='B'");
		int qcPrimroseVisits = billingDb_m
				.getIntValue("SELECT COUNT(*) from billingTbl WHERE Site='P'");
		int qcAlliedHealthVisits = billingDb_m
				.getIntValue("SELECT COUNT(*) from billingTbl WHERE Site!='P' AND Site!='B'");

		Date minAppt = billingDb_m
				.getDateValue("SELECT MIN(DateOfService) from billingTbl");
		Date maxAppt = billingDb_m
				.getDateValue("SELECT MAX(DateOfService) from billingTbl");

		int bruyereVisits = 0;
		int primroseVisits = 0;
		int alliedHealthVisits = 0;
		rs = stmt.executeQuery("SELECT providerTbl.location "
				+ " FROM apptTbl, providerTbl"
				+ " WHERE providerTbl.ProviderId = apptTbl.ProviderId"
				+ " AND " + CANCEL_DELETED_COND);

		while (rs.next()) {
			String location = rs.getString("providerTbl.location");
			if (location.equals("P"))
				primroseVisits++;
			else if (location.equals("B"))
				bruyereVisits++;
			else
				alliedHealthVisits++;

		}

		stmt.close();

		Map<String, Integer> specialSiteToCountMap = new HashMap<String, Integer>();
		specialSiteToCountMap.put("N", qcAlliedHealthVisits);
		specialSiteToCountMap.put("B", qcBruyereVisits);
		specialSiteToCountMap.put("P", qcPrimroseVisits);
		shareNCountsFairly(specialSiteToCountMap);

		spreadsheet.updateSpreadsheet(minAppt, maxAppt, bruyereVisits,
				primroseVisits, alliedHealthVisits, qcBruyereVisits,
				qcPrimroseVisits, qcAlliedHealthVisits, specialSiteToCountMap
						.get("B"), specialSiteToCountMap.get("P"));
		warningList.addLog(spreadsheetName.getName(), -1, "Updated file "
				+ spreadsheetName + " with new totals");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#loadBillingTbl(java.io.File,
	 * com.bronzeage.babq.common.BabqWarningList)
	 */
	public void loadBillingTbl(BabqWarningList warningList)	throws IOException, SQLException, ParseException {
		File inFile = new File(BabqConfig
				.getPref(BabqConfig.BILLING_CODE_TBL_NAME));
		if (!inFile.isFile()) {
			warningList.addWarning(-1, "File " + inFile.getAbsolutePath()
					+ " was not found");
			return;
		}
		BabqFileLoader loader = new BabqFileLoader(inFile, progressTracker_m,
				false);
		String fileName = inFile.getName();
		warningList.addLog(fileName, -1, "Opened file");
		String strings[] = loader.readRecord(); // Skip header line
		billingDb_m.createTbl();
		int count = 0;
		while ((strings = loader.readRecord()) != null) {
			billingDb_m.addRecord(fileName, loader.getLineNumber(), strings,
					warningList);
			count++;
		}
		loader.close();

		warningList.addLog(fileName, -1, "Read " + count + " records");
	}

	/**
	 * Load file of billing codes that cannot be sent to Quebec
	 * @throws Exception 
	 */
	public Set<String> loadExcludedBillingCodesSet(BabqWarningList warningList)	throws Exception {
		File inFile = new File(BabqConfig
				.getPref(BabqConfig.EXCLUDED_BILLING_CODE_FILE_NAME));
		if (!inFile.isFile()) {
			BabqUtils.copyFromResource("resources/excludedBillingCodes.csv", inFile);
			warningList.addLog("", -1, "Made excludedBillingCodes.csv from template");
		}
		BabqFileLoader loader = new BabqFileLoader(inFile, progressTracker_m,
				false);
		Set<String> excludedBillingCodesSet = new HashSet<String>();
		String fileName = inFile.getName();
		warningList.addLog(fileName, -1, "Opened file");
		String strings[] = loader.readRecord(); // Skip header line
		int count = 0;
		while ((strings = loader.readRecord()) != null) {
			excludedBillingCodesSet.add(strings[0]);
			count++;
		}
		loader.close();

		warningList.addLog("", -1, "Read " + count + " records from the excluded billing code file");
		return excludedBillingCodesSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#checkQuebecHealthNumbers
	 * (java.lang.String, com.bronzeage.babq.common.BabqWarningList)
	 */
	public void checkQuebecHealthNumbers(String tableToCheck,
			BabqWarningList warningList) throws IOException, SQLException,
			ParseException {

		Statement stmt = conn_m.createStatement();
		String whereClause = "";
		if (tableToCheck.equals("patientTbl")) {
			whereClause = " WHERE Province = 'QC' AND ActiveStatus='Active'";
		}

		badHealthNumList_m.clear();
		ResultSet rs = stmt.executeQuery("SELECT * from " + tableToCheck
				+ " LEFT JOIN nameExcTbl" + " ON nameExcTbl.HealthNumber = "
				+ tableToCheck + ".HealthNumber" + whereClause);
		warningList.setFile("unknownFile");
		while (rs.next()) {

			String sex = rs.getString("Sex");
			Date dateOfBirth = rs.getDate("DateOfBirth");
			Date dateOfExpiry = rs.getDate("DateOfCardExpiry");
			if ((BabqConfig.fixExpiryDates()) && (dateOfExpiry != null)) {
				dateOfExpiry = new Date(BabqUtils
						.getDateAtLastDayOfMonth(dateOfExpiry.getTime()));
			}

			// For names, use the fields in the name exception database
			// if they exist otherwise use the fields from the patient table
			String surname = rs.getString("nameExcTbl.qcSurname");
			if (surname == null) {
				surname = rs.getString("Surname");
			}

			String firstName = rs.getString("nameExcTbl.qcFirstName");
			if (firstName == null) {
				firstName = rs.getString("FirstName");
			}

			String healthCard = rs.getString(tableToCheck + ".HealthNumber");
			String sourceFile = rs.getString("SourceFile");
			int lineNumber = rs.getInt("SourceLineNumber");
			String province;
			Date dateOfService;
			Integer patientNum = null;
			if (tableToCheck.equals("billingTbl")) {
				province = "QC";
				dateOfService = rs.getDate("DateOfService");
			} else {
				province = rs.getString("Province");
				dateOfService = new Date(0);
				patientNum = rs.getInt("PatientNum");
			}
			
			/**
			 * @Since version 3.1. Address address to clinic output data
			 */
			String mailingAddress = rs.getString("MailAddrLine1")+", "+rs.getString("MailAddrLine2")
									+rs.getString("MailCity")+", "+rs.getString("MailProvince")+", "
									+rs.getString("MailCountry")+", "+rs.getString("MailPostal");
			
			/**
			 * @Since version 3.1. Address address to clinic output data
			 */
			String resAddress = rs.getString("ResAddrLine1")+", "+rs.getString("ResAddrLine2")
									+rs.getString("ResCity")+", "+rs.getString("ResProvince")+", "
									+rs.getString("ResCountry")+", "+rs.getString("ResPostal");
			
			if (BabqRules.validateBasicFields(sourceFile, lineNumber,
					healthCard, surname, firstName, province, sex, dateOfBirth,
					warningList, null)) {
				boolean okToUseParentHealthNum = BabqRules
						.isAllowedToUseParentHealthNum(dateOfService,
								dateOfBirth, healthCard);
				BabqValErrorType valResult = BabqRules.validateHealthNum(
						sourceFile, lineNumber, healthCard, surname, firstName,
						sex, dateOfBirth, okToUseParentHealthNum, warningList,
						patientNum);
				boolean usingParentHealthNum = false;
				if ((valResult != BabqValErrorType.NO_ERROR)
						&& okToUseParentHealthNum) {
					valResult = BabqValErrorType.NO_ERROR;
					usingParentHealthNum = true;
				}

				boolean expiryDateOk = BabqRules.validateExpiryDate(sourceFile,
						lineNumber, dateOfExpiry, dateOfService, warningList,
						patientNum);
				if ((valResult == BabqValErrorType.NO_ERROR) && (expiryDateOk)) {
					billingDb_m.addRecord(sourceFile, lineNumber, healthCard,
							dateOfExpiry, //
							surname, //
							firstName, //
							dateOfBirth, // 
							sex, // 
							"", dateOfService, usingParentHealthNum, province, mailingAddress, resAddress,
							null);
				}

				if (valResult == BabqValErrorType.NAME_ERROR) {
					badHealthNumList_m.add(healthCard);
				}

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#makeBillingSheets(java.io
	 * .File, java.io.File, com.bronzeage.babq.common.BabqWarningList)
	 */
	public void makeBillingSheets(BabqWarningList warningList) throws Exception {

		File templateFile = new File(BabqConfig
				.getPref(BabqConfig.QB_BILLING_TEMPLATE_FILE));
		if (!templateFile.exists()) {
			warningList
					.addWarning(
							templateFile.getName(),
							0,
							"Quebec billing template file "
									+ templateFile
									+ " not found - run aborted.  Edit settings and run again. ");
			return;
		}
		File outputDir = BabqConfig.getOutputPathNow();
		outputDir.mkdirs();
		File outputFile = new File(outputDir, "qbBilling.xls");

		java.util.Date latestServiceDate = billingDb_m
				.getDateValue("SELECT MAX(DateOfService) FROM billingTbl");
		if (latestServiceDate == null)
		{
			warningList.addWarning(-1, "No billing records found.  Load or merge to create billing records");
			return;
		}
		BabqBillingSpreadsheet spreadsheet = new BabqBillingSpreadsheet(
				templateFile, outputFile, latestServiceDate);

		warningList.setFile("");

		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT  * from billingTbl ORDER BY HealthNumber, DateOfService, DateOfBirth, FirstName");
		String prevPersonalId = "";
		Date prevDateOfService = new Date(0);
		int dupCount = 0;
		int count = 0;
		Map<String, Integer> deletedDupCounts = new HashMap<String, Integer>();
		while (rs.next()) {
			String healthNum = rs.getString("HealthNumber");
			Date dateOfService = rs.getDate("DateOfService");

			// Originally we looked for duplicates by checking the same
			// health number on the same day, but there was a problem with
			// a baby and a mother on the same day being declared a duplicate.
			// To fix this, we make this "personalId" string with the name,
			// date of birth and health number. This should even cover twins
			// (same DoB!) where one is named after the mother (same name)!

			String personalId = rs.getString("Surname") + "-"
					+ rs.getString("FirstName") + "-"
					+ rs.getDate("DateOfBirth") + "-"
					+ rs.getString("HealthNumber");
			String site = rs.getString("Site");
			if ((prevPersonalId.equals(personalId))
					&& (prevDateOfService.compareTo(dateOfService) == 0)) {
				{
					dupCount++;
					warningList.addLog("", -1, "Duplicate record "
							+ String.format("%03d", dupCount) + " for "
							+ healthNum + " on " + dateOfService
							+ " not written");
					if (deletedDupCounts.get(site) == null)
						deletedDupCounts.put(site, 0);
					deletedDupCounts.put(site, deletedDupCounts.get(site) + 1);
				}
			} else {
				spreadsheet.addRecord(healthNum,
						rs.getDate("DateOfCardExpiry"),
						rs.getString("surname"), rs.getString("firstName"), rs
								.getDate("DateOfBirth"), rs.getString("sex"),
						dateOfService, BabqConfig.getCostOfService(), rs
								.getString("UsingParentHealthNum").equals("T"));
				count++;
			}

			prevPersonalId = personalId;
			prevDateOfService = dateOfService;
		}
		spreadsheet.touchUpCurrentSheetAfterDataInput();
		
		if (dupCount > 0)
			warningList.addLog(templateFile.getName(), -1, "There were "
					+ dupCount + " duplicate records.");
		spreadsheet.closeAndSave();
		warningList.addLog(templateFile.getName(), -1, "There were " + count
				+ " records output to the spreadsheet.");

		makeBillingReport(deletedDupCounts, warningList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bronzeage.babq.processing.IBabqProcessor#clearAllTables()
	 */
	public void clearAllTables(BabqWarningList warningList) throws SQLException {

		billingDb_m.createTbl();
		apptDb_m.createTbl();
		patientDb_m.createTbl();
		providerDb_m.createTbl();
		nameExcDb_m.createTbl();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bronzeage.babq.processing.IBabqProcessor#getRowCount(java.lang.String
	 * )
	 */
	public int getRowCount(String tblName) {
		Statement stmt = null;
		int retValue = 0;
		try {
			stmt = conn_m.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tblName);
			rs.next();
			retValue = rs.getInt(1);
		} catch (Throwable e) {
			logger_m.warning("Exception getting row count: ");
			StringBuilder sb = new StringBuilder(e.toString());
			sb.append(eol);
			StackTraceElement[] trace = e.getStackTrace();
			for (StackTraceElement ef : trace)
				sb.append("   " + ef.getFileName() + ":" + ef.getLineNumber()
						+ " " + ef.getClassName() + "." + ef.getMethodName()
						+ eol);
			sb.append("   Trace back done" + eol);

			logger_m.warning(sb.toString());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return retValue;
	}

	public void setProgressTracker(IBabqProgress progressTracker) {
		progressTracker_m = progressTracker;
	}

	public void setUpDirs(BabqWarningList warningList) throws Exception {
		/*
		 * Directory structure is typically: ~/Babq/templateFiles - locations of
		 * template files (e.g. Quebec billing spreadsheet template
		 * ~/Babq/inputFiles - recommended location of input files
		 * ~/Babq/outputFiles - recommended location of output files (in
		 * separate directories for each run) ~/Babq/updatedFiles - recommended
		 * location of updated files. These include the name exception list and
		 * various summaries.
		 */
		File outputDir = new File(BabqConfig
				.getPref(BabqConfig.OUTPUT_DIR_ROOT));
		if (!outputDir.isDirectory()) {
			if (!outputDir.mkdirs()) {
				throw new IOException("Failed to create output directory "
						+ outputDir.getAbsolutePath());
			}
		}

		File excFile = new File(BabqConfig
				.getPref(BabqConfig.EXC_NAME_FILE_NAME));
		if (!excFile.exists()) {
			if (!excFile.getParentFile().isDirectory()) {
				if (!excFile.getParentFile().mkdirs()) {
					throw new IOException(
							"Failed to create parent directory "
									+ excFile.getParentFile());
				}
			}
			BabqConfig.createEmptyExcNameFile(excFile);
		}

		File qcBilling = new File(BabqConfig
				.getPref(BabqConfig.QB_BILLING_TEMPLATE_FILE));
		if (!qcBilling.exists()) {
			if (!qcBilling.getParentFile().isDirectory()) {
				if (!qcBilling.getParentFile().mkdirs()) {
					throw new IOException(
							"Failed to create parent directory "
									+ qcBilling.getParentFile());
				}
				BabqUtils.copyFromResource("resources/qcBillingTemplate.xls",
						qcBilling);
			}
		}

		File summaryFile = new File(BabqConfig
				.getPref(BabqConfig.SUMMARY_FILE_NAME));
		if (!summaryFile.exists()) {
			if (!summaryFile.getParentFile().isDirectory()) {
				if (!summaryFile.getParentFile().mkdirs()) {
					throw new IOException(
							"Failed to create parent directory "
									+ summaryFile.getParentFile());
				}
				BabqUtils.copyFromResource("resources/summary.xls", summaryFile);
			}
		}

		File teamSummaryFile = new File(BabqConfig
				.getPref(BabqConfig.TEAM_SUMMARY_FILE_NAME));
		if (!teamSummaryFile.exists()) {
			if (!teamSummaryFile.getParentFile().isDirectory()) {
				if (!teamSummaryFile.getParentFile().mkdirs()) {
					throw new IOException(
							"Failed to create parent directory "
									+ teamSummaryFile.getParentFile());
				}
				BabqUtils.copyFromResource("resources/teamSummary.xls", teamSummaryFile);
			}
		}

		File excludedBillingCodesFile = new File(BabqConfig
				.getPref(BabqConfig.EXCLUDED_BILLING_CODE_FILE_NAME));
		if (!excludedBillingCodesFile.exists()) {
			if (!excludedBillingCodesFile.getParentFile().isDirectory()) {
				if (!excludedBillingCodesFile.getParentFile().mkdirs()) {
					throw new IOException(
							"Failed to create parent directory "
									+ excludedBillingCodesFile.getParentFile());
				}
			}
			BabqUtils.copyFromResource("resources/excludedBillingCodes.csv", excludedBillingCodesFile);
		}

	}

	public void saveComboTable(BabqWarningList warningList_m) throws Exception {		
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m.setProgressString("Combining data...");
		ResultSet rs = stmt
				.executeQuery("SELECT  * from apptTbl, providerTbl, patientTbl"
						+ " WHERE apptTbl.PatientNum = patientTbl.PatientNum "
						+ " AND apptTbl.ProviderID = providerTbl.ProviderID"
						/**@Since 2015 Xmas Project*/
						//This is excluded as request.
						//+ " AND LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'"
						+ " ORDER BY apptTbl.ApptDate");
		
		File file = new File(BabqConfig.getPref(BabqConfig.COMBO_TBL_NAME));
		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(file));
		PrintWriter pw = new PrintWriter(os);
		int count = 0;

		pw.print("Hello");
		pw.printf(
				"%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", //
				"ApptDate", "BookingDate", "PatientNum", "ProviderID",
				"Duration", "Details", "Status", "NoShow", "Deleted",
				"BookingInitials");
		pw.printf(
				"%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", //
				"Surname", "FirstName", "Province", "DateOfBirth", "Sex",
				"HealthNumber", "HNVersionCode", "DateOfCardExpiry");
		pw.printf("%s\t%s\t%s\t%s\t%s", //
				"ProviderSurname", "Location", "Team", "Role", "Patient's MD"); 
		pw.printf("%n");
		while (rs.next()) {
			pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", //
					rs.getDate("apptTbl.ApptDate"), rs
							.getDate("apptTbl.BookingDate"), rs
							.getInt("apptTbl.PatientNum"), rs
							.getInt("apptTbl.ProviderID"), rs
							.getString("apptTbl.Duration"), rs
							.getString("apptTbl.Details"), rs
							.getString("apptTbl.Status"), rs
							.getString("apptTbl.NoShow"), rs
							.getString("apptTbl.Deleted"), rs
							.getString("apptTbl.BookingInitials"));
			pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", //
					rs.getString("patientTbl.Surname"), rs
							.getString("patientTbl.FirstName"), rs
							.getString("patientTbl.Province"), rs
							.getDate("patientTbl.DateOfBirth"), rs
							.getString("patientTbl.Sex"), rs
							.getString("patientTbl.HealthNumber"), rs
							.getString("patientTbl.HNVersionCode"), rs
							.getDate("patientTbl.DateOfCardExpiry"));
			pw.printf("%s\t%s\t%s\t%s\t%s", 
					rs.getString("providerTbl.ProviderSurname"), 
					rs.getString("providerTbl.Location"), 
					rs.getString("providerTbl.Team"), 
					rs.getString("providerTbl.Role"), 
					rs.getString("patientTbl.PatientDoctor")) ;
			pw.printf("%n");
			if ((++count % 100) == 0) {
				progressTracker_m.setProgressString(String.format(
						"Outputing record %06d to %s", count, file.getName()));
			}
		}
		pw.close();
		os.close();
		stmt.close();
		warningList_m.addLog("", -1, "Wrote data to " + file.getAbsolutePath());
	}

	public void createTempNameExcFile(File realExcFile, File tempExcFile)
			throws Exception {
		BabqFileLoader loader = new BabqFileLoader(realExcFile, null, false);
		PrintWriter pw = new PrintWriter(tempExcFile);
		String[] strings;
		while ((strings = loader.readRecord()) != null) {
			for (int i = 0; i < strings.length; i++) {
				if (i != 0)
					pw.printf(",");
				pw.printf("\"%s\"", strings[i]);
			}
			pw.printf("%n");
		}
		// Append health numbers
		for (String healthNumber : badHealthNumList_m) {
			ResultSet rs = patientDb_m.doQuery(//
					"SELECT * FROM patientTbl " + //
							"WHERE HealthNumber = '" + healthNumber + "'");

			if (rs.next()) {
				pw
						.printf(
								"\"%s\",\"%s\",\"%s\",\"PATIENT ID %s (DELETE THIS COLUMN)\"%n",
								healthNumber, rs.getString("Surname"), rs
										.getString("FirstName"), rs
										.getInt("PatientNum"));
			}
			patientDb_m.closeQuery(rs);
		}
		pw.close();
		loader.close();
	}

	public void generateTeamReports(BabqWarningList warningList)
			throws Exception {
		makeTeamReport(warningList);
		makeRoleReport(warningList);
		makeProviderReport(warningList);
		makeAgeReport(warningList);

	}

	private void makeTeamReport(BabqWarningList warningList)
			throws SQLException, Exception, IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m
				.setProgressString("Combining data for team report...");

		// Open spreadsheet
		File spreadsheetFile = new File(BabqConfig
				.getPref(BabqConfig.TEAM_SUMMARY_FILE_NAME));

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy MM dd");

		BabqTeamRoleSummarySpreadsheet spreadsheet = new BabqTeamRoleSummarySpreadsheet(
				spreadsheetFile, fmt.format(new java.util.Date()),
				"resources/teamSummary.xls");

		ResultSet rs;

		// Get min and max dates
		rs = stmt.executeQuery("SELECT MIN(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(2, 1, rs.getString(1));
		rs.close();
		rs = stmt.executeQuery("SELECT MAX(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(3, 1, rs.getString(1));
		rs.close();

		// Get list of teams

		rs = stmt
				.executeQuery("SELECT Team, MIN(Location) Loc from providerTbl"
						+ " GROUP BY providerTbl.Team ORDER BY providerTbl.Team");
		// Write teams into first column
		while (rs.next()) {
			String team = rs.getString("Team");
			spreadsheet.addTeam(team, false);
			progressTracker_m
					.setProgressString("Writing data for team " + team);
			spreadsheet.addString(team, 1, rs.getString("Loc"));
		}
		rs.close();

		// Write Appointment counts into third column
		rs = stmt
				.executeQuery("SELECT  providerTbl.Team, COUNT(*) num FROM providerTbl"
						+ " JOIN apptTbl"
						+ " ON apptTbl.ProviderID = providerTbl.ProviderID"
						+ " WHERE "
						+ CANCEL_DELETED_COND
						+ " GROUP BY providerTbl.Team ORDER BY providerTbl.Team");
		while (rs.next())
			spreadsheet.addCount(rs.getString("Team"), 2, rs.getInt("num"));
		rs.close();

		// Write Enrolled into fourth column
		rs = stmt
				.executeQuery("SELECT  providerTbl.Team, COUNT(*) num FROM providerTbl"
						+ " JOIN patientTbl"
						+ " ON (providerTbl.OhipId = patientTbl.FamilyDoctor)"
						+ " GROUP BY providerTbl.Team ORDER BY providerTbl.Team");
		while (rs.next())
			spreadsheet.addCount(rs.getString("Team"), 3, rs.getInt("num"));
		rs.close();

		stmt.close();

		// Close spreadsheet
		progressTracker_m.setProgressString("Closing "
				+ spreadsheetFile.getAbsolutePath());
		spreadsheet.closeAndSave();
		warningList.addLog("", -1, "Updated file "
				+ spreadsheetFile.getAbsolutePath());
	}

	private void makeRoleReport(BabqWarningList warningList)
			throws SQLException, Exception, IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m
				.setProgressString("Combining data for role report...");

		// Open spreadsheet
		File spreadsheetFile = new File(BabqConfig
				.getPref(BabqConfig.ROLE_SUMMARY_FILE_NAME));

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy MM dd");

		BabqTeamRoleSummarySpreadsheet spreadsheet = new BabqTeamRoleSummarySpreadsheet(
				spreadsheetFile, fmt.format(new java.util.Date()),
				"resources/roleSummary.xls");

		ResultSet rs;

		// Get min and max dates
		rs = stmt.executeQuery("SELECT MIN(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(2, 1, rs.getString(1));
		rs.close();
		rs = stmt.executeQuery("SELECT MAX(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(3, 1, rs.getString(1));
		rs.close();

		// Get list of teams

		rs = stmt.executeQuery("SELECT Role from providerTbl"
				+ " GROUP BY Role " + "ORDER BY providerTbl.Role");
		// Write teams into first column
		while (rs.next()) {
			String role = rs.getString("Role");
			progressTracker_m
					.setProgressString("Writing data for role " + role);
			spreadsheet.addTeam(role, false);
		}
		rs.close();

		// Write appointment counts into second column
		rs = stmt
				.executeQuery("SELECT  providerTbl.Role, COUNT(*) num FROM providerTbl"
						+ " JOIN apptTbl"
						+ " ON apptTbl.ProviderID = providerTbl.ProviderID"
						+ " WHERE "
						+ CANCEL_DELETED_COND
						+ " GROUP BY providerTbl.Role ORDER BY providerTbl.Role");
		while (rs.next())
			spreadsheet.addCount(rs.getString("Role"), 1, rs.getInt("num"));
		rs.close();

		stmt.close();

		progressTracker_m.setProgressString("Closing "
				+ spreadsheetFile.getAbsolutePath());
		// Close spreadsheet
		spreadsheet.closeAndSave();
		warningList.addLog("", -1, "Updated file "
				+ spreadsheetFile.getAbsolutePath());
	}

	private void makeProviderReport(BabqWarningList warningList)
			throws SQLException, Exception, IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m
				.setProgressString("Combining data for provider report...");

		// Open spreadsheet
		File spreadsheetFile = new File(BabqConfig
				.getPref(BabqConfig.PROVIDER_SUMMARY_FILE_NAME));

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy MM dd");

		BabqTeamRoleSummarySpreadsheet spreadsheet = new BabqTeamRoleSummarySpreadsheet(
				spreadsheetFile, fmt.format(new java.util.Date()),
				"resources/providerSummary.xls");

		ResultSet rs;

		// Get min and max dates
		rs = stmt.executeQuery("SELECT MIN(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(2, 1, rs.getString(1));
		rs.close();
		rs = stmt.executeQuery("SELECT MAX(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(3, 1, rs.getString(1));
		rs.close();

		// Get list of teams

		rs = stmt
				.executeQuery("SELECT ProviderID, Team, Role, ProviderSurname, ProviderFirstName from providerTbl"
						+ " ORDER BY Team,ProviderSurname");
		// Write teams into first column
		while (rs.next()) {
			String providerId = "" + rs.getInt("ProviderId");
			progressTracker_m.setProgressString("Writing data for provider "
					+ providerId);
			spreadsheet.addTeam(providerId, true);
			spreadsheet.addString(providerId, 1, rs
					.getString("ProviderSurname"));
			spreadsheet.addString(providerId, 2, rs
					.getString("ProviderFirstName"));
			spreadsheet.addString(providerId, 3, rs.getString("Role"));
			spreadsheet.addString(providerId, 4, rs.getString("Team"));
		}
		rs.close();

		// Write appointment counts
		rs = stmt
				.executeQuery("SELECT  providerTbl.ProviderID, COUNT(*) num FROM providerTbl"
						+ " JOIN (apptTbl)"
						+ " ON (apptTbl.ProviderID = providerTbl.ProviderID)"
						+ " WHERE "
						+ CANCEL_DELETED_COND
						+ " GROUP BY providerTbl.ProviderID");
		while (rs.next())
			spreadsheet.addCount(rs.getString("ProviderID"), 5, rs
					.getInt("num"));
		rs.close();

		// Write enrolled count
		rs = stmt
				.executeQuery("SELECT  providerTbl.ProviderID, COUNT(*) num FROM providerTbl"
						+ " JOIN (patientTbl)"
						+ " ON (patientTbl.FamilyDoctor = providerTbl.OhipId)"
						+ " GROUP BY providerTbl.ProviderID");
		while (rs.next())
			spreadsheet.addCount(rs.getString("ProviderID"), 6, rs
					.getInt("num"));
		rs.close();

		// Write no show count
		// Write appointment counts
		rs = stmt
				.executeQuery("SELECT  providerTbl.ProviderID, COUNT(*) num FROM providerTbl"
						+ " JOIN (apptTbl)"
						+ " ON (apptTbl.ProviderID = providerTbl.ProviderID)"
						+ " WHERE "
						+ CANCEL_DELETED_COND
						+ " AND NoShow = 'T' "
						+ " GROUP BY providerTbl.ProviderID");
		while (rs.next())
			spreadsheet.addCount(rs.getString("ProviderID"), 7, rs
					.getInt("num"));
		rs.close();

		stmt.close();

		progressTracker_m.setProgressString("Closing "
				+ spreadsheetFile.getAbsolutePath());
		// Close spreadsheet
		spreadsheet.closeAndSave();
		warningList.addLog("", -1, "Updated file "
				+ spreadsheetFile.getAbsolutePath());
	}

	private void makeAgeReport(BabqWarningList warningList)
			throws SQLException, Exception, IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m.setProgressString("Combining data for age report...");

		// Open spreadsheet
		File spreadsheetFile = new File(BabqConfig
				.getPref(BabqConfig.AGE_SUMMARY_FILE_NAME));

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy MM dd");

		BabqTeamRoleSummarySpreadsheet spreadsheet = new BabqTeamRoleSummarySpreadsheet(
				spreadsheetFile, fmt.format(new java.util.Date()),
				"resources/ageSummary.xls");

		ResultSet rs;

		// Get min and max dates
		rs = stmt.executeQuery("SELECT MIN(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(2, 1, rs.getString(1));
		rs.close();
		rs = stmt.executeQuery("SELECT MAX(ApptDate) FROM apptTbl");
		if (rs.next())
			spreadsheet.setCellText(3, 1, rs.getString(1));
		rs.close();

		// Make view with age Range
		try {
			stmt.execute("DROP VIEW patientAgeRangeView");
		} catch (SQLException e) {
			// Ignore failure
		}

		String dateDiff = null;
		if (BabqConfig.isH2())
			dateDiff = "DATEDIFF('DAY', DateOfBirth, NOW())";
		else
			dateDiff = "DATEDIFF(NOW(),DateOfBirth)";

		stmt.execute("CREATE VIEW patientAgeRangeView"
				+ " AS SELECT PatientNum,DateOfBirth," + "TRUNCATE(" + dateDiff
				+ "/365.25 /5,0)" + //
				" AS ageGroup FROM patientTbl");

		rs = stmt
				.executeQuery("SELECT ageGroup, count(*) ageCount FROM patientTbl, patientAgeRangeView"
						+ " WHERE patientTbl.PatientNum = patientAgeRangeView.PatientNum "
						+ "GROUP BY ageGroup ORDER BY ageGroup");
		// Write counts in database into first column
		while (rs.next()) {
			String ageGroup = ageGroupToString(rs);
			progressTracker_m.setProgressString("Writing data for age group "
					+ ageGroup);
			spreadsheet.addTeam(ageGroup, false);
			spreadsheet.addCount(ageGroup, 1, rs.getInt("ageCount"));
		}
		rs.close();

		// Write counts of patients with at leat one appt

		// Make view with age Range
		try {
			stmt.execute("DROP VIEW patientWithApptView");
		} catch (SQLException e) {
			// Ignore failure
		}

		stmt.execute("CREATE VIEW patientWithApptView"
				+ " AS SELECT patientTbl.PatientNum , count(*) "
				+ "FROM patientTbl, apptTbl "
				+ "WHERE patientTbl.PatientNum = apptTbl.PatientNum" + " AND "
				+ CANCEL_DELETED_COND + " GROUP BY patientTbl.PatientNum");

		rs = stmt
				.executeQuery("SELECT  ageGroup, count(*) ageCount "
						+ "FROM patientAgeRangeView, patientWithApptView"
						+ " WHERE 	patientAgeRangeView.PatientNum  = patientWithApptView.PatientNum "
						+ " GROUP BY ageGroup ORDER BY ageGroup");
		while (rs.next())
			spreadsheet
					.addCount(ageGroupToString(rs), 2, rs.getInt("ageCount"));
		rs.close();

		// Write appointment counts
		rs = stmt
				.executeQuery("SELECT  ageGroup, count(*) ageCount "
						+ "FROM patientAgeRangeView, patientTbl"
						+ " JOIN (apptTbl)"
						+ " ON (apptTbl.PatientNum = patientTbl.PatientNum)"
						+ " WHERE patientTbl.PatientNum = patientAgeRangeView.PatientNum AND "
						+ CANCEL_DELETED_COND
						+ " GROUP BY ageGroup ORDER BY ageGroup");
		while (rs.next())
			spreadsheet
					.addCount(ageGroupToString(rs), 3, rs.getInt("ageCount"));
		rs.close();

		// Write Family MD counts
		rs = stmt
				.executeQuery("SELECT  ageGroup, count(*) ageCount "
						+ "FROM patientAgeRangeView, patientTbl"
						+ " JOIN (providerTbl)"
						+ " ON (patientTbl.FamilyDoctor = providerTbl.OhipId)"
						+ " WHERE patientTbl.PatientNum = patientAgeRangeView.PatientNum "
						+ " GROUP BY ageGroup ORDER BY ageGroup");
		while (rs.next())
			spreadsheet
					.addCount(ageGroupToString(rs), 4, rs.getInt("ageCount"));
		rs.close();

		stmt.close();

		progressTracker_m.setProgressString("Closing "
				+ spreadsheetFile.getAbsolutePath());
		// Close spreadsheet
		spreadsheet.closeAndSave();
		warningList.addLog("", -1, "Updated file "
				+ spreadsheetFile.getAbsolutePath());
	}

	private String ageGroupToString(ResultSet rs) throws SQLException {
		return "" + (rs.getInt("ageGroup") * 5) + '-'
				+ ((rs.getInt("ageGroup") * 5) + 4);
	}

	public String getInitError() {
		return initError_m;
	}

	private void makeBillingReport(Map<String, Integer> deletedDupCounts,
			BabqWarningList warningList) throws SQLException, Exception,
			IOException {
		File dirToOutputFilesTo = BabqConfig.getOutputPathNow();
		dirToOutputFilesTo.mkdirs();

		Statement stmt = conn_m.createStatement();
		progressTracker_m.setProgressString("Extracting billing data...");

		// Open spreadsheet
		File spreadsheetFile = new File(BabqConfig
				.getPref(BabqConfig.BILLING_SUMMARY_FILE_NAME));

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy MM dd");

		BabqTeamRoleSummarySpreadsheet spreadsheet = new BabqTeamRoleSummarySpreadsheet(
				spreadsheetFile, fmt.format(new java.util.Date()),
				"resources/billingSummary.xls");

		ResultSet rs;

		// Get min and max dates
		rs = stmt.executeQuery("SELECT MIN(DateOfService) FROM billingTbl");
		if (rs.next())
			spreadsheet.setCellText(2, 1, rs.getString(1));
		rs.close();
		rs = stmt.executeQuery("SELECT MAX(DateOfService) FROM billingTbl");
		if (rs.next())
			spreadsheet.setCellText(3, 1, rs.getString(1));
		rs.close();

		if (deletedDupCounts.get("N") == null)
			deletedDupCounts.put("N", 0);
		if (deletedDupCounts.get("B") == null)
			deletedDupCounts.put("B", 0);
		if (deletedDupCounts.get("P") == null)
			deletedDupCounts.put("P", 0);
		// Get counts of no-site, bruyere and primrose so they can
		// be fairly divided into bruyere and primrose
		Map<String, Integer> specialSiteToCountMap = new HashMap<String, Integer>();
		specialSiteToCountMap.put("N", billingDb_m
				.getIntValue("SELECT COUNT(*) cnt from billingTbl"
						+ " WHERE Site='N'")
				- deletedDupCounts.get("N"));
		specialSiteToCountMap.put("P", billingDb_m
				.getIntValue("SELECT COUNT(*) cnt from billingTbl"
						+ " WHERE Site='P'")
				- deletedDupCounts.get("P"));
		specialSiteToCountMap.put("B", billingDb_m
				.getIntValue("SELECT COUNT(*) cnt from billingTbl"
						+ " WHERE Site='B'")
				- deletedDupCounts.get("B"));
		shareNCountsFairly(specialSiteToCountMap);

		// Get list of teams
		rs = stmt.executeQuery("SELECT Site, COUNT(*) cnt from billingTbl"
				+ " GROUP BY Site " + "ORDER BY Site");
		// Write teams into first column
		int totalVisits = 0;
		double pricePerVisit = BabqConfig.getCostOfService();
		while (rs.next()) {
			String site = rs.getString("Site");
			int numVisits = rs.getInt("cnt");
			if (deletedDupCounts.get(site) != null)
				numVisits -= deletedDupCounts.get(site);

			// If there are special totals (B and P have some N's included
			// in each) use these instead.
			Integer specialValue = specialSiteToCountMap.get(site);
			if (specialValue != null) {
				if (specialValue == -1)
					continue; // Skip 'N'
				numVisits = specialValue;
			}
			totalVisits += numVisits;
			progressTracker_m
					.setProgressString("Writing data for role " + site);
			spreadsheet.addTeam(site, false);
			spreadsheet.addCount(site, 1, numVisits);
			spreadsheet.addDouble(site, 2, numVisits * pricePerVisit);
		}
		rs.close();

		String total = "TOTAL";
		spreadsheet.addTeam(total, false);
		spreadsheet.addCount(total, 1, totalVisits);
		spreadsheet.addDouble(total, 2, totalVisits * pricePerVisit);

		stmt.close();

		progressTracker_m.setProgressString("Closing "
				+ spreadsheetFile.getAbsolutePath());
		// Close spreadsheet
		spreadsheet.closeAndSave();
		warningList.addLog("", -1, "Updated file "
				+ spreadsheetFile.getAbsolutePath());
	}

	/**
	 * This function takes a map with 3 counts (B for Bruyere, P for Primrose
	 * and N for No assignment) and splits the N count proportionally into the B
	 * and P counts.
	 * 
	 * The N count is set to -1 to indicate that it should be ignored.
	 * 
	 * @param specialSiteToCountMap
	 */
	private void shareNCountsFairly(Map<String, Integer> specialSiteToCountMap) {
		Integer alliedHealth = specialSiteToCountMap.get("N");
		// If no allied health, then just clear the N count and exit
		if (alliedHealth != null) {
			Integer qcBruyereVisits = specialSiteToCountMap.get("B");
			if (qcBruyereVisits == null)
				qcBruyereVisits = 0;
			Integer qcPrimroseVisits = specialSiteToCountMap.get("P");
			if (qcPrimroseVisits == null)
				qcPrimroseVisits = 0;

			int qcBruyereAlliedHealth;
			int qcPrimroseAlliedHealth;
			// If no Primrose visits then assign all to Bruyere (avoid div by 0)
			if (qcPrimroseVisits == 0) {
				qcBruyereAlliedHealth = qcBruyereVisits + alliedHealth;
				qcPrimroseAlliedHealth = 0;
			} else {
				qcBruyereAlliedHealth = (alliedHealth * qcBruyereVisits)
						/ (qcBruyereVisits + qcPrimroseVisits);
				qcPrimroseAlliedHealth = alliedHealth - qcBruyereAlliedHealth;
			}

			// Write the new counts into the map
			specialSiteToCountMap.put("B", qcBruyereAlliedHealth
					+ qcBruyereVisits);
			specialSiteToCountMap.put("P", qcPrimroseAlliedHealth
					+ qcPrimroseVisits);
		}
		// Clear the N count
		specialSiteToCountMap.put("N", -1);
	}

	
	public void setInitError(String string) {
		initError_m = string;
	}

	/**
	 * This method checks all Quebec babies to see if they have either a valid
	 * health card number or an entry in the name exceptions table,
	 * 
	 */
	
	public void checkQuebecBabiesWithoutNumbers(
			Collection<String> cardlessQcBabies, BabqWarningList warningList)
			throws SQLException {

		Statement stmt = conn_m.createStatement();
		try {
			badHealthNumList_m.clear();

			StringBuffer query = new StringBuffer(
					"SELECT * from apptTbl,patientTbl "
							+ " LEFT JOIN nameExcTbl ON nameExcTbl.BabyFirstName = patientTbl.FirstName"
							+ " AND nameExcTbl.BabySurname = patientTbl.Surname"
							+ " WHERE apptTbl.PatientNum=patientTbl.PatientNum "
							/**@Since 2015 Xmas Project*/
							+ " AND LOWER(apptTbl.Type) not like '%annual physical%' and LOWER(apptTbl.Type) not like '%phe%' and LOWER(apptTbl.Type) not like '%periodic health exam%'"
							+ "AND apptTbl.PatientNum IN (");
			int count = 0;
			for (String ptNum : cardlessQcBabies) {
				if (count++ != 0)
					query.append(",");
				query.append(ptNum);
			}
			query.append(")");

			ResultSet rs = stmt.executeQuery(query.toString());

			warningList.setFile("unknownFile");
			while (rs.next()) {

				Integer patientNumber = rs.getInt("PatientNum");

				String babyFirstName = rs.getString("FirstName");
				String babySurname = rs.getString("Surname");
				String healthCardNumber = rs
						.getString("nameExcTbl.HealthNumber");

				if (healthCardNumber == null)
					warningList.addWarning(-1,
							"No health card number in Name Exceptions for baby:\n    "
									+ babyFirstName + " " + babySurname + " (#"
									+ patientNumber + ")");
				else {
					String momSurname = rs.getString("nameExcTbl.QcSurname");
					String momFirstname = rs
							.getString("nameExcTbl.QcFirstName");
					Date dateOfExpiry = rs
							.getDate("nameExcTbl.HealthCardExpiry");

					if ((momFirstname == null) || (momSurname == null)
							|| (dateOfExpiry == null)) {
						warningList
								.addWarning(
										-1,
										"Missing mother first name, surname or expiry number Name Exceptions for baby:\n    "
												+ babyFirstName
												+ " "
												+ babySurname
												+ " (#"
												+ patientNumber + ")");

					} // mom name not OK

				}// else
			}// while
		} finally {
			stmt.close();
		}

	}
}
