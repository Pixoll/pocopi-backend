package com.pocopi.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.TimeLog.SendOptionEvent;
import com.pocopi.api.services.interfaces.TimeLogsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class OptionEventWebSocketHandler extends TextWebSocketHandler {

    private final TimeLogsService timeLogsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OptionEventWebSocketHandler(TimeLogsService timeLogsService) {
        this.timeLogsService = timeLogsService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("ws connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("received: " + payload);

        SendOptionEvent optionEvent = objectMapper.readValue(payload, SendOptionEvent.class);

        String result = timeLogsService.addTimeLog(optionEvent);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("ws connection closed: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("ws error: " + exception.getMessage());
    }
}