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

package com.hmdm.p2p.rental;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing rental sessions in Revonixo marketplace
 */
public interface RentalSessionService {
    
    /**
     * Create a new rental session
     * @param hostDeviceId the host device ID
     * @param renterDeviceId the renter device ID
     * @param durationHours rental duration in hours
     * @param amount payment amount
     * @param currency payment currency
     * @return created rental session
     */
    RentalSession createSession(Integer hostDeviceId, Integer renterDeviceId, 
                                Integer durationHours, BigDecimal amount, String currency);
    
    /**
     * Confirm payment for a rental session
     * @param sessionId the session ID
     * @param transactionId payment transaction ID
     * @return updated rental session
     */
    RentalSession confirmPayment(String sessionId, String transactionId);
    
    /**
     * Start a rental session (activates the P2P connection)
     * @param sessionId the session ID
     * @return updated rental session
     */
    RentalSession startSession(String sessionId);
    
    /**
     * Terminate a rental session
     * @param sessionId the session ID
     * @param reason termination reason
     * @return updated rental session
     */
    RentalSession terminateSession(String sessionId, String reason);
    
    /**
     * Get rental session by ID
     * @param sessionId the session ID
     * @return rental session or null if not found
     */
    RentalSession getSession(String sessionId);
    
    /**
     * Get active sessions for a host device
     * @param deviceId the device ID
     * @return list of active sessions
     */
    List<RentalSession> getActiveSessionsForHost(Integer deviceId);
    
    /**
     * Get active sessions for a renter device
     * @param deviceId the device ID
     * @return list of active sessions
     */
    List<RentalSession> getActiveSessionsForRenter(Integer deviceId);
    
    /**
     * Check for expired sessions and terminate them
     * @return number of sessions terminated
     */
    int cleanupExpiredSessions();
    
    /**
     * Validate if a device can start a rental session
     * @param deviceId the device ID
     * @return true if device can rent
     */
    boolean canDeviceRent(Integer deviceId);
    
    /**
     * Generate P2P handshake credentials for a session
     * @param sessionId the session ID
     * @return handshake credentials (encryption key, connection info)
     */
    String generateHandshakeCredentials(String sessionId);
}
