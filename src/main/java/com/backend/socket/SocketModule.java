package com.backend.socket;

import com.backend.model.Line;
import com.backend.service.SocketService;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class SocketModule {

    private final SocketIOServer server;

    private final SocketService socketService;

    public SocketModule(SocketIOServer server, SocketService socketService) {
        this.server = server;
        this.socketService = socketService;
        server.addConnectListener(this.onConnected());
        server.addDisconnectListener(this.onDisconnected());
        server.addEventListener("client-ready", Void.class, this.onClientReadyReceived());
        server.addEventListener("canvas-state", Object.class, this.onCanvasStateReceived());
        server.addEventListener("draw-line", Object.class, this.onDrawLineReceived());
        server.addEventListener("clear", Void.class, this.onClearReceived());
    }

    private DataListener<Void> onClientReadyReceived() {
        return (senderClient, data, ackSender) -> {
            socketService.onSendGetCanvasState(senderClient);
        };
    }

    private DataListener<Object> onCanvasStateReceived() {
        return (senderClient, data, ackSender) -> {
            try {
                System.out.println("Received data: " + data.toString());
                socketService.onSendCanvasStateFromServer(senderClient, data);
//                if (data != null && data instanceof Object[] && ((Object[]) data).length >= 2){
//                    String eventName = (String) data[0];
//                    System.out.println("Received event name: " + eventName);
//
//                    // Assuming the second element is a Map
//                    Map<String, Object> eventDataMap = (Map<String, Object>) data[1];
//
//                    System.out.println("Received data: " + eventDataMap);
//
//                    // Handle the event and associated data
//                    if ("canvas-state".equals(eventName)) {
//                        // Handle canvas state event data
//                        String canvasState = (String) eventDataMap.get("canvasState");
//                        String point = (String) eventDataMap.get("point");
//                        System.out.println("Canvas state: " + canvasState);
//                        System.out.println("Point: " + point);
//                    }
//                    // Handle other event types if needed
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }


    private DataListener<Object> onDrawLineReceived() {
        return (senderClient, data, ackSender) -> {
            log.info(data.toString());
            socketService.onSendDrawLine(senderClient, data);
        };
    }

    private DataListener<Void> onClearReceived() {
        return (senderClient, data, ackSender) -> {
            socketService.onSendClear(senderClient);
        };
    }

    private ConnectListener onConnected() {
        return (client) -> {
            var params = client.getHandshakeData().getUrlParams();
            System.out.println("params: " + params.get("room").getFirst());
            String room = params.get("room").getFirst();
            client.joinRoom(room);
            //            String username = params.get("username").stream().collect(Collectors.joining());
//            socketService.saveInfoMessage(client, String.format(Constants.WELCOME_MESSAGE, username), room);
//            log.info("Socket ID[{}] - room[{}] - username [{}]  Connected to chat module through", client.getSessionId().toString(), room, username);
        };
    }


    private DisconnectListener onDisconnected() {
        return client -> {
            var params = client.getHandshakeData().getUrlParams();
            String room = params.get("room").stream().collect(Collectors.joining());
//            String username = params.get("username").stream().collect(Collectors.joining());
//            socketService.saveInfoMessage(client, String.format(Constants.DISCONNECT_MESSAGE, username), room);
//            log.info("Socket ID[{}] - room[{}] - username [{}]  discnnected to chat module through", client.getSessionId().toString(), room, username);
        };
    }

}
