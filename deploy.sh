#!/bin/bash

# Revonixo Backend - Automated Deployment Script
# This script clones, builds, and runs the Revonixo P2P backend server
# Usage: bash deploy.sh

set -e  # Exit on any error

echo "======================================"
echo "Revonixo Backend Deployment Script"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    echo "Please install Maven first:"
    echo "  Ubuntu/Debian: sudo apt-get install maven"
    echo "  macOS: brew install maven"
    echo "  Or download from: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed${NC}"
    echo "Please install Java 8 or later:"
    echo "  Ubuntu/Debian: sudo apt-get install openjdk-11-jdk"
    echo "  macOS: brew install openjdk@11"
    exit 1
fi

echo -e "${BLUE}Checking Java version...${NC}"
java -version

echo -e "${BLUE}Checking Maven version...${NC}"
mvn -version

echo ""
echo -e "${GREEN}Step 1: Cloning repository...${NC}"
REPO_DIR="AppHoster-Revonixo"

if [ -d "$REPO_DIR" ]; then
    echo "Repository already exists. Updating..."
    cd "$REPO_DIR"
    git pull origin copilot/analyze-redesign-potential
else
    git clone https://github.com/poly-plot/AppHoster-.git "$REPO_DIR"
    cd "$REPO_DIR"
    git checkout copilot/analyze-redesign-potential
fi

echo ""
echo -e "${GREEN}Step 2: Building the project...${NC}"
echo "This may take a few minutes..."
mvn clean install -DskipTests

echo ""
echo -e "${GREEN}Step 3: Starting the server...${NC}"
cd server

echo ""
echo -e "${BLUE}======================================"
echo "Server is starting..."
echo "======================================${NC}"
echo ""
echo -e "${GREEN}Access points:${NC}"
echo "  - Admin UI: http://localhost:8080/"
echo "  - REST API: http://localhost:8080/rest/"
echo "  - WebSocket Signaling: ws://localhost:8080/signaling/{sessionId}?peerId={peerId}"
echo ""
echo -e "${BLUE}For Android client:${NC}"
echo "  - Emulator: ws://10.0.2.2:8080"
echo "  - Physical device: ws://YOUR_LOCAL_IP:8080"
echo ""
echo -e "${BLUE}Press Ctrl+C to stop the server${NC}"
echo ""

# Run the server
mvn tomcat7:run
