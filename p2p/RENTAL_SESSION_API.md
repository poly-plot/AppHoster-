# Revonixo Rental Session API Documentation

## Overview

The Rental Session Management API provides server-side coordination for the Revonixo decentralized cloud computing marketplace. This API manages rental sessions between host devices (providers) and renter devices (consumers), handling payment verification, P2P connection coordination, and session lifecycle management.

## Architecture

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Renter    │◄───────►│   Server     │◄───────►│    Host     │
│   Device    │  REST   │   (This      │  REST   │   Device    │
│ (Consumer)  │   API   │   Module)    │   API   │ (Provider)  │
└─────────────┘         └──────────────┘         └─────────────┘
       │                       │                         │
       │                       │                         │
       └───────────────────────┴─────────────────────────┘
                      P2P Encrypted Video Stream
                      (Netty + Bouncycastle)
```

## Database Schema

### rental_sessions
Tracks active and historical rental sessions

| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| session_id | VARCHAR(64) | Unique session identifier |
| host_device_id | INTEGER | Host device reference |
| host_peer_id | VARCHAR(64) | P2P peer ID of host |
| renter_device_id | INTEGER | Renter device reference |
| renter_peer_id | VARCHAR(64) | P2P peer ID of renter |
| rental_duration_hours | INTEGER | Rental period (default: 24) |
| start_time | TIMESTAMP | Session start timestamp |
| end_time | TIMESTAMP | Session expiration timestamp |
| payment_status | VARCHAR(32) | Payment state (pending/confirmed/failed/refunded) |
| payment_transaction_id | VARCHAR(128) | Web3/RVX transaction ID |
| payment_amount | DECIMAL(10,2) | Payment amount |
| payment_currency | VARCHAR(10) | Currency code (USD/RVX/ETH) |
| session_status | VARCHAR(32) | Session state (pending/active/expired/terminated/cancelled) |
| p2p_encryption_key | TEXT | Encrypted P2P session key |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

### rental_session_events
Audit trail for session lifecycle events

| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| session_id | VARCHAR(64) | Reference to rental_sessions |
| event_type | VARCHAR(64) | Event type (session_created, payment_confirmed, session_started, session_terminated, etc.) |
| event_data | JSONB | Additional event metadata |
| created_at | TIMESTAMP | Event timestamp |

### host_availability
Marketplace listing for available host devices

| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| device_id | INTEGER | Device reference |
| peer_id | VARCHAR(64) | P2P peer ID |
| is_available | BOOLEAN | Availability status |
| hourly_rate | DECIMAL(10,2) | Rental rate per hour |
| currency | VARCHAR(10) | Currency code |
| max_rental_hours | INTEGER | Maximum rental duration |
| device_specs | JSONB | Device specifications (CPU, RAM, GPU, etc.) |
| availability_schedule | JSONB | Availability time windows |
| total_rentals | INTEGER | Total completed rentals |
| average_rating | DECIMAL(3,2) | Average user rating |
| last_updated | TIMESTAMP | Last status update |
| created_at | TIMESTAMP | Registration time |

## REST API Endpoints

### 1. Create Rental Session
**POST** `/api/v1/rental/sessions`

Creates a new rental session between a renter and host device.

**Request:**
```json
{
  "hostDeviceId": 123,
  "renterDeviceId": 456,
  "durationHours": 24,
  "paymentAmount": 10.00,
  "paymentCurrency": "USD"
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123xyz",
  "status": "pending",
  "paymentStatus": "pending",
  "createdAt": 1737534000000
}
```

### 2. Confirm Payment
**POST** `/api/v1/rental/sessions/{sessionId}/confirm-payment`

Confirms payment for a rental session (called by Web3/RVX module).

**Request:**
```json
{
  "transactionId": "0x123abc...",
  "blockchainNetwork": "ethereum",
  "confirmationCount": 12
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123xyz",
  "paymentStatus": "confirmed",
  "sessionStatus": "active",
  "startTime": 1737534100000,
  "endTime": 1737620500000,
  "p2pHandshakeToken": "encrypted_credentials_here"
}
```

### 3. Start Session (Activate P2P)
**POST** `/api/v1/rental/sessions/{sessionId}/start`

Activates the P2P connection between renter and host.

**Response:**
```json
{
  "sessionId": "sess_abc123xyz",
  "sessionStatus": "active",
  "hostPeerId": "peer_host_abc",
  "renterPeerId": "peer_renter_xyz",
  "p2pEncryptionKey": "base64_encrypted_key",
  "connectionInfo": {
    "host": "192.168.1.100",
    "port": 9090,
    "protocol": "netty-tls"
  }
}
```

### 4. Get Session Status
**GET** `/api/v1/rental/sessions/{sessionId}`

Retrieves current session status and details.

**Response:**
```json
{
  "sessionId": "sess_abc123xyz",
  "hostDeviceId": 123,
  "renterDeviceId": 456,
  "sessionStatus": "active",
  "paymentStatus": "confirmed",
  "startTime": 1737534100000,
  "endTime": 1737620500000,
  "remainingMinutes": 1380,
  "isExpired": false
}
```

### 5. Terminate Session
**POST** `/api/v1/rental/sessions/{sessionId}/terminate`

Terminates an active rental session (manual or auto-expiration).

**Request:**
```json
{
  "reason": "rental_expired",
  "performCleanup": true
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123xyz",
  "sessionStatus": "terminated",
  "terminatedAt": 1737620500000,
  "cleanupCompleted": true
}
```

### 6. Register Host Device
**POST** `/api/v1/rental/hosts`

Registers a device as available for rental in the marketplace.

**Request:**
```json
{
  "deviceId": 123,
  "peerId": "peer_host_abc",
  "hourlyRate": 5.00,
  "currency": "USD",
  "maxRentalHours": 48,
  "deviceSpecs": {
    "cpu": "Snapdragon 888",
    "ram": 8,
    "gpu": "Adreno 660",
    "storage": 256
  }
}
```

**Response:**
```json
{
  "id": 1,
  "deviceId": 123,
  "isAvailable": true,
  "hourlyRate": 5.00,
  "registeredAt": 1737534000000
}
```

### 7. Search Available Hosts
**GET** `/api/v1/rental/hosts/search?maxRate=10&minRating=4.0`

Searches for available host devices in the marketplace.

**Response:**
```json
{
  "hosts": [
    {
      "deviceId": 123,
      "peerId": "peer_host_abc",
      "hourlyRate": 5.00,
      "currency": "USD",
      "deviceSpecs": {
        "cpu": "Snapdragon 888",
        "ram": 8
      },
      "averageRating": 4.5,
      "totalRentals": 42
    }
  ],
  "totalCount": 1
}
```

### 8. Update Host Availability
**PATCH** `/api/v1/rental/hosts/{deviceId}/availability`

Updates host availability status.

**Request:**
```json
{
  "isAvailable": false
}
```

**Response:**
```json
{
  "deviceId": 123,
  "isAvailable": false,
  "updatedAt": 1737534000000
}
```

## Session Lifecycle

```
1. PENDING    → Session created, awaiting payment
              ↓
2. CONFIRMED  → Payment verified by Web3/RVX module
              ↓
3. ACTIVE     → P2P connection established, streaming active
              ↓
4. EXPIRED    → Rental period ended (auto-triggered)
              ↓
5. TERMINATED → Session closed, cleanup performed
```

## P2P Handshake Flow

1. **Renter creates session** → Server generates `sessionId`
2. **Payment confirmed** → Server generates `p2pEncryptionKey` using Bouncycastle
3. **Renter calls start** → Server coordinates P2P connection:
   - Returns host peer info (IP, port, peerId)
   - Returns encrypted session key
4. **Devices connect** → Direct P2P connection via Netty:
   - TLS handshake using session key
   - Encrypted video stream starts
5. **Session expires** → Server notifies both devices:
   - Host triggers cleanup (purge sandbox)
   - P2P connection terminated

## Security

- **Payment Verification**: Server validates Web3/RVX transactions before activating sessions
- **P2P Encryption**: Bouncycastle-generated AES-256 keys for video stream encryption
- **Session Isolation**: Each session has unique encryption keys
- **Auto-Expiration**: Server-side scheduler terminates expired sessions
- **Audit Trail**: All events logged in `rental_session_events` table

## Integration Points

### Web3/RVX Payment Module
- Calls `POST /api/v1/rental/sessions/{sessionId}/confirm-payment` when payment confirmed
- Provides transaction ID for verification

### Android Client App (Future Phase 2)
- Calls session APIs to coordinate rental lifecycle
- Receives P2P connection credentials
- Implements Virtual Display and frame capture (client-side)

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Invalid request parameters |
| 401 | Unauthorized (authentication required) |
| 403 | Forbidden (host not available, renter blocked, etc.) |
| 404 | Session not found |
| 409 | Conflict (session already active, payment already confirmed, etc.) |
| 500 | Internal server error |

## Implementation Status

- [x] Database schema designed
- [x] Service interfaces defined
- [x] Model classes created
- [ ] Service implementation (stub)
- [ ] REST API endpoints (to be implemented in Phase 2)
- [ ] Payment verification integration (Web3/RVX)
- [ ] Auto-expiration scheduler
- [ ] P2P coordination logic
- [ ] Android client app (separate repository)

## Next Steps

1. Implement service layer with database operations
2. Create REST API endpoints in server module
3. Add payment verification integration
4. Implement session expiration scheduler
5. Test P2P coordination flow
6. Create Android client app for Phase 2
