package com.backend.service;

import com.backend.model.Draw;
import com.backend.model.Line;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))

public class SocketService {

//    private final DrawService drawService;

    public void sendSocketMessage(String eventName, SocketIOClient senderClient, Object data) {
        System.out.println("sendSocketMessage");
        for (
                SocketIOClient client: senderClient.getNamespace().getAllClients()
        ) {
//            if (!client.getSessionId().equals(senderClient.getSessionId())) {
//
            client.sendEvent(eventName, data);
        }
    }
    public void onSendGetCanvasState(SocketIOClient senderClient) {
        sendSocketMessage("get-canvas-state",senderClient, String.valueOf("requesting client canvas state"));
    }
    public void onSendCanvasStateFromServer(SocketIOClient senderClient, Object canvasState) {
        sendSocketMessage("canvas-state-from-server", senderClient, canvasState);
        System.out.println("onSendCanvasStateFromServer");
    }
    public void onSendDrawLine(SocketIOClient senderClient, Object line) {
        sendSocketMessage("draw-line", senderClient, line);
    }
    public void onSendClear(SocketIOClient senderClient){
        sendSocketMessage("clear", senderClient, String.valueOf("clearing canvas"));
    }
}
