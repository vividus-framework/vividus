@echo off

set GRADLE_PROPERTIES=gradle.properties
set ERROR_MSG=%GRADLE_PROPERTIES% file is missing
if exist %GRADLE_PROPERTIES% goto readproperies
goto fail

:readproperies
For /F "tokens=1* delims==" %%A IN (gradle.properties) DO (
    IF "%%A"=="buildSystemVersion" set buildSystemVersion=%%B
    )
set ERROR_MSG=buildSystemVersion is NOT defined in %GRADLE_PROPERTIES% file
if "%buildSystemVersion%"=="" goto fail

goto checkpath

:fail
echo.
echo ERROR: %ERROR_MSG%
echo.
echo Please check Build System guide:
echo https://Vividus-BuildSystem
exit /b 1

:checkpath
if not exist "%BUILD_SYSTEM_ROOT%" (
    set BUILD_SYSTEM_ROOT=vividus-build-system)
set GRADLEW_PATH=%BUILD_SYSTEM_ROOT%\%buildSystemVersion%\gradlew.bat
if exist "%GRADLEW_PATH%" goto call
echo.
echo ERROR: Couldn't find %GRADLEW_PATH%
echo Check BUILD_SYSTEM_ROOT and buildSystemVersion set correctly
echo.
echo Please check Build System guide:
echo https://github.com/vividus-framework/vividus-build-system
echo or
echo clone this repo recursively: git clone --recursive <git-repository-url>
exit /b 1

:call
@CALL "%GRADLEW_PATH%" %*
