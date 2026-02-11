package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import com.backend.lxy.domain.dto.ChatRequestDTO;
import com.backend.lxy.domain.dto.ChatResponseDTO;
import com.backend.lxy.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI chat APIs")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    public ApiResponse<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) throws IOException {
        ChatResponseDTO response = aiService.chat(request);
        return ApiResponse.success(response);
    }
}
