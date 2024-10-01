#!/bin/bash
rm -rf resources/intention/output/*.csv
rm -rf resources/intention/output/*.json
rm -f resources/.ready
docker compose down