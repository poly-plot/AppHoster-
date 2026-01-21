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

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * JmDNS-based peer discovery implementation for local network discovery
 */
public class JmDNSPeerDiscoveryService implements PeerDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(JmDNSPeerDiscoveryService.class);
    
    private static final String SERVICE_TYPE = "_hmdm-p2p._tcp.local.";
    private static final int DEFAULT_PORT = 9090;
    
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;
    private boolean advertising = false;
    private boolean scanning = false;
    private String deviceId;
    private Set<String> capabilities;
    
    public JmDNSPeerDiscoveryService(String deviceId, Set<String> capabilities) {
        this.deviceId = deviceId;
        this.capabilities = capabilities != null ? capabilities : new HashSet<>();
    }
    
    @Override
    public void startAdvertising() {
        if (advertising) {
            logger.warn("Already advertising");
            return;
        }
        
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
            
            String serviceName = "HMDM-P2P-" + deviceId;
            serviceInfo = ServiceInfo.create(
                SERVICE_TYPE,
                serviceName,
                DEFAULT_PORT,
                "Headwind MDM P2P Node"
            );
            
            jmdns.registerService(serviceInfo);
            advertising = true;
            logger.info("Started advertising P2P service: {}", serviceName);
            
        } catch (IOException e) {
            logger.error("Failed to start advertising", e);
        }
    }
    
    @Override
    public void stopAdvertising() {
        if (!advertising) {
            return;
        }
        
        try {
            if (jmdns != null && serviceInfo != null) {
                jmdns.unregisterService(serviceInfo);
                jmdns.close();
            }
            advertising = false;
            logger.info("Stopped advertising P2P service");
            
        } catch (Exception e) {
            logger.error("Error stopping advertising", e);
        }
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
        
        try {
            if (jmdns == null) {
                jmdns = JmDNS.create(InetAddress.getLocalHost());
            }
            
            scanning = true;
            
            jmdns.addServiceListener(SERVICE_TYPE, new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                    logger.debug("Service added: {}", event.getName());
                    jmdns.requestServiceInfo(event.getType(), event.getName());
                }
                
                @Override
                public void serviceRemoved(ServiceEvent event) {
                    logger.debug("Service removed: {}", event.getName());
                }
                
                @Override
                public void serviceResolved(ServiceEvent event) {
                    ServiceInfo info = event.getInfo();
                    logger.info("Peer discovered: {} at {}:{}", 
                        info.getName(), 
                        info.getHostAddresses()[0], 
                        info.getPort());
                    
                    // Parse device ID from service name
                    String peerDeviceId = extractDeviceId(info.getName());
                    if (peerDeviceId != null && !peerDeviceId.equals(deviceId)) {
                        Peer peer = new Peer(info.getName(), peerDeviceId);
                        peer.setIpAddress(info.getHostAddresses()[0]);
                        peer.setPort(info.getPort());
                        peer.setStatus(Peer.PeerStatus.DISCOVERED);
                        
                        callback.accept(peer);
                    }
                }
            });
            
            logger.info("Started scanning for peers");
            
        } catch (IOException e) {
            logger.error("Failed to start peer scanning", e);
            scanning = false;
        }
    }
    
    @Override
    public void stopScanning() {
        if (!scanning) {
            return;
        }
        
        try {
            if (jmdns != null) {
                jmdns.removeServiceListener(SERVICE_TYPE, null);
            }
            scanning = false;
            logger.info("Stopped scanning for peers");
            
        } catch (Exception e) {
            logger.error("Error stopping peer scanning", e);
        }
    }
    
    private String extractDeviceId(String serviceName) {
        // Extract device ID from service name format: HMDM-P2P-{deviceId}
        if (serviceName != null && serviceName.startsWith("HMDM-P2P-")) {
            return serviceName.substring("HMDM-P2P-".length());
        }
        return null;
    }
    
    @Override
    public DiscoveryMethod getDiscoveryMethod() {
        return DiscoveryMethod.LOCAL_ONLY;
    }
    
    @Override
    public void scanForGlobalPeers(Consumer<Peer> callback) {
        // JmDNS only supports local network discovery
        logger.warn("Global peer scanning not supported by JmDNS - use HybridPeerDiscoveryService");
    }
    
    @Override
    public boolean registerInGlobalDHT(Peer peer) {
        // JmDNS does not support global DHT registration
        logger.warn("Global DHT registration not supported by JmDNS - use HybridPeerDiscoveryService");
        return false;
    }
    
    @Override
    public Peer lookupPeerByDhtId(String dhtId) {
        // JmDNS does not support DHT lookup
        logger.warn("DHT lookup not supported by JmDNS - use HybridPeerDiscoveryService");
        return null;
    }
}
