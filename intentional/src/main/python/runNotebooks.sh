#!/bin/bash
set -exo
if [ -d venv/bin ]; then
    source venv/bin/activate
else
    source venv/Scripts/activate
fi
for f in *.ipynb;
    do echo "Processing $f file...";
    jupyter nbconvert --execute --to notebook --inplace $f
done
