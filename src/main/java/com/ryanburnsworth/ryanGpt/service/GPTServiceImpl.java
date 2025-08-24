package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.utils.ImageUtil;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.support.Base64Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ryanburnsworth.ryanGpt.utils.Constants;
import com.ryanburnsworth.ryanGpt.utils.Prompts;

import java.util.List;
import java.util.stream.Stream;

@Service
public class GPTServiceImpl implements GPTService {
    private final SimpleOpenAI openAIClient;
    private final ImageUtil imageUtil;

    public GPTServiceImpl(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.organization-id}") String orgId,
            @Value("${openai.project-id}") String projectId
    ) {
        this.openAIClient = SimpleOpenAI.builder()
                .apiKey(apiKey)
                .organizationId(orgId)
                .projectId(projectId)
                .build();

        imageUtil = new ImageUtil();
    }

    @Override
    public String getResponse(String userInput, String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            ChatRequest chatRequest = getChatCompletionUsingVisionRequest(userInput, base64Image);
            return getChatCompletion(chatRequest);
        }

        ChatRequest chatRequest = getChatCompletionRequest(userInput);
        return getChatCompletion(chatRequest);
    }

    private String getChatCompletion(ChatRequest chatRequest) {
        Stream<Chat> chatResponse = openAIClient.chatCompletions().createStream(chatRequest).join();

        StringBuilder sb = new StringBuilder();

        chatResponse
                .filter(chatResp -> !chatResp.getChoices().isEmpty() && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(sb::append);

        return sb.toString();
    }

    private ChatRequest getChatCompletionRequest(String userInput) {
        return ChatRequest.builder()
                .model(Constants.GPT_MODEL)
                .message(ChatMessage.SystemMessage.of(Prompts.chatCompletionPrompt))
                .message(ChatMessage.UserMessage.of(userInput))
                .temperature(0.75)
                .maxCompletionTokens(300)
                .build();
    }

    private ChatRequest getChatCompletionUsingVisionRequest(String userInput, String base64Image) {
        // store the image file locally in the resources/static folder as file.jpg
        imageUtil.saveBase64Image(base64Image);

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
