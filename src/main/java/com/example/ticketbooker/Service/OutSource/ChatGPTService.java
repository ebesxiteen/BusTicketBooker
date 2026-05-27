package com.example.ticketbooker.Service.OutSource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatGPTService {

    private final ChatClient chatClient;

    public ChatGPTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // Tên method này đúng với cái controller đang gọi: askChatGPT(String)
    public String askChatGPT(String userMessage) {

        String systemPrompt = """
                Bạn là trợ lý ảo của hãng xe khách GreenBus.
                Hãy trả lời bằng tiếng Việt, thân thiện, rõ ràng, ưu tiên câu trả lời ngắn gọn.
                Nếu câu hỏi không liên quan tới xe khách, vẫn trả lời bình thường.
                """;

        // ✅ CÚ PHÁP ĐÚNG CỦA SPRING AI 1.1.0
        String answer = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()      // trả về CallResponseSpec
                .content();  // lấy String nội dung trả lời

        return answer;
    }

    // Nếu chỗ khác trong code gọi askDetailChatGPT thì cho nó dùng chung
    public String askDetailChatGPT(String userMessage) {
        return askChatGPT(userMessage);
    }
}
