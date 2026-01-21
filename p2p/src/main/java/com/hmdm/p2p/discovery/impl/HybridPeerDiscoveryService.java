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

package com.hmdm.p2p.discovery.impl;

import com.hmdm.p2p.discovery.PeerDiscoveryService;
import com.hmdm.p2p.model.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Hybrid peer discovery service supporting both local (mDNS) and global (DHT) discovery
 * Acts as bootstrap node for DHT network when configured
 */
public class HybridPeerDiscoveryService implements PeerDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(HybridPeerDiscoveryService.class);
    
    private final PeerDiscoveryService localDiscovery;
    private final DiscoveryMethod discoveryMethod;
    private final String bootstrapServerUrl;
    private final Map<String, Peer> globalDhtRegistry;
    
    private boolean advertising = false;
    private boolean scanning = false;
    
    public HybridPeerDiscoveryService(PeerDiscoveryService localDiscovery, 
                                     DiscoveryMethod discoveryMethod,
                                     String bootstrapServerUrl) {
        this.localDiscovery = localDiscovery;
        this.discoveryMethod = discoveryMethod;
        this.bootstrapServerUrl = bootstrapServerUrl;
        this.globalDhtRegistry = new ConcurrentHashMap<>();
    }
    
    @Override
    public void startAdvertising() {
        if (advertising) {
            logger.warn("Already advertising");
            return;
        }
        
        // Start local advertising if enabled
        if (discoveryMethod == DiscoveryMethod.LOCAL_ONLY || 
            discoveryMethod == DiscoveryMethod.HYBRID) {
            localDiscovery.startAdvertising();
        }
        
        advertising = true;
        logger.info("Started hybrid advertising (method: {})", discoveryMethod);
    }
    
    @Override
    public void stopAdvertising() {
        if (!advertising) {
            return;
        }
        
        if (discoveryMethod == DiscoveryMethod.LOCAL_ONLY || 
            discoveryMethod == DiscoveryMethod.HYBRID) {
            localDiscovery.stopAdvertising();
        }
        
        advertising = false;
        logger.info("Stopped hybrid advertising");
    }
    
    @Override
    public boolean isAdvertising() {
        return advertising;
    }
    
    @Override
    public void scanForPeers(Consumer<Peer> callback) {
        if (scanning) {
            logger.warn("Already scanning for peers");
            return;
        }
        
        scanning = true;
        
        // Scan local network if enabled
        if (discoveryMethod == DiscoveryMethod.LOCAL_ONLY || 
            discoveryMethod == DiscoveryMethod.HYBRID) {
            localDiscovery.scanForPeers(callback);
        }
        
        // Scan global DHT if enabled
        if (discoveryMethod == DiscoveryMethod.GLOBAL_ONLY || 
            discoveryMethod == DiscoveryMethod.HYBRID) {
            scanForGlobalPeers(callback);
        }
        
        logger.info("Started hybrid peer scan (method: {})", discoveryMethod);
    }
    
    @Override
    public void stopScanning() {
        if (!scanning) {
            return;
        }
        
        if (discoveryMethod == DiscoveryMethod.LOCAL_ONLY || 
            discoveryMethod == DiscoveryMethod.HYBRID) {
            localDiscovery.stopScanning();
        }
        
        scanning = false;
        logger.info("Stopped hybrid peer scan");
    }
    
    @Override
    public DiscoveryMethod getDiscoveryMethod() {
        return discoveryMethod;
    }
    
    @Override
    public void scanForGlobalPeers(Consumer<Peer> callback) {
        logger.info("Scanning global DHT registry...");
        
        // Return all peers from the global registry
        globalDhtRegistry.values().forEach(peer -> {
            logger.debug("Found global peer: {} at {}:{}", 
                peer.getPeerId(), 
                peer.getPublicIpAddress(), 
                peer.getPublicPort());
            callback.accept(peer);
        });
        
        // In production, this would query the DHT network
        // For now, we use the server as a central registry
        logger.info("Global peer scan completed. Found {} peers", globalDhtRegistry.size());
    }
    
    @Override
    public boolean registerInGlobalDHT(Peer peer) {
        if (peer == null || peer.getDhtId() == null) {
            logger.error("Cannot register peer without DHT ID");
            return false;
        }
        
        // Register peer in global DHT
        globalDhtRegistry.put(peer.getDhtId(), peer);
        logger.info("Registered peer {} in global DHT with ID: {}", 
            peer.getPeerId(), peer.getDhtId());
        
        // In production, this would announce to the DHT network
        // and connect to bootstrap nodes
        return true;
    }
    
    @Override
    public Peer lookupPeerByDhtId(String dhtId) {
        if (dhtId == null) {
            return null;
        }
        
        Peer peer = globalDhtRegistry.get(dhtId);
        if (peer != null) {
            logger.debug("Found peer in local DHT cache: {}", dhtId);
            return peer;
        }
        
        // In production, this would query the DHT network
        logger.debug("Peer not found in DHT: {}", dhtId);
        return null;
    }
    
    /**
     * Get bootstrap server URL for DHT network
     * @return bootstrap server URL
     */
    public String getBootstrapServerUrl() {
        return bootstrapServerUrl;
    }
    
    /**
     * Get number of peers in global registry
     * @return peer count
     */
    public int getGlobalPeerCount() {
        return globalDhtRegistry.size();
    }
}
