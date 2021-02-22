@echo off

set GRADLE_PROPERTIES=gradle.properties
set ERROR_MSG=%GRADLE_PROPERTIES% file is missing
if exist %GRADLE_PROPERTIES% goto readproperties
goto fail

:readproperties
call :setpropertyvalue buildSystemVersion , buildSystemVersion

:checkpath
if exist "%VIVIDUS_BUILD_SYSTEM_HOME%" (
    goto findPath
)
if exist "%BUILD_SYSTEM_ROOT%" (
    echo.
    echo WARNING: BUILD_SYSTEM_ROOT environment variable is deprecated, use VIVIDUS_BUILD_SYSTEM_HOME instead
    set VIVIDUS_BUILD_SYSTEM_HOME=%BUILD_SYSTEM_ROOT%
) else (
    call :setpropertyvalue buildSystemRootDir , VIVIDUS_BUILD_SYSTEM_HOME
)


:findPath
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
exit /b 0

:setpropertyvalue
For /F "tokens=1* delims==" %%A IN (%GRADLE_PROPERTIES%) DO (
    IF %%A==%~1 set %~2=%%B
)
