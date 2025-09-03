package com.ryanburnsworth.ryanGpt.service.chatService;

import com.ryanburnsworth.ryanGpt.data.dto.ChatMessage;
import com.ryanburnsworth.ryanGpt.data.dto.VisionChatRequest;
import com.ryanburnsworth.ryanGpt.service.databaseService.DatabaseService;
import com.ryanburnsworth.ryanGpt.service.imageService.ImageService;
import com.ryanburnsworth.ryanGpt.service.redis.RedisService;
import com.ryanburnsworth.ryanGpt.utils.Constants;
import com.ryanburnsworth.ryanGpt.utils.Prompts;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.support.Base64Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.ryanburnsworth.ryanGpt.utils.Constants.*;

@Service
public class ChatServiceImpl implements ChatService {
    private final SimpleOpenAI openAIClient;
    private final ImageService imageService;
    private final RedisService redisService;
    private final DatabaseService databaseService;

    public ChatServiceImpl(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.organization-id}") String orgId,
            @Value("${openai.project-id}") String projectId,
            final ImageService imageService,
            final RedisService redisService,
            final DatabaseService databaseService
    ) {
        this.openAIClient = SimpleOpenAI.builder()
                .apiKey(apiKey)
                .organizationId(orgId)
                .projectId(projectId)
                .build();

        this.imageService = imageService;

        this.redisService = redisService;

        this.databaseService = databaseService;
    }

    @Override
    public String getResponse(String userInput, String base64Image) {
        // get chat completion for an image request
        if (base64Image != null && !base64Image.isEmpty()) {
            VisionChatRequest visionChatRequest = getChatCompletionUsingVisionRequest(userInput, base64Image);

            // return the AI output text
            return getChatCompletion(visionChatRequest.getChatRequest(), userInput, base64Image, visionChatRequest.getFilename());
        }

        // if not base64Image, continue with a text only chat completion request
        ChatRequest chatRequest = getChatCompletionRequest(userInput);

        // return the AI output text
        return getChatCompletion(chatRequest, userInput, "", "");
    }

    private String getChatCompletion(ChatRequest chatRequest, String userInput, String base64Image, String imageFilename) {
        Stream<Chat> chatResponse = openAIClient.chatCompletions().createStream(chatRequest).join();

        StringBuilder sb = new StringBuilder();

        chatResponse
                .filter(chatResp -> !chatResp.getChoices().isEmpty() && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(sb::append);

        String aiOutput = sb.toString();

        // store chat message in data storage
        saveChatMessage(userInput, aiOutput, base64Image);

        if (imageFilename != null && !imageFilename.isEmpty()) {
            removeImageFile(imageFilename);
        }

        return aiOutput;
    }

    private ChatRequest getChatCompletionRequest(String userInput) {
        // fetch up to 3 of the latest chat messages from our datasource
        List<ChatMessage> chatMessages = getLatestMessages();

        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .model(Constants.GPT_MODEL)
                .message(io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage.of(Prompts.chatCompletionPrompt))
                .message(io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage.of(userInput))
                .temperature(TEMPERATURE)
                .maxCompletionTokens(MAX_COMPLETION_TOKENS);

        // attach previous messages as context
        for (ChatMessage msg : chatMessages) {
            builder.message(io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage.of(msg.getAiOutput()));
            builder.message(io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage.of(msg.getUserInput()));
        }

        return builder.build();
    }

    private VisionChatRequest getChatCompletionUsingVisionRequest(String userInput, String base64Image) {
        // store the image file locally in the resources/static folder as file.jpg
        String filename = imageService.saveBase64Image(base64Image);

        ChatRequest request = ChatRequest.builder()
                .model(Constants.GPT_MODEL)
                .messages(List.of(
                        io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage.of(List.of(
                                ContentPart.ContentPartText.of(userInput),
                                ContentPart.ContentPartImageUrl.of(ContentPart.ContentPartImageUrl.ImageUrl.of(Base64Util.encode(Constants.FILE_LOCATION, Base64Util.MediaType.IMAGE)))))))
                .temperature(0.75)
                .maxCompletionTokens(500)
                .build();

        return VisionChatRequest.builder()
                .filename(filename)
                .chatRequest(request)
                .build();
    }

    private List<ChatMessage> getLatestMessages() {
        // fetch up to 3 of the latest chat messages from Redis
        List<ChatMessage> chatMessages = redisService.getLatestChatMessages(MAX_CHAT_MESSAGES);

        // if Redis doesn't contain any messages check the Postgres (long-term memory)
        if (chatMessages.isEmpty()) {
            chatMessages = databaseService.getLatestChatMessages(MAX_CHAT_MESSAGES);
        }

        return chatMessages;
    }

    @Async
    private void saveChatMessage(String userInput, String aiOutput, String base64Image) {
        // run the saveChatMessage methods asynchronously to not block main thread
        CompletableFuture.runAsync(() -> {
            ChatMessage chatMessage = ChatMessage.builder()
                    .userInput(userInput)
                    .aiOutput(aiOutput)
                    .base64Image(base64Image)
                    .build();

            // save in Redis
            redisService.saveChatMessage(chatMessage);

            // save to Postgres
            databaseService.saveChatMessage(chatMessage);
        });
    }

    @Async
    private void removeImageFile(String filename) {
        // delete the image file to conserve diskspace
        CompletableFuture.runAsync(() -> {
            imageService.deleteImageFile(filename);
        });
    }
}
