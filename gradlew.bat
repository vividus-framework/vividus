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
if exist "%GRADLEW_PATH%" goto checkprojectdir
set ERROR_MSG=Couldn't find %GRADLEW_PATH%. Neither environment variable "VIVIDUS_BUILD_SYSTEM_HOME" is set nor embedded build system is synced

:fail
echo.
echo ERROR: %ERROR_MSG%
echo Please check Build System guide:
echo https://github.com/vividus-framework/vividus-build-system
echo or
echo clone this repo recursively: git clone --recursive <git-repository-url>
exit /b 1

:checkprojectdir
set "PROJECT_DIR_PROVIDED=false"

for %%i in (%*) do (
    if "%%i"=="--project-dir" set "PROJECT_DIR_PROVIDED=true"
    if "%%i"=="-p" set "PROJECT_DIR_PROVIDED=true"
)

if "%PROJECT_DIR_PROVIDED%"=="true" (
    @CALL "%GRADLEW_PATH%" %*
) else (
    set "SCRIPT_DIR=%~dp0"
    if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
    @CALL "%GRADLEW_PATH%" "--project-dir" "%SCRIPT_DIR%" %*
)
exit /b %ERRORLEVEL%

:setpropertyvalue
For /F "tokens=1* delims==" %%A IN (%GRADLE_PROPERTIES%) DO (
    IF %%A==%~1 set %~2=%%B
)
