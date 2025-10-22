package com.pocopi.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.time_log.NewTimeLogEvent;
import com.pocopi.api.services.TimeLogService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TimeLogEventWebSocketHandler extends TextWebSocketHandler {
    private final TimeLogService timeLogsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TimeLogEventWebSocketHandler(TimeLogService timeLogsService) {
        this.timeLogsService = timeLogsService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("ws connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("received: " + payload);

        NewTimeLogEvent optionEvent = objectMapper.readValue(payload, NewTimeLogEvent.class);

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