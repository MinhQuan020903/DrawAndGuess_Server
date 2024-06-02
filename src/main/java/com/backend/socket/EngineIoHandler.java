package com.backend.socket;

import com.backend.configuration.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.socket.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EngineIoHandler implements HandshakeInterceptor, WebSocketHandler {

    private static final String ATTRIBUTE_ENGINE_IO_BRIDGE = "engine.io.bridge";
    private static final String ATTRIBUTE_ENGINE_IO_QUERY = "engine.io.query";
    private static final String ATTRIBUTE_ENGINE_IO_HEADERS = "engine.io.headers";

    private final JwtService jwtService;


    private final EngineIoServer mEngineIoServer;

    public EngineIoHandler(JwtService jwtService, EngineIoServer engineIoServer) {
        this.jwtService = jwtService;
        mEngineIoServer = engineIoServer;
    }

    @RequestMapping(
            value = "/socket.io/",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
            headers = "Connection!=Upgrade")
    public void httpHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        mEngineIoServer.handleRequest(request, response);
    }

    private boolean verifyToken(String token) {
        try {
            final String username = jwtService.extractUsername(token);
            return username != null && !jwtService.isTokenExpired(token);
        } catch (Exception e) {
            System.out.println("Token verification failed: " + e.getMessage());
            return false; // Token verification failed
        }
    }


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {

        attributes.put(ATTRIBUTE_ENGINE_IO_QUERY, request.getURI().getQuery());
        attributes.put(ATTRIBUTE_ENGINE_IO_HEADERS, request.getHeaders());

        // Verify token here
        String queryString = request.getURI().getQuery();
        if (queryString != null && queryString.contains("token")) {
            String token = null;
            for (String param : queryString.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.split("=", 2)[1];
                    break;
                }
            }
            if (verifyToken(token)) {
                return true; // Token is valid
            }
        } else {
            assert queryString != null;
            if (!queryString.contains("token")) {
                System.out.println("Query string: " + queryString +  ", Token is not valid or missing");
                // If token is not valid, reject the handshake
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }
        }

        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
    /* WebSocketHandler */

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        final EngineIoSpringWebSocket webSocket = new EngineIoSpringWebSocket(webSocketSession);
        webSocketSession.getAttributes().put(ATTRIBUTE_ENGINE_IO_BRIDGE, webSocket);
        mEngineIoServer.handleWebSocket(webSocket);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        ((EngineIoSpringWebSocket)webSocketSession.getAttributes().get(ATTRIBUTE_ENGINE_IO_BRIDGE))
                .afterConnectionClosed(closeStatus);
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        ((EngineIoSpringWebSocket)webSocketSession.getAttributes().get(ATTRIBUTE_ENGINE_IO_BRIDGE))
                .handleMessage(webSocketMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        ((EngineIoSpringWebSocket)webSocketSession.getAttributes().get(ATTRIBUTE_ENGINE_IO_BRIDGE))
                .handleTransportError(throwable);
    }

    private static final class EngineIoSpringWebSocket extends EngineIoWebSocket {

        private final WebSocketSession mSession;
        private final Map<String, String> mQuery;
        private final Map<String, List<String>> mHeaders;

        EngineIoSpringWebSocket(WebSocketSession session) {
            mSession = session;

            final String queryString = (String)mSession.getAttributes().get(ATTRIBUTE_ENGINE_IO_QUERY);
            if (queryString != null) {
                mQuery = ParseQS.decode(queryString);
            } else {
                mQuery = new HashMap<>();
            }
            this.mHeaders = (Map<String, List<String>>) mSession.getAttributes().get(ATTRIBUTE_ENGINE_IO_HEADERS);
        }

        /* EngineIoWebSocket */

        @Override
        public Map<String, String> getQuery() {
            return mQuery;
        }

        @Override
        public Map<String, List<String>> getConnectionHeaders() {
            return mHeaders;
        }

        @Override
        public void write(String message) throws IOException {
            mSession.sendMessage(new TextMessage(message));
        }

        @Override
        public void write(byte[] message) throws IOException {
            mSession.sendMessage(new BinaryMessage(message));
        }

        @Override
        public void close() {
            try {
                mSession.close();
            } catch (IOException ignore) {
            }
        }

        /* WebSocketHandler */

        void afterConnectionClosed(CloseStatus closeStatus) {
            emit("close");
        }

        void handleMessage(WebSocketMessage<?> message) {
            if (message.getPayload() instanceof String || message.getPayload() instanceof byte[]) {
                emit("message", (Object) message.getPayload());
            } else {
                throw new RuntimeException(String.format(
                        "Invalid message type received: %s. Expected String or byte[].",
                        message.getPayload().getClass().getName()));
            }
        }

        void handleTransportError(Throwable exception) {
            emit("error", "write error", exception.getMessage());
        }
    }
}