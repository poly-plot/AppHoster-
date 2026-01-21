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

package com.hmdm.p2p.discovery;

import com.hmdm.p2p.model.Peer;

import java.util.function.Consumer;

/**
 * Service for discovering peers on the network
 */
public interface PeerDiscoveryService {
    
    /**
     * Start advertising this node as a peer
     */
    void startAdvertising();
    
    /**
     * Stop advertising this node
     */
    void stopAdvertising();
    
    /**
     * Check if currently advertising
     * @return true if advertising
     */
    boolean isAdvertising();
    
    /**
     * Scan for peers on the network
     * @param callback callback to be invoked for each discovered peer
     */
    void scanForPeers(Consumer<Peer> callback);
    
    /**
     * Stop the current peer scan
     */
    void stopScanning();
}
