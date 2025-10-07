package com.pocopi.api.config;

import com.pocopi.api.websocket.OptionEventWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OptionEventWebSocketHandler optionEventWebSocketHandler;

    public WebSocketConfig(OptionEventWebSocketHandler optionEventWebSocketHandler) {
        this.optionEventWebSocketHandler = optionEventWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(optionEventWebSocketHandler, "/ws/option-event")
            .setAllowedOrigins("*");
    }
}