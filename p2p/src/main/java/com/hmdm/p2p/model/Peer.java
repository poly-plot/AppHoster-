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

package com.hmdm.p2p.model;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a peer node in the P2P network
 */
public class Peer {
    private String peerId;
    private String deviceId;
    private String ipAddress;
    private int port;
    private String certificate;
    private Set<String> capabilities;
    private long lastSeen;
    private boolean isSuperNode;
    private PeerStatus status;

    public enum PeerStatus {
        DISCOVERED,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        FAILED
    }

    public Peer() {
    }

    public Peer(String peerId, String deviceId) {
        this.peerId = peerId;
        this.deviceId = deviceId;
        this.status = PeerStatus.DISCOVERED;
        this.lastSeen = System.currentTimeMillis();
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isSuperNode() {
        return isSuperNode;
    }

    public void setSuperNode(boolean superNode) {
        isSuperNode = superNode;
    }

    public PeerStatus getStatus() {
        return status;
    }

    public void setStatus(PeerStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(peerId, peer.peerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerId);
    }

    @Override
    public String toString() {
        return "Peer{" +
                "peerId='" + peerId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", status=" + status +
                ", isSuperNode=" + isSuperNode +
                '}';
    }
}
