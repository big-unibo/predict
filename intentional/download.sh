#!/bin/bash
set -exo
cd resources
for file in "salespurchase_v1-2023-06-07.dmp" "sales_v1-2023-06-07.dmp" "purchase_v1-2023-06-07.dmp" "SSB_FLIGHT.DMP" "foodmart-mysql.sql" "foodmart-mysql-schema.sql" "FOODMART.DMP" "FRENCHELECTRICITY.DMP" "FRENCHELECTRICITYEXT.DMP"; do
  if [ ! -f "$file" ]; then
    curl -k -o "$file" "https://big.csr.unibo.it/projects/nosql-datasets/$file"
  fi
done

if [ ! -f "COVID_WEEKLY.DMP" ]; then
  curl -k -o COVID_WEEKLY.DMP -L https://github.com/w4bo/covid-dataset/releases/download/1.0.2/COVID_WEEKLY.DMP
fi

for file in "measurement-dfm_20240417T133322.dmp" "cimice-dfm_20240417T133322.dmp"; do
  if [ ! -f "$file" ]; then
    curl -k -o "$file" -L "https://github.com/w4bo/dataset-watering/releases/download/1.0.10/$file"
  fi
done
ls -las
cd -

cd libs
for file in "instantclient-basic-linux.x64-21.1.0.0.0.zip" "instantclient-basic-windows.x64-21.3.0.0.0.zip" "db-migration-0.1.0.jar"; do
  if [ ! -f "$file" ]; then
    curl -k -o "$file" "https://big.csr.unibo.it/projects/nosql-datasets/$file"
    if [[ "$file" == *.zip ]]; then
        unzip "$file"
    fi
  fi
done
chmod -R 777 instantclient_21_1
chmod -R 777 instantclient_21_3
ls -las
cd -
