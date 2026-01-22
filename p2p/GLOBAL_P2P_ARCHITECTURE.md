# Global P2P Architecture for Revonixo

## Overview

This document describes the global-scale peer-to-peer networking architecture for Revonixo, enabling worldwide device-to-device connections with NAT traversal and DHT-based discovery.

## Architecture Comparison

### Local P2P (Phase 1)
- **Scope**: Same local network (LAN/subnet)
- **Discovery**: mDNS (multicast DNS)
- **Connection**: Direct TCP/UDP
- **Use Case**: Office/campus deployments

### Global P2P (Phase 2)
- **Scope**: Internet-wide (cross-country, cross-continent)
- **Discovery**: DHT (Distributed Hash Table) + mDNS hybrid
- **Connection**: NAT traversal (STUN/TURN) + direct
- **Use Case**: Global marketplace

## Key Components

### 1. Hybrid Discovery Service

**Local Discovery (mDNS)**:
- Fast peer discovery on LAN (< 2 seconds)
- Zero-configuration
- Limited to same subnet

**Global Discovery (DHT)**:
- Distributed peer registry across Internet
- Server acts as bootstrap node
- Peers can find each other globally

**Hybrid Mode** (Default):
- Tries local first (fast)
- Falls back to global (reliable)
- Best performance for all scenarios

```java
// Hybrid discovery example
HybridPeerDiscoveryService discovery = new HybridPeerDiscoveryService(
    localDiscovery,
    DiscoveryMethod.HYBRID,
    "https://bootstrap.revonixo.com"
);

// Discover local peers first
discovery.scanForPeers(peer -> {
    if (peer.getIpAddress().startsWith("192.168")) {
        // Local peer - direct connection
    }
});

// Then search globally
discovery.scanForGlobalPeers(globalPeer -> {
    if (globalPeer.isBehindNat()) {
        // Use NAT traversal
    }
});
```

### 2. DHT Network Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Server     │◄────────┤   Server     │────────►│   Server     │
│ (Bootstrap)  │   DHT   │  (Region 1)  │   DHT   │  (Region 2)  │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │ Register               │ Query                  │ Lookup
       │                        │                        │
┌──────▼───────┐         ┌──────▼───────┐         ┌──────▼───────┐
│  Peer (USA)  │         │ Peer (Europe)│         │ Peer (Asia)  │
│  DHT ID: A1  │◄────────┤  DHT ID: B2  │────────►│  DHT ID: C3  │
└──────────────┘  P2P    └──────────────┘  P2P    └──────────────┘
```

**DHT ID Generation**:
```java
String dhtId = SHA256(deviceId + publicKey + timestamp);
// Example: "a1b2c3d4e5f6..." (128 characters)
```

**Registration Flow**:
1. Peer generates DHT ID
2. Connects to bootstrap server
3. Announces: `{dhtId, publicIp, publicPort, publicKey}`
4. Bootstrap server stores in DHT
5. Peer periodically refreshes registration (every 15 minutes)

### 3. Peer Model Extensions

**New Fields**:
```java
public class Peer {
    // Existing fields
    private String peerId;          // Local identifier
    private String ipAddress;       // Private IP (LAN)
    private int port;              // Private port
    
    // Global networking fields
    private String publicIpAddress; // Public IP (WAN)
    private Integer publicPort;     // Public port (after NAT)
    private String dhtId;          // DHT network identifier
    private String publicKey;       // For encryption/authentication
    private String stunServer;      // STUN server for NAT detection
    private String turnServer;      // TURN relay server
    private boolean behindNat;      // NAT detection flag
}
```

**Database Schema**:
```sql
ALTER TABLE peers ADD COLUMN public_ip_address VARCHAR(45);
ALTER TABLE peers ADD COLUMN public_port INTEGER;
ALTER TABLE peers ADD COLUMN dht_id VARCHAR(128);
ALTER TABLE peers ADD COLUMN public_key TEXT;
ALTER TABLE peers ADD COLUMN stun_server VARCHAR(255);
ALTER TABLE peers ADD COLUMN turn_server VARCHAR(255);
ALTER TABLE peers ADD COLUMN behind_nat BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_peers_dht_id ON peers(dht_id);
CREATE INDEX idx_peers_public_ip ON peers(public_ip_address);
```

### 4. NAT Traversal

**NAT Types & Solutions**:

| NAT Type | Solution | Success Rate |
|----------|----------|--------------|
| Full Cone | Direct connection | 100% |
| Restricted Cone | STUN | 90% |
| Port Restricted | STUN + Hole Punching | 80% |
| Symmetric | TURN relay | 100% |

**STUN (Session Traversal Utilities for NAT)**:
- Discovers public IP and port
- Free/lightweight
- Works for ~80% of NAT scenarios

**TURN (Traversal Using Relays around NAT)**:
- Relay server for difficult NATs
- Guaranteed connectivity
- Higher server costs

**Connection Flow**:
```
1. Peer A discovers public IP via STUN
   → STUN server: "Your public IP is 203.0.113.5:12345"

2. Peer A registers in DHT
   → {dhtId: "a1b2...", publicIp: "203.0.113.5", publicPort: 12345}

3. Peer B queries DHT for Peer A
   → Receives: {publicIp, publicPort, publicKey}

4. Peer B attempts direct connection
   → If fails: Use TURN relay
   → Server relays traffic between A ↔ B

5. Once connected: Switch to direct P2P
   → Close relay tunnel
   → End-to-end encryption (Bouncycastle)
```

### 5. Signaling Flow

**Problem**: How do two peers behind different NATs find each other?

**Solution**: Server-assisted signaling (like WebRTC)

**Signaling Protocol**:

```
┌─────────┐         ┌──────────┐         ┌─────────┐
│ Peer A  │         │  Server  │         │ Peer B  │
│ (USA)   │         │(Signaling)│         │ (Europe)│
└────┬────┘         └────┬─────┘         └────┬────┘
     │                   │                    │
     │ 1. Initiate       │                    │
     ├──────────────────►│                    │
     │                   │                    │
     │                   │ 2. Notify          │
     │                   ├───────────────────►│
     │                   │                    │
     │                   │ 3. Offer           │
     │                   │◄───────────────────┤
     │ 4. Offer          │                    │
     │◄──────────────────┤                    │
     │                   │                    │
     │ 5. Answer         │                    │
     ├──────────────────►│                    │
     │                   │ 6. Answer          │
     │                   ├───────────────────►│
     │                   │                    │
     │ 7. ICE Candidates │                    │
     ├──────────────────►│────────────────────┤
     │◄──────────────────┤◄───────────────────┤
     │                   │                    │
     │ 8. Direct P2P Connection Established   │
     ├────────────────────────────────────────┤
     │      Encrypted Video Stream            │
     │◄──────────────────────────────────────►│
     │                   │                    │
```

**Implementation**:

```java
// 1. Renter initiates connection to Host
String handshakeToken = signalingService.initiateHandshake(renterPeer, hostPeer);

// 2. Server notifies host via push notification

// 3. Host generates connection offer
String offer = generateConnectionOffer(); // SDP-like format
signalingService.exchangeOffer(handshakeToken, offer);

// 4. Renter receives offer

// 5. Renter generates answer
String answer = generateConnectionAnswer();
signalingService.exchangeAnswer(handshakeToken, answer);

// 6. Both peers exchange ICE candidates
signalingService.exchangeIceCandidate(handshakeToken, iceCandidate);

// 7. Connection established
ConnectionInfo connInfo = signalingService.completeHandshake(handshakeToken);

// 8. Switch to direct P2P
nettyChannel.connect(connInfo.getPublicIp(), connInfo.getPublicPort());
```

### 6. REST API Endpoints

**Global Peer Registry**:

```http
POST /api/v1/p2p/register-global
{
  "deviceId": 123,
  "peerId": "peer_abc",
  "publicIpAddress": "203.0.113.5",
  "publicPort": 12345,
  "dhtId": "a1b2c3d4...",
  "publicKey": "-----BEGIN PUBLIC KEY-----\n...",
  "behindNat": true,
  "stunServer": "stun:stun.revonixo.com:3478",
  "turnServer": "turn:turn.revonixo.com:3478"
}
```

**DHT Lookup**:

```http
GET /api/v1/p2p/lookup-dht/{dhtId}

Response:
{
  "peerId": "peer_abc",
  "deviceId": 123,
  "publicIpAddress": "203.0.113.5",
  "publicPort": 12345,
  "publicKey": "...",
  "lastSeen": 1737534000000
}
```

**Signaling Handshake**:

```http
POST /api/v1/p2p/signaling/initiate
{
  "initiatorPeerId": "peer_renter",
  "targetPeerId": "peer_host"
}

Response:
{
  "handshakeToken": "hs_xyz123",
  "expiresIn": 300
}
```

```http
POST /api/v1/p2p/signaling/offer
{
  "handshakeToken": "hs_xyz123",
  "offerData": "v=0\r\no=- ... (SDP format)"
}
```

```http
POST /api/v1/p2p/signaling/answer
{
  "handshakeToken": "hs_xyz123",
  "answerData": "v=0\r\na=..."
}
```

```http
POST /api/v1/p2p/signaling/ice-candidate
{
  "handshakeToken": "hs_xyz123",
  "candidateData": {
    "candidate": "...",
    "sdpMid": "0",
    "sdpMLineIndex": 0
  }
}
```

```http
POST /api/v1/p2p/signaling/complete
{
  "handshakeToken": "hs_xyz123"
}

Response:
{
  "publicIp": "203.0.113.5",
  "publicPort": 12345,
  "encryptionKey": "base64_encrypted_key",
  "protocol": "netty-tls"
}
```

### 7. Configuration

**Enable Global P2P**:

```properties
# Hybrid discovery (local + global)
p2p.discovery.method=hybrid

# DHT network
p2p.dht.enabled=true
p2p.dht.bootstrap.servers=https://bootstrap.revonixo.com:8443

# NAT traversal
p2p.nat.traversal.enabled=true
p2p.stun.servers=stun:stun.revonixo.com:3478,stun:stun.l.google.com:19302
p2p.turn.servers=turn:turn.revonixo.com:3478
p2p.turn.username=revonixo
p2p.turn.credential=secret

# Public IP detection
p2p.public.ip.detection.enabled=true
p2p.public.ip.detection.service=https://api.revonixo.com/v1/ip

# Signaling server
p2p.signaling.enabled=true
p2p.signaling.server=wss://signal.revonixo.com/ws
```

### 8. Security Considerations

**DHT Security**:
- Peers must have valid certificates from server
- DHT ID tied to device identity
- Public key used for authentication

**NAT Traversal Security**:
- STUN: No security concerns (read-only)
- TURN: Requires authentication
- All relayed traffic encrypted

**Signaling Security**:
- Handshake tokens expire after 5 minutes
- Server validates both peers before relaying
- Signaling data encrypted (WSS)

**P2P Connection Security**:
- End-to-end encryption (Bouncycastle AES-256)
- Perfect forward secrecy (ephemeral keys)
- Mutual authentication via certificates

### 9. Performance Optimization

**Connection Strategy** (Priority Order):
1. **Local Direct**: Same LAN (fastest, free)
2. **Global Direct**: Public IPs, no NAT (fast, free)
3. **STUN**: NAT hole punching (fast, free)
4. **TURN**: Relay server (reliable, costs money)

**Bandwidth Optimization**:
- Use TURN only when necessary (last resort)
- Monitor connection quality, switch if better path available
- Peer preference: Local > Direct > STUN > TURN

**Latency Targets**:
- Local: < 10ms
- Global Direct: 50-200ms (depends on distance)
- STUN: +10-20ms overhead
- TURN: +30-50ms overhead (relay hop)

### 10. Scalability

**DHT Network**:
- Distributed across multiple servers
- Each server handles ~10,000 peers
- Peer routing via consistent hashing

**Bootstrap Servers**:
- Multiple servers per region
- Load balanced
- Failover support

**TURN Infrastructure**:
- Deploy in each major region
- Auto-scale based on demand
- ~10% of connections need TURN

## Implementation Roadmap

### Phase 2.1: Global Discovery
- [x] Extend Peer model with global fields
- [x] Update database schema
- [x] Hybrid discovery service interface
- [x] DHT registration (server-side)
- [ ] REST API for global registry

### Phase 2.2: NAT Traversal
- [x] Configuration for STUN/TURN
- [ ] Public IP detection service
- [ ] STUN client implementation
- [ ] TURN client integration
- [ ] Netty NAT traversal handlers

### Phase 2.3: Signaling
- [x] Signaling service interface
- [ ] Signaling REST endpoints
- [ ] WebSocket signaling server
- [ ] Handshake state machine
- [ ] ICE candidate exchange

### Phase 2.4: Integration
- [ ] Rental session integration
- [ ] Payment-triggered P2P activation
- [ ] Global peer search in marketplace
- [ ] End-to-end testing

## Next Steps

1. Implement REST API endpoints for global registry
2. Deploy STUN/TURN servers
3. Implement signaling WebSocket server
4. Test cross-country connections
5. Performance benchmarking
6. Android client integration

## References

- RFC 5389: STUN (Session Traversal Utilities for NAT)
- RFC 5766: TURN (Traversal Using Relays around NAT)
- RFC 5245: ICE (Interactive Connectivity Establishment)
- Kademlia DHT Protocol
- WebRTC Signaling Architecture
