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

package com.hmdm.p2p.signaling;

import com.hmdm.p2p.model.Peer;

/**
 * Signaling service for coordinating P2P handshakes between global peers
 * The server acts as a signaling relay for peers behind NAT
 */
public interface SignalingService {
    
    /**
     * Initiate handshake between two peers
     * @param initiatorPeer the peer initiating the connection
     * @param targetPeer the peer to connect to
     * @return handshake token for establishing P2P connection
     */
    String initiateHandshake(Peer initiatorPeer, Peer targetPeer);
    
    /**
     * Exchange connection offer between peers
     * @param handshakeToken the handshake token
     * @param offerData connection offer data (SDP-like format)
     * @return true if offer accepted
     */
    boolean exchangeOffer(String handshakeToken, String offerData);
    
    /**
     * Exchange connection answer between peers
     * @param handshakeToken the handshake token
     * @param answerData connection answer data
     * @return true if answer accepted
     */
    boolean exchangeAnswer(String handshakeToken, String answerData);
    
    /**
     * Exchange ICE candidates for NAT traversal
     * @param handshakeToken the handshake token
     * @param candidateData ICE candidate information
     * @return true if candidate accepted
     */
    boolean exchangeIceCandidate(String handshakeToken, String candidateData);
    
    /**
     * Complete handshake and transition to direct P2P
     * @param handshakeToken the handshake token
     * @return connection information for direct P2P
     */
    ConnectionInfo completeHandshake(String handshakeToken);
    
    /**
     * Cancel an ongoing handshake
     * @param handshakeToken the handshake token
     */
    void cancelHandshake(String handshakeToken);
    
    /**
     * Connection information for establishing direct P2P connection
     */
    class ConnectionInfo {
        private String publicIp;
        private Integer publicPort;
        private String encryptionKey;
        private String protocol;
        
        public ConnectionInfo() {
        }
        
        public String getPublicIp() {
            return publicIp;
        }
        
        public void setPublicIp(String publicIp) {
            this.publicIp = publicIp;
        }
        
        public Integer getPublicPort() {
            return publicPort;
        }
        
        public void setPublicPort(Integer publicPort) {
            this.publicPort = publicPort;
        }
        
        public String getEncryptionKey() {
            return encryptionKey;
        }
        
        public void setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }
        
        public String getProtocol() {
            return protocol;
        }
        
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }
}
