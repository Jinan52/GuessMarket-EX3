@echo off
setlocal
cd /d "%~dp0"

set "JAVA_EXE="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    )
)
if not defined JAVA_EXE (
    where java >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Java was not found.
        echo Install 64-bit JDK 25 and set JAVA_HOME to that JDK.
        pause
        exit /b 1
    )
    set "JAVA_EXE=java"
)

"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: The Java command could not be started.
    echo Install 64-bit JDK 25 and set JAVA_HOME to that JDK.
    pause
    exit /b 1
)

if not exist "guess-market-javafx-ui.jar" (
    echo ERROR: guess-market-javafx-ui.jar is missing from the client folder.
    pause
    exit /b 1
)
if not exist "lib\guess-market-engine.jar" (
    echo ERROR: lib\guess-market-engine.jar is missing.
    pause
    exit /b 1
)
if not exist "lib\gson-2.11.0.jar" (
    echo ERROR: lib\gson-2.11.0.jar is missing.
    pause
    exit /b 1
)
if not exist "lib\javafx\lib\javafx.controls.jar" (
    echo ERROR: JavaFX runtime JARs are missing under lib\javafx\lib
    pause
    exit /b 1
)

set "PATH=%CD%\lib\javafx\bin;%PATH%"

"%JAVA_EXE%" ^
  --module-path "lib\javafx\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "guess-market-javafx-ui.jar;lib\guess-market-engine.jar;lib\gson-2.11.0.jar" ^
  guessmarket.ui.GuessMarketApplication
if errorlevel 1 (
    echo.
    echo The Guess Market client ended with an error.
    echo Required: 64-bit JDK 25, Tomcat at http://localhost:8080/guess-market
    pause
    exit /b 1
)
endlocal
