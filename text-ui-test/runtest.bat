@echo off
setlocal enableextensions
pushd %~dp0

cd ..
if exist data\CG2StocksTracker.txt del /q data\CG2StocksTracker.txt
call gradlew clean shadowJar

for /f "tokens=*" %%a in (
    'dir /b build\libs\*.jar'
) do (
    set jarloc=build\libs\%%a
)

java -jar %jarloc% < text-ui-test\input.txt > text-ui-test\ACTUAL.TXT

cd text-ui-test

FC ACTUAL.TXT EXPECTED.TXT >NUL && ECHO Test passed! || Echo Test failed!
