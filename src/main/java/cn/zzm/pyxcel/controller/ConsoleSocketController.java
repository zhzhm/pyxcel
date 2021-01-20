package cn.zzm.pyxcel.controller;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/console")
@Singleton
public class ConsoleSocketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSocketController.class);
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
        var resp = buildResponse("init");
        resp.put("wsId", session.getId());
        send(session, resp.toString());
        broadcast("sessionCounter", sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
        broadcast("sessionCounter", sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session.getId());
        broadcast("sessionCounter", sessions.size());
        LOGGER.error(session.getId(), throwable);
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.info(message);
    }

    private void send(Session session, String message) {
        session.getAsyncRemote().sendObject(message, result -> {
            if (result.getException() != null) {
                if (result.getException() instanceof ClosedChannelException) {
                    if (sessions.containsKey(session.getId())) {
                        sessions.remove(session.getId());
                    }
                }
                LOGGER.error("Unable to send message ");

            }
        });

    }

    public void send(String sessionId, String message) {
        if (sessions.containsKey(sessionId))
            send(sessions.get(sessionId), message);
    }

    private static JsonObject buildResponse(String type) {
        var response = new JsonObject();
        response.put("type", type);
        return response;
    }

    public static Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void broadcast(String type, Object message) {
        var resp = buildResponse(type);
        resp.put("message", message);
        var it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            var session = it.next().getValue();
            session.getAsyncRemote().sendObject(resp.toString(), result -> {
                if (result.getException() != null) {
                    if (result.getException() instanceof ClosedChannelException) {
                        it.remove();
                    }
                    LOGGER.error("Unable to broadcast message ");
                }
            });
        }
    }
}
