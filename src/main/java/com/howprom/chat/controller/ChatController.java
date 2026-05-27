package com.howprom.chat.controller;

import com.howprom.chat.dto.ChatMessageRequest;
import com.howprom.chat.dto.ChatMessageResponse;
import com.howprom.chat.service.ChatService;
import com.howprom.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        ChatMessageResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
}