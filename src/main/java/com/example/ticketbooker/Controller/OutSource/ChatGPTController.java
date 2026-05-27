package com.example.ticketbooker.Controller.OutSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketbooker.Service.OutSource.ChatGPTService;

@RestController
@RequestMapping("/api/chatgpt")   // ❗ CHỈ /api/chatgpt, KHÔNG /greenbus ở đây
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    @PostMapping("/askDetail")
    public ResponseEntity<String> askDetailChatGPT(@RequestBody String userMessage) {
        String response = chatGPTService.askChatGPT(userMessage);
        return ResponseEntity.ok(response);
    }
}
