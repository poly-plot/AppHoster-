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

import javax.websocket.server.ServerEndpointConfig;

/**
 * Configurator for WebSocket signaling server
 * Handles server-side configuration and initialization
 */
public class SignalingServerConfigurator extends ServerEndpointConfig.Configurator {
    
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                               javax.websocket.server.HandshakeRequest request,
                               javax.websocket.HandshakeResponse response) {
        // Extract peerId from query parameters if available
        String query = request.getQueryString();
        if (query != null && query.contains("peerId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("peerId=")) {
                    String peerId = param.substring("peerId=".length());
                    config.getUserProperties().put("peerId", peerId);
                    break;
                }
            }
        }
    }
}
