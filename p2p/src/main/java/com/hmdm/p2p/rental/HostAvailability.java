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

/**
 * Represents host device availability in the Revonixo marketplace
 */
public class HostAvailability {
    private Integer id;
    private Integer deviceId;
    private String peerId;
    private Boolean isAvailable;
    private BigDecimal hourlyRate;
    private String currency;
    private Integer maxRentalHours;
    private String deviceSpecs;
    private String availabilitySchedule;
    private Integer totalRentals;
    private BigDecimal averageRating;
    private Long lastUpdated;
    private Long createdAt;

    public HostAvailability() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getMaxRentalHours() {
        return maxRentalHours;
    }

    public void setMaxRentalHours(Integer maxRentalHours) {
        this.maxRentalHours = maxRentalHours;
    }

    public String getDeviceSpecs() {
        return deviceSpecs;
    }

    public void setDeviceSpecs(String deviceSpecs) {
        this.deviceSpecs = deviceSpecs;
    }

    public String getAvailabilitySchedule() {
        return availabilitySchedule;
    }

    public void setAvailabilitySchedule(String availabilitySchedule) {
        this.availabilitySchedule = availabilitySchedule;
    }

    public Integer getTotalRentals() {
        return totalRentals;
    }

    public void setTotalRentals(Integer totalRentals) {
        this.totalRentals = totalRentals;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "HostAvailability{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", isAvailable=" + isAvailable +
                ", hourlyRate=" + hourlyRate +
                ", currency='" + currency + '\'' +
                '}';
    }
}
