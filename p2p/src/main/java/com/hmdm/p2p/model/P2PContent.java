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

/**
 * Represents content that can be shared via P2P network
 */
public class P2PContent {
    private String contentHash;
    private String contentType;
    private long sizeBytes;
    private String fileName;
    private String metadata;
    private long addedAt;
    private long lastAccessed;
    private int accessCount;

    public enum ContentType {
        APPLICATION("app"),
        CONFIGURATION("config"),
        FILE("file"),
        UPDATE("update");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public P2PContent() {
    }

    public P2PContent(String contentHash, String contentType, long sizeBytes) {
        this.contentHash = contentHash;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.addedAt = System.currentTimeMillis();
        this.lastAccessed = this.addedAt;
        this.accessCount = 0;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessed = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        P2PContent that = (P2PContent) o;
        return Objects.equals(contentHash, that.contentHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentHash);
    }

    @Override
    public String toString() {
        return "P2PContent{" +
                "contentHash='" + contentHash + '\'' +
                ", contentType='" + contentType + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", fileName='" + fileName + '\'' +
                ", accessCount=" + accessCount +
                '}';
    }
}
