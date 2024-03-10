package com.backend.controller;

import com.backend.model.Draw;
import com.backend.model.Line;
import com.backend.service.DrawService;
import com.backend.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DrawController {

    private final SocketService socketService;
    @GetMapping()
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok().body("Hello world");
    }

//    @PostMapping("/client-ready")
//    public ResponseEntity<Void> handleClientReady() {
//        // Handle the 'client-ready' event
//        // Perform any necessary initialization logic
//        socketService.onSendGetCanvasState();
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/canvas-state")
//    public ResponseEntity<String> handleCanvasState(@RequestBody String canvasState) {
//        // Handle the 'canvas-state' event
//        socketService.onSendCanvasStateFromServer(canvasState);
//        return ResponseEntity.ok("Canvas state received and sent to clients");
//    }
//
//    @PostMapping("/draw-line")
//    public ResponseEntity<String> handleDrawLine(@RequestBody Line line) {
//        // Handle the 'canvas-state' event
//        // Save the received canvas state to the backend
//        socketService.onSendDrawLine(line);
//        return ResponseEntity.ok("Line received and sent to clients");
//    }
//
//    @PostMapping("/clear")
//    public ResponseEntity<String> handleClear() {
//        // Handle the 'clear' event
//        // Clear the canvas
//        socketService.onSendClear();
//        return ResponseEntity.ok("Canvas cleared");
//    }

}
