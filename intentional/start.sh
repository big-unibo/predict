#!/bin/bash
set -exo

if [ -f .env ]; then
  set -a
  source ./.env
  set +a
fi

./stop.sh
docker compose up --build -d

./wait-for-it.sh ${ORACLE_IP}:${ORACLE_PORT} --strict --timeout=0 -- echo "ORACLE is up"

export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}

until [ -f resources/.ready ]
do
     docker logs oracledb | tail -n 10
     sleep 10
done
echo "All databases have been imported!"

./gradlew --stacktrace --scan