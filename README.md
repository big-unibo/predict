# Intentional OLAP: PREDICT

[![build](https://github.com/w4bo/predict/actions/workflows/build.yml/badge.svg)](https://github.com/w4bo/predict/actions/workflows/build.yml)

## Research papers

This repository implements the paper "Predicting Multidimensional Cubes Through Intentional Analytics" submitted to the IEEE TKDE journal.

## Running the tests

This repository allows the user to:
1. download the necessary datasets;
2. bring up a Docker container with Oracle 11g;
3. load the datasets into Oracle;
4. run the tests.

Running the experiments requires the following software to be installed:
- Docker
- Java 14
- Python 3.12

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
