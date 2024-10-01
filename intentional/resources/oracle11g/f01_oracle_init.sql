CREATE DIRECTORY ORACLE_DUMP as '/data';

create user foodmart identified by oracle;
create user cimice identified by oracle;
create user measurement identified by oracle;
create user frenchelectricity identified by oracle;
create user frenchelectricityext identified by oracle;
create user covid_weekly identified by oracle;
create user ssb_flight identified by oracle;
create user sales_v1 identified by oracle;
create user purchase_v1 identified by oracle;
create user salespurchase_v1 identified by oracle;

grant all privileges to cimice;
grant all privileges to measurement;
grant all privileges to foodmart;
grant all privileges to frenchelectricity;
grant all privileges to frenchelectricityext;
grant all privileges to covid_weekly;
grant all privileges to ssb_flight;
grant all privileges to sales_v1;
grant all privileges to purchase_v1;
grant all privileges to salespurchase_v1;

grant read, write on directory oracle_dump to system;
grant read, write on directory oracle_dump to cimice;
grant read, write on directory oracle_dump to measurement;
grant read, write on directory oracle_dump to foodmart;
grant read, write on directory oracle_dump to frenchelectricity;
grant read, write on directory oracle_dump to frenchelectricityext;
grant read, write on directory oracle_dump to covid_weekly;
grant read, write on directory oracle_dump to ssb_flight;
grant read, write on directory oracle_dump to sales_v1;
grant read, write on directory oracle_dump to purchase_v1;
grant read, write on directory oracle_dump to salespurchase_v1;
