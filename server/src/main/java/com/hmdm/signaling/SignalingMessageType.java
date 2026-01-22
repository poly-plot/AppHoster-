/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.signaling;

/**
 * Signaling message types for P2P handshake coordination
 */
public enum SignalingMessageType {
    ICE_CANDIDATE,    // NAT traversal ICE candidate
    SDP_OFFER,        // Connection offer from initiator
    SDP_ANSWER,       // Connection answer from responder
    PEER_READY,       // Peer is ready for direct P2P
    PEER_JOINED,      // Peer joined the signaling session
    PEER_LEFT,        // Peer left the signaling session
    ERROR             // Error message
}
