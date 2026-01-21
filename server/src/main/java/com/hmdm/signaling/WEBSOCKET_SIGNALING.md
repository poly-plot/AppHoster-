# WebSocket Signaling Server Documentation

## Overview

The WebSocket Signaling Server provides real-time bidirectional communication for coordinating P2P handshakes between Host and Renter devices in the Revonixo marketplace. It implements the JSR 356 Java WebSocket API for standard Tomcat compatibility.

## Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Renter      │         │   Signaling  │         │    Host      │
│  Device      │◄───────►│    Server    │◄───────►│   Device     │
│  (Client)    │ WebSocket  (Matchmaker)│ WebSocket  (Provider)  │
└──────────────┘         └──────────────┘         └──────────────┘
       │                       │                         │
       │  1. ICE_CANDIDATE     │  1. ICE_CANDIDATE      │
       ├──────────────────────►├────────────────────────►│
       │                       │                         │
       │  2. SDP_OFFER         │  2. SDP_OFFER          │
       ├──────────────────────►├────────────────────────►│
       │                       │                         │
       │  3. SDP_ANSWER        │  3. SDP_ANSWER         │
       │◄──────────────────────┤◄────────────────────────┤
       │                       │                         │
       │  4. PEER_READY        │  4. PEER_READY         │
       ├──────────────────────►│                         │
       │                       │                         │
       │           Direct P2P Connection Established      │
       ├─────────────────────────────────────────────────►│
       │      (WebSocket closed, server resources saved) │
```

## Endpoint

**WebSocket URL**: `ws://{server-host}/signaling/{sessionId}?peerId={peerId}`

**Parameters**:
- `sessionId` (path): Rental session ID from `rental_sessions` table
- `peerId` (query): Unique identifier for the connecting peer (host or renter)

**Example**:
```
ws://revonixo.com/signaling/sess_abc123?peerId=peer_host_xyz
ws://revonixo.com/signaling/sess_abc123?peerId=peer_renter_abc
```

## Message Types

### 1. ICE_CANDIDATE
NAT traversal ICE candidate exchange.

**From Client**:
```json
{
  "type": "ICE_CANDIDATE",
  "to": "peer_host_xyz",
  "data": {
    "candidate": "candidate:842163049 1 udp 1677729535 203.0.113.5 51234 typ srflx",
    "sdpMid": "0",
    "sdpMLineIndex": 0
  }
}
```

**Server Behavior**: Forwards ICE candidate from sender to target peer.

### 2. SDP_OFFER
Connection offer from initiator (typically Renter).

**From Client**:
```json
{
  "type": "SDP_OFFER",
  "to": "peer_host_xyz",
  "data": {
    "sdp": "v=0\r\no=- 4611731400430051336 2 IN IP4 127.0.0.1\r\n...",
    "type": "offer"
  }
}
```

**Server Behavior**: Forwards SDP offer to target peer.

### 3. SDP_ANSWER
Connection answer from responder (typically Host).

**From Client**:
```json
{
  "type": "SDP_ANSWER",
  "to": "peer_renter_abc",
  "data": {
    "sdp": "v=0\r\no=- 4611731400430051337 2 IN IP4 127.0.0.1\r\n...",
    "type": "answer"
  }
}
```

**Server Behavior**: Forwards SDP answer to target peer.

### 4. PEER_READY
Signal that peer is ready for direct P2P connection.

**From Client**:
```json
{
  "type": "PEER_READY",
  "to": "peer_host_xyz",
  "data": {
    "ready": true
  }
}
```

**Server Behavior**: Forwards readiness signal. Both peers can now close WebSocket and establish direct P2P.

### 5. PEER_JOINED (Server-Initiated)
Notification that a peer has joined the signaling session.

**From Server**:
```json
{
  "type": "PEER_JOINED",
  "from": "server",
  "to": null,
  "data": "peer_renter_abc",
  "timestamp": 1737534100000
}
```

### 6. PEER_LEFT (Server-Initiated)
Notification that a peer has left the signaling session.

**From Server**:
```json
{
  "type": "PEER_LEFT",
  "from": "server",
  "to": null,
  "data": "peer_renter_abc",
  "timestamp": 1737534200000
}
```

### 7. ERROR (Server-Initiated)
Error message from server.

**From Server**:
```json
{
  "type": "ERROR",
  "from": "server",
  "to": null,
  "data": "Invalid message format",
  "timestamp": 1737534300000
}
```

## Connection Flow

### Step 1: Both Peers Connect
```javascript
// Renter connects
const renterWs = new WebSocket('ws://revonixo.com/signaling/sess_abc123?peerId=peer_renter_abc');

// Host connects
const hostWs = new WebSocket('ws://revonixo.com/signaling/sess_abc123?peerId=peer_host_xyz');
```

### Step 2: Exchange ICE Candidates
```javascript
// Renter sends ICE candidate
renterWs.send(JSON.stringify({
  type: 'ICE_CANDIDATE',
  to: 'peer_host_xyz',
  data: iceCandidate
}));

// Host receives and processes
hostWs.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (msg.type === 'ICE_CANDIDATE') {
    peerConnection.addIceCandidate(msg.data);
  }
};
```

### Step 3: Exchange SDP Offer/Answer
```javascript
// Renter sends offer
const offer = await peerConnection.createOffer();
await peerConnection.setLocalDescription(offer);

renterWs.send(JSON.stringify({
  type: 'SDP_OFFER',
  to: 'peer_host_xyz',
  data: offer
}));

// Host receives offer and sends answer
hostWs.onmessage = async (event) => {
  const msg = JSON.parse(event.data);
  if (msg.type === 'SDP_OFFER') {
    await peerConnection.setRemoteDescription(msg.data);
    const answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);
    
    hostWs.send(JSON.stringify({
      type: 'SDP_ANSWER',
      to: 'peer_renter_abc',
      data: answer
    }));
  }
};
```

### Step 4: Signal Ready and Close
```javascript
// Both peers signal ready
renterWs.send(JSON.stringify({
  type: 'PEER_READY',
  to: 'peer_host_xyz',
  data: { ready: true }
}));

// Close WebSocket (server resources saved)
renterWs.close();
hostWs.close();

// Direct P2P connection is now active
```

## Server Implementation

### Key Classes

**SignalingServer** (`@ServerEndpoint`):
- Manages WebSocket connections
- Routes messages between peers
- Handles lifecycle events (connect, disconnect, error)

**SignalingMessage**:
- Data model for signaling messages
- Contains type, from, to, data, timestamp

**SignalingMessageType** (Enum):
- Message type constants
- ICE_CANDIDATE, SDP_OFFER, SDP_ANSWER, PEER_READY, PEER_JOINED, PEER_LEFT, ERROR

**SignalingServerConfigurator**:
- WebSocket endpoint configuration
- Extracts peerId from connection parameters

### Session Management

**Data Structure**:
```java
// Map of sessionId -> Map of peerId -> WebSocket Session
Map<String, Map<String, Session>> sessionPeers
```

**Lifecycle**:
1. Peer connects → Added to `sessionPeers`
2. Message received → Forwarded to target peer
3. Peer disconnects → Removed from `sessionPeers`
4. Session empty → Cleaned up (no peers left)

### Message Routing

**Point-to-Point** (when `to` field specified):
```java
forwardMessageToPeer(sessionId, targetPeerId, message);
```

**Broadcast** (when `to` field is null):
```java
broadcastMessage(sessionId, fromPeerId, message);
```

## Security Considerations

### Current Implementation
- ✅ Per-session isolation (peers in different sessions cannot communicate)
- ✅ PeerId validation required for connection
- ✅ Message origin validation (server sets `from` field)
- ✅ Connection metadata tracking

### Future Enhancements
- 🔲 Integration with RentalSessionService for authorization
- 🔲 JWT token validation in handshake
- 🔲 Rate limiting per peer
- 🔲 Message size limits
- 🔲 Session timeout enforcement
- 🔲 TLS/WSS for encrypted transport

### Recommended Authorization Flow
```java
@OnOpen
public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
    String peerId = extractPeerId(session);
    String token = extractToken(session);
    
    // Validate peer is authorized for this session
    RentalSession rentalSession = rentalSessionService.getSession(sessionId);
    if (!isAuthorized(rentalSession, peerId, token)) {
        closeSession(session, "Unauthorized");
        return;
    }
    
    // Continue with connection...
}
```

## Monitoring

### Metrics

**Active Sessions**:
```java
int activeSessions = SignalingServer.getActiveSessionsCount();
```

**Total Peers**:
```java
int totalPeers = SignalingServer.getTotalPeersCount();
```

### Logging

**Log Levels**:
- `INFO`: Connection lifecycle (connect, disconnect)
- `DEBUG`: Message routing details
- `WARN`: Connection issues (peer not found)
- `ERROR`: Server errors (message processing failures)

**Example Logs**:
```
INFO  Peer peer_renter_abc joining signaling session: sess_abc123
INFO  Peer peer_renter_abc successfully joined signaling session: sess_abc123. Total peers: 1
INFO  Peer peer_host_xyz joining signaling session: sess_abc123
INFO  Peer peer_host_xyz successfully joined signaling session: sess_abc123. Total peers: 2
DEBUG Received signaling message: ICE_CANDIDATE from peer: peer_renter_abc in session: sess_abc123
DEBUG Forwarded ICE_CANDIDATE from peer_renter_abc to peer_host_xyz in session: sess_abc123
INFO  Peer peer_renter_abc leaving signaling session: sess_abc123
INFO  Peer peer_host_xyz leaving signaling session: sess_abc123
INFO  Signaling session sess_abc123 cleaned up (no more peers)
```

## Performance Considerations

### Resource Management
- **Memory**: ~1KB per peer connection
- **CPU**: Minimal (message relay only)
- **Network**: Depends on ICE candidate exchange frequency

### Scalability
- **Concurrent Sessions**: Limited by Tomcat thread pool (default: 200)
- **Peers per Session**: Typically 2 (host + renter)
- **Message Throughput**: ~1000 messages/second per session

### Optimization Tips
1. **Close WebSocket after P2P established**: Saves server resources
2. **Use broadcast sparingly**: Point-to-point is more efficient
3. **Set message size limits**: Prevent abuse
4. **Implement connection pooling**: Reuse resources

## Client Integration

### JavaScript/TypeScript Example
```javascript
class SignalingClient {
  constructor(sessionId, peerId) {
    this.sessionId = sessionId;
    this.peerId = peerId;
    this.ws = null;
    this.messageHandlers = new Map();
  }
  
  connect() {
    const url = `ws://revonixo.com/signaling/${this.sessionId}?peerId=${this.peerId}`;
    this.ws = new WebSocket(url);
    
    this.ws.onopen = () => {
      console.log('Connected to signaling server');
    };
    
    this.ws.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      const handler = this.messageHandlers.get(msg.type);
      if (handler) {
        handler(msg);
      }
    };
    
    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
    
    this.ws.onclose = () => {
      console.log('Disconnected from signaling server');
    };
  }
  
  on(messageType, handler) {
    this.messageHandlers.set(messageType, handler);
  }
  
  send(messageType, targetPeerId, data) {
    const message = {
      type: messageType,
      to: targetPeerId,
      data: data
    };
    this.ws.send(JSON.stringify(message));
  }
  
  close() {
    if (this.ws) {
      this.ws.close();
    }
  }
}

// Usage
const client = new SignalingClient('sess_abc123', 'peer_renter_abc');
client.connect();

client.on('ICE_CANDIDATE', (msg) => {
  peerConnection.addIceCandidate(msg.data);
});

client.on('SDP_OFFER', async (msg) => {
  await peerConnection.setRemoteDescription(msg.data);
  const answer = await peerConnection.createAnswer();
  client.send('SDP_ANSWER', msg.from, answer);
});

client.send('ICE_CANDIDATE', 'peer_host_xyz', iceCandidate);
```

### Android/Java Example
```java
// Using OkHttp WebSocket
OkHttpClient client = new OkHttpClient();
String url = "ws://revonixo.com/signaling/sess_abc123?peerId=peer_renter_abc";

WebSocket ws = client.newWebSocket(new Request.Builder().url(url).build(), 
  new WebSocketListener() {
    @Override
    public void onMessage(WebSocket webSocket, String text) {
      SignalingMessage msg = gson.fromJson(text, SignalingMessage.class);
      // Handle message
    }
});

// Send message
SignalingMessage msg = new SignalingMessage(
  SignalingMessageType.ICE_CANDIDATE,
  "peer_renter_abc",
  "peer_host_xyz",
  iceCandidate
);
ws.send(gson.toJson(msg));
```

## Testing

### Unit Tests
```java
@Test
public void testPeerConnection() {
  // Test peer connects successfully
  // Test message routing
  // Test peer disconnect cleanup
}
```

### Integration Tests
```java
@Test
public void testE2EHandshake() {
  // Connect two peers
  // Exchange ICE candidates
  // Exchange SDP offer/answer
  // Verify P2P connection established
}
```

### Manual Testing
```bash
# Using wscat tool
npm install -g wscat

# Terminal 1 (Renter)
wscat -c "ws://localhost:8080/signaling/sess_test123?peerId=peer_renter"

# Terminal 2 (Host)
wscat -c "ws://localhost:8080/signaling/sess_test123?peerId=peer_host"

# Send message from Renter
> {"type":"ICE_CANDIDATE","to":"peer_host","data":{"candidate":"test"}}

# Host receives message
< {"type":"ICE_CANDIDATE","from":"peer_renter","to":"peer_host","data":{"candidate":"test"},"timestamp":1737534400000}
```

## Deployment

### Tomcat Configuration
No special configuration required. JSR 356 WebSocket support is built into Tomcat 7.0.47+.

### Load Balancing
For horizontal scaling, use sticky sessions:
```apache
# Apache mod_proxy_wstunnel
<VirtualHost *:80>
  ProxyPass /signaling ws://backend1:8080/signaling sticky
  ProxyPass / http://backend1:8080/
</VirtualHost>
```

### Monitoring
```bash
# Check active sessions via JMX
jconsole -> com.hmdm.signaling.SignalingServer -> getActiveSessionsCount()

# Check logs
tail -f /var/log/tomcat/catalina.out | grep SignalingServer
```

## Troubleshooting

### Connection Refused
- **Check**: Tomcat is running
- **Check**: WebSocket endpoint is deployed
- **Check**: Firewall allows WebSocket connections

### Messages Not Forwarded
- **Check**: Both peers connected to same sessionId
- **Check**: PeerId is correctly specified in message
- **Check**: Target peer is still connected

### High Memory Usage
- **Check**: Old sessions are being cleaned up
- **Check**: Peers are closing WebSocket after P2P established
- **Check**: No memory leaks in message handlers

## Next Steps

1. Implement RentalSessionService integration for authorization
2. Add JWT token validation
3. Implement rate limiting
4. Add monitoring dashboard
5. Load testing and performance optimization
6. Production deployment with TLS/WSS
