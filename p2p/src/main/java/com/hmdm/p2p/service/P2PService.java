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

package com.hmdm.p2p.service;

import com.hmdm.p2p.model.Peer;

import java.util.List;

/**
 * Core P2P service for managing peer-to-peer network connections
 */
public interface P2PService {
    
    /**
     * Start the P2P service and begin peer discovery
     */
    void start();
    
    /**
     * Stop the P2P service and disconnect all peers
     */
    void stop();
    
    /**
     * Check if P2P service is running
     * @return true if service is running
     */
    boolean isRunning();
    
    /**
     * Discover peers on the network
     * @return list of discovered peers
     */
    List<Peer> discoverPeers();
    
    /**
     * Connect to a specific peer
     * @param peerId the peer ID to connect to
     * @return true if connection successful
     */
    boolean connectToPeer(String peerId);
    
    /**
     * Disconnect from a specific peer
     * @param peerId the peer ID to disconnect from
     */
    void disconnectPeer(String peerId);
    
    /**
     * Get list of currently connected peers
     * @return list of connected peers
     */
    List<Peer> getConnectedPeers();
    
    /**
     * Get a specific peer by ID
     * @param peerId the peer ID
     * @return the peer or null if not found
     */
    Peer getPeer(String peerId);
    
    /**
     * Check if connected to a specific peer
     * @param peerId the peer ID to check
     * @return true if connected
     */
    boolean isConnectedTo(String peerId);
}
