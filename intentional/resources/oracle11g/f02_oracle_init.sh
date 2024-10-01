chmod -R 777 /data
ls -las /data
# expdp sales_v1/oracle@xe DIRECTORY=ORACLE_DUMP DUMPFILE=sales_v1-2023-06-07.dmp SCHEMAS=sales_v1
impdp foodmart/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=FOODMART.DMP SCHEMAS=foodmart
impdp cimice/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=cimice-dfm_20240417T133322.dmp SCHEMAS=cimice
impdp measurement/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=measurement-dfm_20240417T133322.dmp SCHEMAS=measurement
impdp covid_weekly/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=COVID_WEEKLY.DMP SCHEMAS=covid_weekly
impdp frenchelectricity/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=FRENCHELECTRICITY.DMP SCHEMAS=frenchelectricity
impdp frenchelectricityext/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=FRENCHELECTRICITYEXT.DMP SCHEMAS=frenchelectricityext
impdp ssb_flight/oracle@127.0.0.1:1521/xe DIRECTORY=ORACLE_DUMP DUMPFILE=SSB_FLIGHT.DMP SCHEMAS=ssb_flight
impdp sales_v1/oracle@xe DIRECTORY=ORACLE_DUMP DUMPFILE=sales_v1-2023-06-07.dmp SCHEMAS=sales_v1
impdp purchase_v1/oracle@xe DIRECTORY=ORACLE_DUMP DUMPFILE=purchase_v1-2023-06-07.dmp SCHEMAS=purchase_v1
impdp salespurchase_v1/oracle@xe DIRECTORY=ORACLE_DUMP DUMPFILE=salespurchase_v1-2023-06-07.dmp SCHEMAS=salespurchase_v1
touch /data/.ready
