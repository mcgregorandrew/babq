/**
 * 
 */
package com.bronzeage.babq.common;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.bronzeage.babq.db.BabqDbNameExceptions;

/**
 * @author andrew
 * 
 */
public class BabqConfig {

	// Settable in initial screen
	public static final String DIR_ROOT = "dirRoot";

	// Set internally
	public static final String PREFS_INITIALIZED = "prefsInitialized";

	// Set in dialogs
	public static final String PATIENT_TBL_NAME = "patientTblFileName";
	public static final String PROVIDER_TBL_NAME = "providerTblFileName";
	public static final String APPT_TBL_NAME = "apptTblFileName";
	public static final String BILLING_TBL_FILE_NAME = "billingTblFileName";
	public static final String SERVICE_PRICE = "servicePrice";
	public static final String FIX_EXPIRY_ERRORS = "fixExpiryErrors";
	public static final String IGNORE_EXPIRY_ERRORS = "ignoreExpiryErrors";
	public static final String IGNORE_FAMILYMD_ERRORS = "ignoreFamilyMdErrors";
	public static final String JDBC_URI = "jdbcUri";
	public static final String AUTO_CLEAR_DATA = "autoClearData";
	public static final String EXC_NAME_FILE_NAME = "excNameTblFileName";

	// Set in settings
	public static final String QB_BILLING_TEMPLATE_FILE = "billingXlsTemplateFile";

	// Never set, always generated from root
	public static final String OUTPUT_DIR_ROOT = "outputDirRoot";
	public static final String INPUT_DIR_NOW = "inputDirNow";
	public static final String SUMMARY_FILE_NAME = "summaryFileName";
	public static final String COMBO_TBL_NAME = "comboTblFileName";
	public static final String TEAM_SUMMARY_FILE_NAME = "teamSummaryFileName";
	public static final String ROLE_SUMMARY_FILE_NAME = "roleSummaryFileName";
	public static final String PROVIDER_SUMMARY_FILE_NAME = "providerSummaryFileName";
	public static final String AGE_SUMMARY_FILE_NAME = "ageSummaryFileName";
	public static final String BILLING_SUMMARY_FILE_NAME = "billingSummaryFileName";

	private static Double costOfService_m = null;
	private static Boolean fixExpDates_m = null;
	private static Boolean ignoreExpiryDateErrors_m = null;
	private static boolean useH2ByDefault_m = true;
	private static long supressExpTimeInMillis_m = new GregorianCalendar(2006,
			11, 25).getTimeInMillis();

	private static String versionCode_ms = "5.1";
	private static String dateCode_ms = "Aug 04, 2018";

	private static Set<String> settablePrefs_ms = null;

	static {
		settablePrefs_ms = new HashSet<String>();
		settablePrefs_ms.add(PREFS_INITIALIZED);
		settablePrefs_ms.add(DIR_ROOT);
		settablePrefs_ms.add(PATIENT_TBL_NAME);
		settablePrefs_ms.add(PROVIDER_TBL_NAME);
		settablePrefs_ms.add(APPT_TBL_NAME);
		settablePrefs_ms.add(BILLING_TBL_FILE_NAME);
		settablePrefs_ms.add(QB_BILLING_TEMPLATE_FILE);
		settablePrefs_ms.add(SERVICE_PRICE);
		settablePrefs_ms.add(FIX_EXPIRY_ERRORS);
		settablePrefs_ms.add(IGNORE_EXPIRY_ERRORS);
		settablePrefs_ms.add(IGNORE_FAMILYMD_ERRORS);
		settablePrefs_ms.add(JDBC_URI);
		settablePrefs_ms.add(AUTO_CLEAR_DATA);
		settablePrefs_ms.add(EXC_NAME_FILE_NAME);

	}

	public static String getVersionCode() {
		return versionCode_ms;
	}

	public static String getDateCode() {
		return dateCode_ms;
	}

	private static Preferences prefs_m = Preferences
			.userNodeForPackage(BabqConfig.class);
	private static Boolean ignoreFamilyMdErrors_m;

	public static double getCostOfService() {
		if (costOfService_m == null)
			costOfService_m = Double.parseDouble(getPref(SERVICE_PRICE));
		return costOfService_m;
	}

	public static boolean fixExpiryDates() {
		if (fixExpDates_m == null)
			fixExpDates_m = Boolean.parseBoolean(prefs_m.get(FIX_EXPIRY_ERRORS,
					"true"));

		return fixExpDates_m;
	}

	public static boolean ignoreExpiryDates() {
		if (ignoreExpiryDateErrors_m == null)
			ignoreExpiryDateErrors_m = Boolean.parseBoolean(prefs_m.get(
					IGNORE_EXPIRY_ERRORS, "false"));
		return ignoreExpiryDateErrors_m;
	}

	public static boolean ignoreFamilyMdErrors() {
		if (ignoreFamilyMdErrors_m == null)
			ignoreFamilyMdErrors_m = Boolean.parseBoolean(prefs_m.get(
					IGNORE_FAMILYMD_ERRORS, "false"));
		return ignoreFamilyMdErrors_m;
	}

	public static String getJdbcInfo() {
		return getPref(BabqConfig.JDBC_URI);
	}

	public static String getSqlDriver() {
		if (isH2())
			return "org.h2.Driver";
		else
			return "com.mysql.jdbc.Driver";
	}

	public static boolean isH2() {
		return getJdbcInfo().contains("h2");
	}

	public static long getSupressExpTimeInMillis() {
		return supressExpTimeInMillis_m;
	}

	public static void clearPrefs() throws BackingStoreException {
		prefs_m.clear();
	}

	public static void saveValues() throws BackingStoreException {
		prefs_m.flush();
	}

	public static void setPref(String prefName, String text) {
		if (!settablePrefs_ms.contains(prefName))
			throw new Error("Preference " + prefName + " is not settable");

		prefs_m.put(PREFS_INITIALIZED, "true");

		prefs_m.put(prefName, text);
		if (prefName.equals(SERVICE_PRICE))
			costOfService_m = null;
		if (prefName.equals(FIX_EXPIRY_ERRORS))
			fixExpDates_m = null;
		if (prefName.equals(IGNORE_EXPIRY_ERRORS))
			ignoreExpiryDateErrors_m = null;
		if (prefName.equals(IGNORE_FAMILYMD_ERRORS))
			ignoreFamilyMdErrors_m = null;

	}

	public static String getPref(String name) {
		String result = prefs_m.get(name, null);
		if (result == null) {

			if (name.equals(SERVICE_PRICE))
				result = "238.00";
			else if (name.equals(QB_BILLING_TEMPLATE_FILE))
				result = getDefaultPath("templateFiles",
						"qcBillingTemplate.xls");
			else if (name.equals(OUTPUT_DIR_ROOT))
				result = getDefaultPath("outputFiles", null);
			else if (name.equals(SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "summary.xls");
			else if (name.equals(BILLING_SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "billingSummary.xls");
			else if (name.equals(TEAM_SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "teamSummary.xls");
			else if (name.equals(ROLE_SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "roleSummary.xls");
			else if (name.equals(PROVIDER_SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "providerSummary.xls");
			else if (name.equals(AGE_SUMMARY_FILE_NAME))
				result = getDefaultPath("updatedFiles", "ageSummary.xls");
			else if (name.equals(EXC_NAME_FILE_NAME))
				result = getDefaultPath("updatedFiles", "nameExceptions.csv");
			else if (name.equals(FIX_EXPIRY_ERRORS))
				result = "true";
			else if (name.equals(IGNORE_EXPIRY_ERRORS))
				result = "false";
			else if (name.equals(IGNORE_FAMILYMD_ERRORS))
				result = "true";
			else if (name.equals(PREFS_INITIALIZED))
				result = "false";
			else if (name.equals(JDBC_URI))
				if (useH2ByDefault_m) {
					File dir = new File(System.getProperty("java.io.tmpdir"),
							"babqDb");
					dir.mkdirs();
					result = "jdbc:h2:" + dir.getAbsolutePath() + "/babq";
				} else
					result = "jdbc:mysql://localhost/?user=root&password=";
			else if (name.equals(AUTO_CLEAR_DATA))
				result = "true";
			else if (name.equals(COMBO_TBL_NAME))
				result = (new File(getOutputPathNow(), "comboTable.txt"))
						.getAbsolutePath();

		}

		return result;
	}

	public static File getOutputPathNow() {
		// Set up output dir for this run
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String dirName = fmt.format(new java.util.Date());
		File outputDir = new File(BabqConfig
				.getPref(BabqConfig.OUTPUT_DIR_ROOT));
		File outputNowDir = new File(outputDir, dirName);
		if (!outputNowDir.exists())
			outputNowDir.mkdirs();
		return outputNowDir;
	}

	private static String getDefaultPath(String dir, String file) {
		File path = new File(getPref(DIR_ROOT));
		if (dir != null)
			path = new File(path, dir);
		if (file != null)
			path = new File(path, file);
		return path.getAbsolutePath();
	}

	public static void createEmptyExcNameFile(File file) throws Exception {
		BabqDbNameExceptions excTbl = new BabqDbNameExceptions(null);
		excTbl.ouputRsToCsv(null, file, null);
	}

	public static URI getHelpUri() {
		try {
			return new URI("http://bronzeagesoftware.com/babq/");
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
