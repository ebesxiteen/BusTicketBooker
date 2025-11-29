package com.example.ticketbooker.Config.Security;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // Builder này Spring AI auto-config khi bà dùng spring-ai-starter-model-openai
        return builder.build();
    }
}