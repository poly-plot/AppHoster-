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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket signaling server for P2P handshake coordination between Host and Renter peers.
 * Implements JSR 356 WebSocket API for real-time bidirectional communication.
 * 
 * Endpoint: ws://{server}/signaling/{sessionId}
 * 
 * Each rental session has its own signaling channel where both Host and Renter connect.
 * The server acts as a relay for ICE candidates, SDP offers/answers, and connection readiness signals.
 */
@ServerEndpoint(value = "/signaling/{sessionId}", 
                configurator = SignalingServerConfigurator.class)
public class SignalingServer {
    
    private static final Logger logger = LoggerFactory.getLogger(SignalingServer.class);
    private static final Gson gson = new Gson();
    
    // Map of sessionId -> Map of peerId -> WebSocket session
    private static final Map<String, Map<String, Session>> sessionPeers = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        String peerId = extractPeerId(session);
        
        if (peerId == null) {
            logger.error("No peerId provided in handshake for sessionId: {}", sessionId);
            closeSession(session, "peerId required in connection parameters");
            return;
        }
        
        logger.info("Peer {} joining signaling session: {}", peerId, sessionId);
        
        // Add peer to session
        sessionPeers.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                    .put(peerId, session);
        
        // Store metadata in session
        session.getUserProperties().put("sessionId", sessionId);
        session.getUserProperties().put("peerId", peerId);
        
        // Notify other peers that this peer has joined
        broadcastPeerJoined(sessionId, peerId);
        
        logger.info("Peer {} successfully joined signaling session: {}. Total peers: {}", 
                   peerId, sessionId, sessionPeers.get(sessionId).size());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        String sessionId = (String) session.getUserProperties().get("sessionId");
        String fromPeerId = (String) session.getUserProperties().get("peerId");
        
        try {
            SignalingMessage signalingMsg = gson.fromJson(message, SignalingMessage.class);
            signalingMsg.setFrom(fromPeerId);
            signalingMsg.setTimestamp(System.currentTimeMillis());
            
            logger.debug("Received signaling message: {} from peer: {} in session: {}", 
                        signalingMsg.getType(), fromPeerId, sessionId);
            
            // Route message to target peer
            String targetPeerId = signalingMsg.getTo();
            if (targetPeerId != null && !targetPeerId.isEmpty()) {
                forwardMessageToPeer(sessionId, targetPeerId, signalingMsg);
            } else {
                // Broadcast to all other peers in the session
                broadcastMessage(sessionId, fromPeerId, signalingMsg);
            }
            
        } catch (Exception e) {
            logger.error("Error processing signaling message from peer: {} in session: {}", 
                        fromPeerId, sessionId, e);
            sendError(session, "Invalid message format");
        }
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("sessionId") String sessionId) {
        String peerId = (String) session.getUserProperties().get("peerId");
        
        if (peerId != null) {
            logger.info("Peer {} leaving signaling session: {}", peerId, sessionId);
            
            // Remove peer from session
            Map<String, Session> peers = sessionPeers.get(sessionId);
            if (peers != null) {
                peers.remove(peerId);
                
                // Notify other peers
                broadcastPeerLeft(sessionId, peerId);
                
                // Clean up empty sessions
                if (peers.isEmpty()) {
                    sessionPeers.remove(sessionId);
                    logger.info("Signaling session {} cleaned up (no more peers)", sessionId);
                }
            }
        }
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = (String) session.getUserProperties().get("sessionId");
        String peerId = (String) session.getUserProperties().get("peerId");
        
        logger.error("WebSocket error for peer: {} in session: {}", peerId, sessionId, error);
    }
    
    /**
     * Extract peerId from session handshake parameters
     */
    private String extractPeerId(Session session) {
        Map<String, String> pathParams = session.getPathParameters();
        String queryString = session.getQueryString();
        
        // Try to get peerId from query parameters
        if (queryString != null && queryString.contains("peerId=")) {
            String[] params = queryString.split("&");
            for (String param : params) {
                if (param.startsWith("peerId=")) {
                    return param.substring("peerId=".length());
                }
            }
        }
        
        // Try user properties (set by configurator)
        Object peerIdObj = session.getUserProperties().get("peerId");
        if (peerIdObj != null) {
            return peerIdObj.toString();
        }
        
        return null;
    }
    
    /**
     * Forward message to a specific peer
     */
    private void forwardMessageToPeer(String sessionId, String targetPeerId, SignalingMessage message) {
        Map<String, Session> peers = sessionPeers.get(sessionId);
        if (peers != null) {
            Session targetSession = peers.get(targetPeerId);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    String json = gson.toJson(message);
                    targetSession.getBasicRemote().sendText(json);
                    logger.debug("Forwarded {} from {} to {} in session: {}", 
                                message.getType(), message.getFrom(), targetPeerId, sessionId);
                } catch (IOException e) {
                    logger.error("Error forwarding message to peer: {} in session: {}", 
                                targetPeerId, sessionId, e);
                }
            } else {
                logger.warn("Target peer {} not found or disconnected in session: {}", 
                           targetPeerId, sessionId);
            }
        }
    }
    
    /**
     * Broadcast message to all peers except sender
     */
    private void broadcastMessage(String sessionId, String fromPeerId, SignalingMessage message) {
        Map<String, Session> peers = sessionPeers.get(sessionId);
        if (peers != null) {
            String json = gson.toJson(message);
            peers.forEach((peerId, peerSession) -> {
                if (!peerId.equals(fromPeerId) && peerSession.isOpen()) {
                    try {
                        peerSession.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        logger.error("Error broadcasting to peer: {} in session: {}", 
                                    peerId, sessionId, e);
                    }
                }
            });
        }
    }
    
    /**
     * Notify all peers that a new peer has joined
     */
    private void broadcastPeerJoined(String sessionId, String joinedPeerId) {
        SignalingMessage message = new SignalingMessage(
            SignalingMessageType.PEER_JOINED,
            "server",
            null,
            joinedPeerId
        );
        broadcastMessage(sessionId, joinedPeerId, message);
    }
    
    /**
     * Notify all peers that a peer has left
     */
    private void broadcastPeerLeft(String sessionId, String leftPeerId) {
        SignalingMessage message = new SignalingMessage(
            SignalingMessageType.PEER_LEFT,
            "server",
            null,
            leftPeerId
        );
        Map<String, Session> peers = sessionPeers.get(sessionId);
        if (peers != null) {
            String json = gson.toJson(message);
            peers.forEach((peerId, peerSession) -> {
                if (peerSession.isOpen()) {
                    try {
                        peerSession.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        logger.error("Error notifying peer: {} in session: {}", 
                                    peerId, sessionId, e);
                    }
                }
            });
        }
    }
    
    /**
     * Send error message to a peer
     */
    private void sendError(Session session, String errorMessage) {
        SignalingMessage message = new SignalingMessage(
            SignalingMessageType.ERROR,
            "server",
            null,
            errorMessage
        );
        try {
            String json = gson.toJson(message);
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            logger.error("Error sending error message", e);
        }
    }
    
    /**
     * Close a session with a reason
     */
    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
        } catch (IOException e) {
            logger.error("Error closing session", e);
        }
    }
    
    /**
     * Get active sessions count for monitoring
     */
    public static int getActiveSessionsCount() {
        return sessionPeers.size();
    }
    
    /**
     * Get total peers count across all sessions
     */
    public static int getTotalPeersCount() {
        return sessionPeers.values().stream()
                          .mapToInt(Map::size)
                          .sum();
    }
}
