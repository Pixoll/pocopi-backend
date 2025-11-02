package com.pocopi.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.event_log.NewOptionEventLog;
import com.pocopi.api.services.EventLogService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TimeLogEventWebSocketHandler extends TextWebSocketHandler {
    private final EventLogService timeLogsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TimeLogEventWebSocketHandler(EventLogService timeLogsService) {
        this.timeLogsService = timeLogsService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("ws connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        final String payload = message.getPayload();
        System.out.println("received: " + payload);

        final NewOptionEventLog optionEvent = objectMapper.readValue(payload, NewOptionEventLog.class);

        timeLogsService.saveOptionEventLog(optionEvent, 0);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        System.out.println("ws connection closed: " + session.getId());
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
        System.err.println("ws error: " + exception.getMessage());
    }
}
