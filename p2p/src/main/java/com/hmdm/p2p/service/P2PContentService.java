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

import com.hmdm.p2p.model.P2PContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service for managing P2P content distribution
 */
public interface P2PContentService {
    
    /**
     * Check if content is available in P2P network
     * @param contentHash the hash of the content to check
     * @return true if content is available from peers
     */
    boolean isContentAvailable(String contentHash);
    
    /**
     * Download content from P2P network
     * @param contentHash the hash of the content to download
     * @return input stream of the content
     * @throws IOException if download fails
     */
    InputStream downloadContent(String contentHash) throws IOException;
    
    /**
     * Announce content availability to peers
     * @param contentHash the hash of the content
     * @param sizeBytes the size of the content in bytes
     */
    void announceContent(String contentHash, long sizeBytes);
    
    /**
     * Remove content from local cache
     * @param contentHash the hash of the content to remove
     */
    void removeContent(String contentHash);
    
    /**
     * Get list of locally cached content
     * @return list of cached content
     */
    List<P2PContent> getLocalContent();
    
    /**
     * Get content metadata
     * @param contentHash the hash of the content
     * @return content metadata or null if not found
     */
    P2PContent getContentMetadata(String contentHash);
    
    /**
     * Clean up old or unused content from cache
     * @param maxCacheSizeMB maximum cache size in megabytes
     */
    void cleanupCache(long maxCacheSizeMB);
}
