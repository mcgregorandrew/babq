# babq
Bruyere Academic Health Team Billing for Quebec Patients

This is a tool used periodically to generate billing spreadsheets for
the Bruyere Academic Family Health Team.  It consumes output from the
NetMedical EMR system in the form of Patient and Appointement records
and then outputs one of the following:

- a CSV file of all appointments extended with the patient
information.  This is used in ad-hoc analysis (e.g. how are geriatric
patients served by different doctors).
- a billing file which can be sent to the billing department
- the billing department can load the billing file and output the forms
that are required by the Quebec Ministry of Health.