/**
 * 
 */
package com.bronzeage.babq.main;

import java.io.File;
import java.util.Collection;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.common.BabqWarningList;
import com.bronzeage.babq.processing.BabqProcessor;

/**
 * @author andrew
 * 
 */
public class BabqMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			BabqProcessor processor = new BabqProcessor();
			processor.setProgressTracker(new BabqPrintfProgress());
			BabqWarningList warningList = new BabqWarningList();

			for (String arg : args) {
				if (arg.equals("-lappt")) {
					BabqConfig.setPref(BabqConfig.APPT_TBL_NAME,
							"/home/andrew/babq/testData/appts.txt");
					BabqConfig.setPref(BabqConfig.PATIENT_TBL_NAME,
							"/home/andrew/babq/testData/patients.txt");
					BabqConfig.setPref(BabqConfig.PROVIDER_TBL_NAME,
							"/home/andrew/babq/testData/providers.txt");

					processor.doLoadData(warningList);
				} else if (arg.equals("-lbt")) {
					BabqConfig.setPref(BabqConfig.BILLING_TBL_FILE_NAME,
							"/home/andrew/babq/testOutput/allClinics.txt");
					processor.loadBillingTbl(warningList);
				} else if (arg.equals("-sbt")) {
					System.out.println("Outputting merged data...");
					BabqConfig.setPref(BabqConfig.SUMMARY_FILE_NAME,
							"/home/andrew/babq/testOutput/summary.xls");
					BabqConfig.setPref(BabqConfig.OUTPUT_DIR_ROOT,
							"/home/andrew/babq/testOutput");
					processor.outputBillingTbl(warningList);
				} else if (arg.equals("-sbs")) {
					System.out.println("Creating forms...");
					BabqConfig.setPref(BabqConfig.QB_BILLING_TEMPLATE_FILE,
							"/home/andrew/babq/templates/qcTemplate.xls"); //
					BabqConfig.setPref(BabqConfig.OUTPUT_DIR_ROOT,
							"/home/andrew/babq/testOutput/");
					processor.makeBillingSheets(

					warningList);

				} else if (arg.equals("-merge")) {
					processor.doMakeQbBillingTbl(warningList);

				} else if (arg.equals("-valp")) {
					processor.checkQuebecHealthNumbers("patientTbl",
							warningList);
					Collection<String> cardlessQcBabies = processor
							.getQuebecBabiesWithoutNumbers();
					processor.checkQuebecBabiesWithoutNumbers(cardlessQcBabies,
							warningList);
				} else if (arg.equals("-valb")) {
					processor.checkQuebecHealthNumbers("billingTbl",
							warningList);

				} else if (arg.equals("-clear")) {
					processor.clearAllTables(warningList);
				} else {
					System.out.println("UNKNOWN OPTION: " + arg);
					System.exit(1);

				}

				System.out.println("After step: " + arg + " warnings were:");
				System.out.print(warningList.toString());
				warningList.writeWarningsToFile(new File(
						"/home/andrew/babq/testOutput/warnings.txt"));
				warningList.clear();
			}

			System.out.println("Done.");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
