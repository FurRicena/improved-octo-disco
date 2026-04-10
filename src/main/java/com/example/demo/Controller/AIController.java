package com.example.demo.Controller;

import com.example.demo.Service.AIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String msg) {
        return aiService.chat(msg);
    }

    @GetMapping(value = "/stream-chat", produces = "text/event-stream")
    public Flux<String> streamChat(@RequestParam String msg) {
        return aiService.streamChat(msg);
    }
}
