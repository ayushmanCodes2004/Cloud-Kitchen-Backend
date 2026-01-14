package com.cloud_kitchen.application.Config;

import com.cloud_kitchen.application.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        URI uri = request.getURI();
        String query = uri.getQuery();
        
        log.info("=== WebSocket Handshake Debug ===");
        log.info("Full URI: {}", uri);
        log.info("Query string: {}", query);
        
        if (query != null && !query.isEmpty()) {
            String[] params = query.split("&");
            String token = null;
            String orderId = null;
            String userId = null;
            
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    log.info("Query param: {} = {}", keyValue[0], keyValue[0].equals("token") ? "[TOKEN]" : keyValue[1]);
                    if ("token".equals(keyValue[0])) {
                        token = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    } else if ("orderId".equals(keyValue[0])) {
                        orderId = keyValue[1];
                    } else if ("userId".equals(keyValue[0])) {
                        userId = keyValue[1];
                    }
                }
            }
            
            log.info("Extracted - orderId: {}, userId: {}, token present: {}", orderId, userId, token != null);
            
            if (token != null) {
                try {
                    log.info("Attempting to validate JWT token...");
                    
                    // Validate JWT token
                    boolean isValid = jwtTokenProvider.validateToken(token);
                    log.info("Token validation result: {}", isValid);
                    
                    if (isValid) {
                        Long userIdFromToken = jwtTokenProvider.getUserIdFromToken(token);
                        log.info("✅ WebSocket authentication successful for userId: {}", userIdFromToken);
                        attributes.put("userId", userIdFromToken);
                        attributes.put("token", token);
                        return true;
                    } else {
                        log.warn("❌ Invalid JWT token in WebSocket handshake");
                    }
                } catch (Exception e) {
                    log.error("❌ Error validating JWT token in WebSocket handshake", e);
                    log.error("Exception type: {}", e.getClass().getName());
                    log.error("Exception message: {}", e.getMessage());
                }
            } else {
                log.warn("❌ No token provided in WebSocket handshake");
            }
        } else {
            log.warn("❌ No query parameters in WebSocket handshake");
        }
        
        log.info("=== Handshake REJECTED ===");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake failed: {}", exception.getMessage());
        }
    }
}
