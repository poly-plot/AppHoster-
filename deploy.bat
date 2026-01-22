@echo off
REM Revonixo Backend - Automated Deployment Script for Windows
REM This script clones, builds, and runs the Revonixo P2P backend server
REM Usage: deploy.bat

echo ======================================
echo Revonixo Backend Deployment Script
echo ======================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven is not installed
    echo Please install Maven first:
    echo   Download from: https://maven.apache.org/download.cgi
    echo   Add Maven bin directory to PATH
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed
    echo Please install Java 8 or later:
    echo   Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo Checking Java version...
java -version
echo.

echo Checking Maven version...
mvn -version
echo.

echo Step 1: Cloning repository...
set REPO_DIR=AppHoster-Revonixo

if exist "%REPO_DIR%" (
    echo Repository already exists. Updating...
    cd "%REPO_DIR%"
    git pull origin copilot/analyze-redesign-potential
) else (
    git clone https://github.com/poly-plot/AppHoster-.git "%REPO_DIR%"
    cd "%REPO_DIR%"
    git checkout copilot/analyze-redesign-potential
)

echo.
echo Step 2: Building the project...
echo This may take a few minutes...
call mvn clean install -DskipTests

echo.
echo Step 3: Starting the server...
cd server

echo.
echo ======================================
echo Server is starting...
echo ======================================
echo.
echo Access points:
echo   - Admin UI: http://localhost:8080/
echo   - REST API: http://localhost:8080/rest/
echo   - WebSocket Signaling: ws://localhost:8080/signaling/{sessionId}?peerId={peerId}
echo.
echo For Android client:
echo   - Emulator: ws://10.0.2.2:8080
echo   - Physical device: ws://YOUR_LOCAL_IP:8080
echo.
echo Press Ctrl+C to stop the server
echo.

REM Run the server
call mvn tomcat7:run
