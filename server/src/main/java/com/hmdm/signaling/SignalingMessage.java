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

package com.hmdm.signaling;

/**
 * Signaling message for P2P handshake coordination
 */
public class SignalingMessage {
    private SignalingMessageType type;
    private String from;
    private String to;
    private Object data;
    private long timestamp;

    public SignalingMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public SignalingMessage(SignalingMessageType type, String from, String to, Object data) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public SignalingMessageType getType() {
        return type;
    }

    public void setType(SignalingMessageType type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SignalingMessage{" +
                "type=" + type +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
