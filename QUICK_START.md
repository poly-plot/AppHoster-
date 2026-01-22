# Revonixo Backend - Quick Start Guide

## One-Command Deployment

### Prerequisites
- **Java 8+** installed
- **Maven 3.6+** installed
- **Git** installed

### Linux/Mac

```bash
curl -sSL https://raw.githubusercontent.com/poly-plot/AppHoster-/copilot/analyze-redesign-potential/deploy.sh | bash
```

Or download and run:
```bash
wget https://raw.githubusercontent.com/poly-plot/AppHoster-/copilot/analyze-redesign-potential/deploy.sh
chmod +x deploy.sh
./deploy.sh
```

### Windows

Download and run:
```cmd
curl -O https://raw.githubusercontent.com/poly-plot/AppHoster-/copilot/analyze-redesign-potential/deploy.bat
deploy.bat
```

Or using PowerShell:
```powershell
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/poly-plot/AppHoster-/copilot/analyze-redesign-potential/deploy.bat" -OutFile "deploy.bat"
.\deploy.bat
```

## What the Script Does

1. ✅ Checks for Java and Maven installation
2. ✅ Clones the repository (or updates if exists)
3. ✅ Checks out the P2P feature branch
4. ✅ Builds the entire project
5. ✅ Starts the Tomcat server
6. ✅ Displays access URLs

## Access Points

Once the server is running:

- **Admin UI**: http://localhost:8080/
- **REST API**: http://localhost:8080/rest/
- **WebSocket Signaling**: ws://localhost:8080/signaling/{sessionId}?peerId={peerId}

### For Android Client

- **Emulator**: `ws://10.0.2.2:8080`
- **Physical Device**: `ws://YOUR_LOCAL_IP:8080` (replace YOUR_LOCAL_IP with your machine's IP)

## Manual Installation (If Script Fails)

```bash
# 1. Clone repository
git clone https://github.com/poly-plot/AppHoster-.git
cd AppHoster-

# 2. Checkout P2P branch
git checkout copilot/analyze-redesign-potential

# 3. Build project
mvn clean install -DskipTests

# 4. Run server
cd server
mvn tomcat7:run
```

## Troubleshooting

### Maven Not Found
- **Linux**: `sudo apt-get install maven`
- **Mac**: `brew install maven`
- **Windows**: Download from https://maven.apache.org/download.cgi

### Java Not Found
- **Linux**: `sudo apt-get install openjdk-11-jdk`
- **Mac**: `brew install openjdk@11`
- **Windows**: Download from https://adoptium.net/

### Port 8080 Already in Use
```bash
# Find process using port 8080
# Linux/Mac:
lsof -i :8080

# Windows:
netstat -ano | findstr :8080

# Kill the process or change the port in server/pom.xml
```

### Build Fails
```bash
# Clean Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository
mvn install -DskipTests
```

## Database Configuration

By default, the server uses an embedded database. For production:

1. Install PostgreSQL
2. Create database: `createdb headwind`
3. Update `server/src/main/resources/dbconfig.properties`:
   ```properties
   jdbc.url=jdbc:postgresql://localhost:5432/headwind
   jdbc.username=postgres
   jdbc.password=yourpassword
   ```

## Stop the Server

Press `Ctrl+C` in the terminal where the server is running.

## What's Implemented

✅ P2P networking foundation (JmDNS + DHT)  
✅ WebSocket signaling server  
✅ Rental session management  
✅ NAT traversal support (STUN/TURN)  
✅ Database schema (7 tables)  
✅ Security (Bouncycastle encryption)  
✅ Android integration guide  

## Next Steps

1. **Test the server**: Visit http://localhost:8080/
2. **Build Android client**: Follow `ANDROID_INTEGRATION_GUIDE.md`
3. **Connect via WebSocket**: Use test endpoint ws://localhost:8080/signaling/test-session-123?peerId=peer1

## Support

For issues or questions:
- Check `ANDROID_INTEGRATION_GUIDE.md` for client integration
- Review `p2p/GLOBAL_P2P_ARCHITECTURE.md` for architecture details
- See `server/src/main/java/com/hmdm/signaling/WEBSOCKET_SIGNALING.md` for WebSocket details
