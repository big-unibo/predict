#!/bin/bash
set -xo
set -e
echo ${PWD}
docker run -p "80:80" -v ${PWD}:/var/www/html mattrayner/lamp:latest-1804