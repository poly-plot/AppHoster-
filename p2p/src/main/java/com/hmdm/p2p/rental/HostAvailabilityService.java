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

import java.util.List;

/**
 * Service for managing host availability in Revonixo marketplace
 */
public interface HostAvailabilityService {
    
    /**
     * Register a device as available for rental
     * @param availability host availability configuration
     * @return registered host availability
     */
    HostAvailability registerHost(HostAvailability availability);
    
    /**
     * Update host availability status
     * @param deviceId the device ID
     * @param isAvailable availability status
     * @return updated host availability
     */
    HostAvailability updateAvailability(Integer deviceId, Boolean isAvailable);
    
    /**
     * Get host availability by device ID
     * @param deviceId the device ID
     * @return host availability or null if not found
     */
    HostAvailability getHostAvailability(Integer deviceId);
    
    /**
     * Search for available hosts
     * @param maxHourlyRate maximum hourly rate filter
     * @param minRating minimum rating filter
     * @return list of available hosts
     */
    List<HostAvailability> searchAvailableHosts(Double maxHourlyRate, Double minRating);
    
    /**
     * Remove host from marketplace
     * @param deviceId the device ID
     * @return true if removed successfully
     */
    boolean unregisterHost(Integer deviceId);
}
