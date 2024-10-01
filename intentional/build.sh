#!/bin/bash
set -e
sudo apt-get -y install libaio1 libaio-dev
docker compose build --no-cache