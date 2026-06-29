package com.timeweaver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timeweaver.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    // userId -> session mapping
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public NotificationHandler(ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.debug("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket connection rejected: invalid token, sessionId={}", session.getId());
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.remove(userId, session);
            log.debug("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client messages are currently not processed server-side
    }

    public void sendNotification(Long userId, String title, String content) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) return;

        try {
            Map<String, Object> payload = Map.of(
                    "type", "notification",
                    "title", title,
                    "content", content,
                    "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (IOException e) {
            log.warn("Failed to send WS notification to userId={}: {}", userId, e.getMessage());
            sessions.remove(userId);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if ("token".equals(parts[0]) && parts.length > 1) {
                try {
                    var claims = jwtUtil.parseToken(parts[1]);
                    return claims.get("user_id", Long.class);
                } catch (Exception e) {
                    log.warn("Invalid WS token: {}", e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }
}
