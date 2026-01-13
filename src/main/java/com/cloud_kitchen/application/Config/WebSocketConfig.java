package com.cloud_kitchen.application.Config;

import com.cloud_kitchen.application.Service.OrderChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private OrderChatWebSocketHandler orderChatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderChatWebSocketHandler, "/ws/chat/order/{orderId}/{userId}")
                .setAllowedOriginPatterns(
                    // Local development
                    "http://localhost:*", 
                    "https://localhost:*", 
                    "http://127.0.0.1:*", 
                    "https://127.0.0.1:*",
                    // Production - Vercel deployment
                    "https://*.vercel.app",
                    "https://vercel.app",
                    // Add your specific Vercel domain if you have a custom domain
                    "https://your-app-name.vercel.app"
                )
                .withSockJS(); // Enable SockJS fallback for better compatibility
    }
}