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
import java.util.Objects;

/**
 * Represents a rental session in the Revonixo marketplace
 */
public class RentalSession {
    private Integer id;
    private String sessionId;
    private Integer hostDeviceId;
    private String hostPeerId;
    private Integer renterDeviceId;
    private String renterPeerId;
    private Integer rentalDurationHours;
    private Long startTime;
    private Long endTime;
    private PaymentStatus paymentStatus;
    private String paymentTransactionId;
    private BigDecimal paymentAmount;
    private String paymentCurrency;
    private SessionStatus sessionStatus;
    private String p2pEncryptionKey;
    private Long createdAt;
    private Long updatedAt;

    public enum PaymentStatus {
        PENDING("pending"),
        CONFIRMED("confirmed"),
        FAILED("failed"),
        REFUNDED("refunded");

        private final String value;

        PaymentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SessionStatus {
        PENDING("pending"),
        ACTIVE("active"),
        EXPIRED("expired"),
        TERMINATED("terminated"),
        CANCELLED("cancelled");

        private final String value;

        SessionStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public RentalSession() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getHostDeviceId() {
        return hostDeviceId;
    }

    public void setHostDeviceId(Integer hostDeviceId) {
        this.hostDeviceId = hostDeviceId;
    }

    public String getHostPeerId() {
        return hostPeerId;
    }

    public void setHostPeerId(String hostPeerId) {
        this.hostPeerId = hostPeerId;
    }

    public Integer getRenterDeviceId() {
        return renterDeviceId;
    }

    public void setRenterDeviceId(Integer renterDeviceId) {
        this.renterDeviceId = renterDeviceId;
    }

    public String getRenterPeerId() {
        return renterPeerId;
    }

    public void setRenterPeerId(String renterPeerId) {
        this.renterPeerId = renterPeerId;
    }

    public Integer getRentalDurationHours() {
        return rentalDurationHours;
    }

    public void setRentalDurationHours(Integer rentalDurationHours) {
        this.rentalDurationHours = rentalDurationHours;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getP2pEncryptionKey() {
        return p2pEncryptionKey;
    }

    public void setP2pEncryptionKey(String p2pEncryptionKey) {
        this.p2pEncryptionKey = p2pEncryptionKey;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return sessionStatus == SessionStatus.ACTIVE && 
               paymentStatus == PaymentStatus.CONFIRMED &&
               endTime != null && 
               System.currentTimeMillis() < endTime;
    }

    public boolean isExpired() {
        return endTime != null && System.currentTimeMillis() >= endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentalSession that = (RentalSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "RentalSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", hostDeviceId=" + hostDeviceId +
                ", renterDeviceId=" + renterDeviceId +
                ", sessionStatus=" + sessionStatus +
                ", paymentStatus=" + paymentStatus +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
