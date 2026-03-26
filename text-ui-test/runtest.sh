#!/usr/bin/env bash

# change to script directory
cd "${0%/*}"

cd ..
rm -f data/CG2StocksTracker.txt
./gradlew clean shadowJar

java  -jar $(find build/libs/ -mindepth 1 -print -quit) < text-ui-test/input.txt > text-ui-test/ACTUAL.TXT

cd text-ui-test

cp EXPECTED.TXT EXPECTED-UNIX.TXT
dos2unix EXPECTED-UNIX.TXT ACTUAL.TXT
diff EXPECTED-UNIX.TXT ACTUAL.TXT
if [ $? -eq 0 ]
then
    echo "Test passed!"
    exit 0
else
    echo "Test failed!"
    exit 1
fi
