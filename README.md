# Intentional OLAP: PREDICT

[![build](https://github.com/big-unibo/explain/actions/workflows/build.yml/badge.svg)](https://github.com/big-unibo/explain/actions/workflows/build.yml)

## Research papers

Implementation of the following research papers:

- Francia, Matteo, Stefano Rizzi, and Patrick Marcel. "Explaining cube measures through Intentional Analytics." **Information Systems** 121 (2024): 102338. DOI: https://doi.org/10.1016/j.is.2023.102338
- Francia, Matteo, Stefano Rizzi, and Patrick Marcel. "The Whys and Wherefores of Cubes." **Proceedings of the 25th International Workshop on Design, Optimization, Languages and Analytical Processing of Big Data (DOLAP)**. 2023. URL: https://ceur-ws.org/Vol-3369/paper4.pdf

## Running the tests

This repository allows the user to:
1. download the necessary datasets;
2. bring up a Docker container with Oracle 11g;
3. load the datasets into Oracle;
4. run the tests.

Running the experiments requires the following software to be installed:
- Docker
- Java 14
- Python 3.10

Once the software is installed, execute the following code to run the tests.

```sh
cd intentional
chmod +x *.sh
bash init.sh
bash build.sh
bash download.sh
bash start.sh
bash stop.sh
```

## Working and deploying the application

- Change the necessary files (see the ones copied by `init.sh`)
- Deploy the web application on Tomcat

```sh
./deploy.sh
```

- **Remember** to `chmod -R 777` the folders `scr/main/python` and the one containing the Oracle client 
- (If using Intellij) **Remember** to set the `LD_LIBRARY_PATH` in the run/debug configuration (see the `.env` file)
