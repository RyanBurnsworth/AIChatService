package com.ryanburnsworth.ryanGpt.service.chatService;

import com.ryanburnsworth.ryanGpt.data.dto.RedisChatMessage;
import com.ryanburnsworth.ryanGpt.service.imageService.ImageService;
import com.ryanburnsworth.ryanGpt.service.redis.RedisService;
import com.ryanburnsworth.ryanGpt.utils.Constants;
import com.ryanburnsworth.ryanGpt.utils.Prompts;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.support.Base64Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static com.ryanburnsworth.ryanGpt.utils.Constants.*;

@Service
public class ChatServiceImpl implements ChatService {
    private final SimpleOpenAI openAIClient;
    private final ImageService imageService;
    private final RedisService redisService;

    public ChatServiceImpl(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.organization-id}") String orgId,
            @Value("${openai.project-id}") String projectId,
            final ImageService imageService,
            final RedisService redisService
    ) {
        this.openAIClient = SimpleOpenAI.builder()
                .apiKey(apiKey)
                .organizationId(orgId)
                .projectId(projectId)
                .build();

        this.imageService = imageService;

        this.redisService = redisService;
    }

    @Override
    public String getResponse(String userInput, String base64Image) {
        // get chat completion for an image request
        if (base64Image != null && !base64Image.isEmpty()) {
            ChatRequest chatRequest = getChatCompletionUsingVisionRequest(userInput, base64Image);

            // return the AI output text
            return getChatCompletion(chatRequest, userInput, base64Image);
        }

        // if not base64Image, continue with a text only chat completion request
        ChatRequest chatRequest = getChatCompletionRequest(userInput);

        // return the AI output text
        return getChatCompletion(chatRequest, userInput, "");
    }

    private String getChatCompletion(ChatRequest chatRequest, String userInput, String base64Image) {
        Stream<Chat> chatResponse = openAIClient.chatCompletions().createStream(chatRequest).join();

        StringBuilder sb = new StringBuilder();

        chatResponse
                .filter(chatResp -> !chatResp.getChoices().isEmpty() && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(sb::append);

        String aiOutput = sb.toString();

        // save in Redis
        redisService.saveChatMessageToRedis(userInput, aiOutput, base64Image);

        return aiOutput;
    }

    private ChatRequest getChatCompletionRequest(String userInput) {
        // fetch up to 3 of the latest chat messages from Redis
        List<RedisChatMessage> chatMessages = redisService.getChatMessagesFromRedis(MAX_CHAT_MESSAGES);

        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .model(Constants.GPT_MODEL)
                .message(ChatMessage.SystemMessage.of(Prompts.chatCompletionPrompt))
                .message(ChatMessage.UserMessage.of(userInput))
                .temperature(TEMPERATURE)
                .maxCompletionTokens(MAX_COMPLETION_TOKENS);

        // attach previous messages as context
        for (RedisChatMessage msg : chatMessages) {
            builder.message(ChatMessage.SystemMessage.of(msg.getAiOutput()));
            builder.message(ChatMessage.UserMessage.of(msg.getUserInput()));
        }

        return builder.build();
    }

    private ChatRequest getChatCompletionUsingVisionRequest(String userInput, String base64Image) {
        // store the image file locally in the resources/static folder as file.jpg
        imageService.saveBase64Image(base64Image);

        return ChatRequest.builder()
                .model(Constants.GPT_MODEL)
                .messages(List.of(
                        ChatMessage.UserMessage.of(List.of(
                                ContentPart.ContentPartText.of(userInput),
                                ContentPart.ContentPartImageUrl.of(ContentPart.ContentPartImageUrl.ImageUrl.of(Base64Util.encode(Constants.FILE_LOCATION, Base64Util.MediaType.IMAGE)))))))
                .temperature(0.75)
                .maxCompletionTokens(500)
                .build();
    }
}
