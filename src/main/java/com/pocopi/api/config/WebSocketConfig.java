package com.pocopi.api.config;

import com.pocopi.api.websocket.TimeLogEventWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final TimeLogEventWebSocketHandler timeLogEventWebSocketHandler;

    public WebSocketConfig(TimeLogEventWebSocketHandler timeLogEventWebSocketHandler) {
        this.timeLogEventWebSocketHandler = timeLogEventWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(timeLogEventWebSocketHandler, "/ws/option-event")
            .setAllowedOrigins("*");
    }
}
