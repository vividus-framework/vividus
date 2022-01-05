@echo off

set GRADLE_PROPERTIES=gradle.properties
set ERROR_MSG=%GRADLE_PROPERTIES% file is missing
if exist %GRADLE_PROPERTIES% goto readproperties
goto fail

:readproperties
call :setpropertyvalue buildSystemVersion , buildSystemVersion

:checkpath
if not exist "%VIVIDUS_BUILD_SYSTEM_HOME%" (
    call :setpropertyvalue buildSystemRootDir , VIVIDUS_BUILD_SYSTEM_HOME
)
set GRADLEW_PATH=%VIVIDUS_BUILD_SYSTEM_HOME%\%buildSystemVersion%\gradlew.bat
if exist "%GRADLEW_PATH%" goto call
set ERROR_MSG=Couldn't find %GRADLEW_PATH%. Neither environment variable "VIVIDUS_BUILD_SYSTEM_HOME" is set nor embedded build system is synced

:fail
echo.
echo ERROR: %ERROR_MSG%
echo Please check Build System guide:
echo https://github.com/vividus-framework/vividus-build-system
echo or
echo clone this repo recursively: git clone --recursive <git-repository-url>
exit /b 1

:call
@CALL "%GRADLEW_PATH%" %*
exit /b %ERRORLEVEL%

:setpropertyvalue
For /F "tokens=1* delims==" %%A IN (%GRADLE_PROPERTIES%) DO (
    IF %%A==%~1 set %~2=%%B
)
