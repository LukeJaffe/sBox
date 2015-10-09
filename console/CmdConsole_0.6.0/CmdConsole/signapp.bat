@echo off

setlocal

set APPNAME=CmdConsole
set KEYPATH=..\..\mykey

REM specify version or null
set APK_VER=""
if "%1" == "" goto next_check
set APK_VER=_%1

:next_check
if not APK_VER == "" goto next_check_2
echo
echo Warning: No version number is specified
echo

:next_check_2
REM delete previously generated apk
del /Q release\%APPNAME%%APK_VER%.apk

if exist %KEYPATH%\keyenv.bat goto check_unsigned_apk
echo Can't find "%KEYPATH%\keyenv.bat"
goto end

:check_unsigned_apk
if exist release\%APPNAME%-unsigned.apk goto normal_sign
echo Can't find "release\%APPNAME%-unsigned.apk"
goto end

:normal_sign
REM
REM call this before calling jarsigner
REM
call %KEYPATH%\keyenv.bat

REM make a copy first
copy release\%APPNAME%-unsigned.apk release\%APPNAME%-signed.apk

REM
REM sign apk
REM
jarsigner -verbose -keystore %KEYPATH%\sss_release.keystore -storepass %STOREPASS% release\%APPNAME%-signed.apk ssskey

REM
REM verify (optional)
REM
REM > jarsigner -verify release\%APPNAME%-signed.apk
REM

REM
REM zipalign finally
REM
zipalign -v 4 release\%APPNAME%-signed.apk release\%APPNAME%%APK_VER%.apk

REM
REM Delete temp apk
REM
del /Q release\%APPNAME%-signed.apk

:end

endlocal
