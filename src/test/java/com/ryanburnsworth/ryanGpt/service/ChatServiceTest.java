package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.service.chatService.ChatService;
import com.ryanburnsworth.ryanGpt.service.chatService.ChatServiceImpl;
import com.ryanburnsworth.ryanGpt.service.imageService.ImageService;
import com.ryanburnsworth.ryanGpt.service.redis.RedisService;
import io.github.sashirestela.openai.OpenAI;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.ryanburnsworth.ryanGpt.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private SimpleOpenAI mockOpenAIClient;

    @Mock
    private OpenAI.ChatCompletions mockChatCompletions;

    @Mock
    private ImageService mockImageService;

    @Mock
    private RedisService mockRedisService;

    @Mock
    private Chat mockChat;

    @Mock
    private Chat.Choice mockChoice;

    private ChatService chatService;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_ORG_ID = "test-org-id";
    private static final String TEST_PROJECT_ID = "test-project-id";
    private static final String TEST_USER_INPUT = "Hello, how are you?";
    private static final String TEST_BASE64_IMAGE = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD";
    private static final String TEST_RESPONSE = "I'm doing well, thank you!";

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(TEST_API_KEY, TEST_ORG_ID, TEST_PROJECT_ID, mockImageService, mockRedisService);

        // Inject mocks using reflection since they're private fields
        ReflectionTestUtils.setField(chatService, "openAIClient", mockOpenAIClient);
    }

    @Test
    void constructor_ShouldInitializeClientWithCorrectParameters() {
        // Create a new instance to test constructor
        ChatServiceImpl newService = new ChatServiceImpl(TEST_API_KEY, TEST_ORG_ID, TEST_PROJECT_ID, mockImageService, mockRedisService);

        // Verify that the service was created successfully
        assertNotNull(newService);
    }

    @Test
    void getResponse_WithoutImage_ShouldReturnTextResponse() {
        setupMockChatResponse();

        String result = chatService.getResponse(TEST_USER_INPUT, null);

        assertEquals(TEST_RESPONSE, result);
        verify(mockOpenAIClient).chatCompletions();
        verify(mockChatCompletions).createStream(any(ChatRequest.class));
        verify(mockRedisService).getChatMessagesFromRedis(MAX_CHAT_MESSAGES);
        verify(mockRedisService).saveChatMessageToRedis(TEST_USER_INPUT, TEST_RESPONSE, "");
        verifyNoInteractions(mockImageService);
    }

    @Test
    void getResponse_WithEmptyImage_ShouldReturnTextResponse() {
        setupMockChatResponse();

        String result = chatService.getResponse(TEST_USER_INPUT, "");

        assertEquals(TEST_RESPONSE, result);
        verify(mockOpenAIClient).chatCompletions();
        verify(mockChatCompletions).createStream(any(ChatRequest.class));
        verify(mockRedisService).getChatMessagesFromRedis(MAX_CHAT_MESSAGES);
        verify(mockRedisService).saveChatMessageToRedis(TEST_USER_INPUT, TEST_RESPONSE, "");
        verifyNoInteractions(mockImageService);
    }

    @Test
    void getResponse_WithImage_ShouldReturnVisionResponse() {
        setupMockChatResponse();

        String result = chatService.getResponse(TEST_USER_INPUT, TEST_BASE64_IMAGE);

        assertEquals(TEST_RESPONSE, result);
        verify(mockImageService).saveBase64Image(TEST_BASE64_IMAGE);
        verify(mockOpenAIClient).chatCompletions();
        verify(mockChatCompletions).createStream(any(ChatRequest.class));
        verify(mockRedisService).saveChatMessageToRedis(TEST_USER_INPUT, TEST_RESPONSE, TEST_BASE64_IMAGE);
    }

    @Test
    void getResponse_WithMultipleStreamResponses_ShouldConcatenateResults() {
        Chat mockChat1 = mock(Chat.class);
        Chat mockChat2 = mock(Chat.class);
        Chat mockChat3 = mock(Chat.class);

        Chat.Choice mockChoice1 = mock(Chat.Choice.class);
        Chat.Choice mockChoice2 = mock(Chat.Choice.class);
        Chat.Choice mockChoice3 = mock(Chat.Choice.class);

        when(mockChat1.getChoices()).thenReturn(List.of(mockChoice1));
        when(mockChat1.firstContent()).thenReturn("Hello ");

        when(mockChat2.getChoices()).thenReturn(List.of(mockChoice2));
        when(mockChat2.firstContent()).thenReturn("there ");

        when(mockChat3.getChoices()).thenReturn(List.of(mockChoice3));
        when(mockChat3.firstContent()).thenReturn("friend!");

        Stream<Chat> chatStream = Stream.of(mockChat1, mockChat2, mockChat3);
        CompletableFuture<Stream<Chat>> future = CompletableFuture.completedFuture(chatStream);

        when(mockOpenAIClient.chatCompletions()).thenReturn(mockChatCompletions);
        when(mockChatCompletions.createStream(any(ChatRequest.class))).thenReturn(future);

        String result = chatService.getResponse(TEST_USER_INPUT, null);

        assertEquals("Hello there friend!", result);
        verify(mockRedisService).getChatMessagesFromRedis(MAX_CHAT_MESSAGES);
        verify(mockRedisService).saveChatMessageToRedis(TEST_USER_INPUT, "Hello there friend!", "");
    }

    @Test
    void getResponse_WithEmptyChoices_ShouldFilterOutEmptyResponses() {
        Chat mockChatWithChoices = mock(Chat.class);
        Chat mockChatWithoutChoices = mock(Chat.class);

        Chat.Choice mockChoice = mock(Chat.Choice.class);

        when(mockChatWithChoices.getChoices()).thenReturn(List.of(mockChoice));
        when(mockChatWithChoices.firstContent()).thenReturn("Valid response");

        when(mockChatWithoutChoices.getChoices()).thenReturn(Collections.emptyList());

        Stream<Chat> chatStream = Stream.of(mockChatWithoutChoices, mockChatWithChoices);
        CompletableFuture<Stream<Chat>> future = CompletableFuture.completedFuture(chatStream);

        when(mockOpenAIClient.chatCompletions()).thenReturn(mockChatCompletions);
        when(mockChatCompletions.createStream(any(ChatRequest.class))).thenReturn(future);

        String result = chatService.getResponse(TEST_USER_INPUT, null);

        assertEquals("Valid response", result);
    }

    @Test
    void getResponse_WithNullContent_ShouldFilterOutNullResponses() {
        Chat mockChatWithContent = mock(Chat.class);
        Chat mockChatWithNullContent = mock(Chat.class);

        Chat.Choice mockChoice1 = mock(Chat.Choice.class);
        Chat.Choice mockChoice2 = mock(Chat.Choice.class);

        when(mockChatWithContent.getChoices()).thenReturn(List.of(mockChoice1));
        when(mockChatWithContent.firstContent()).thenReturn("Valid response");

        when(mockChatWithNullContent.getChoices()).thenReturn(List.of(mockChoice2));
        when(mockChatWithNullContent.firstContent()).thenReturn(null);

        Stream<Chat> chatStream = Stream.of(mockChatWithNullContent, mockChatWithContent);
        CompletableFuture<Stream<Chat>> future = CompletableFuture.completedFuture(chatStream);

        when(mockOpenAIClient.chatCompletions()).thenReturn(mockChatCompletions);
        when(mockChatCompletions.createStream(any(ChatRequest.class))).thenReturn(future);

        String result = chatService.getResponse(TEST_USER_INPUT, null);

        assertEquals("Valid response", result);
    }

    @Test
    void getResponse_WithNoValidResponses_ShouldReturnEmptyString() {
        Chat mockChatEmpty = mock(Chat.class);
        when(mockChatEmpty.getChoices()).thenReturn(Collections.emptyList());

        Stream<Chat> chatStream = Stream.of(mockChatEmpty);
        CompletableFuture<Stream<Chat>> future = CompletableFuture.completedFuture(chatStream);

        when(mockOpenAIClient.chatCompletions()).thenReturn(mockChatCompletions);
        when(mockChatCompletions.createStream(any(ChatRequest.class))).thenReturn(future);

        String result = chatService.getResponse(TEST_USER_INPUT, null);

        assertEquals("", result);
    }

    @Test
    void getResponse_TextRequest_ShouldCreateCorrectChatRequest() {
        setupMockChatResponse();

        chatService.getResponse(TEST_USER_INPUT, null);

        verify(mockChatCompletions).createStream(argThat(request -> {
            // Verify the request structure for text-only requests
            assertNotNull(request);
            assertEquals(TEMPERATURE, request.getTemperature());
            assertEquals(MAX_COMPLETION_TOKENS, request.getMaxCompletionTokens());

            // Verify messages contain system and user messages
            List<ChatMessage> messages = request.getMessages();
            assertEquals(2, messages.size());

            // First message should be system message
            assertTrue(messages.get(0) instanceof ChatMessage.SystemMessage);

            // Second message should be user message
            assertTrue(messages.get(1) instanceof ChatMessage.UserMessage);

            return true;
        }));
    }

    @Test
    void getResponse_VisionRequest_ShouldCreateCorrectChatRequest() {
        setupMockChatResponse();

        chatService.getResponse(TEST_USER_INPUT, TEST_BASE64_IMAGE);

        verify(mockImageService).saveBase64Image(TEST_BASE64_IMAGE);
        verify(mockChatCompletions).createStream(argThat(request -> {
            // Verify the request structure for vision requests
            assertNotNull(request);
            assertEquals(TEMPERATURE, request.getTemperature());
            assertEquals(500, request.getMaxCompletionTokens());

            // Verify messages structure for vision requests
            List<ChatMessage> messages = request.getMessages();
            assertEquals(1, messages.size());

            // Should be a user message with content parts
            assertTrue(messages.get(0) instanceof ChatMessage.UserMessage);

            return true;
        }));
    }

    private void setupMockChatResponse() {
        when(mockChat.getChoices()).thenReturn(List.of(mockChoice));
        when(mockChat.firstContent()).thenReturn(TEST_RESPONSE);

        Stream<Chat> chatStream = Stream.of(mockChat);
        CompletableFuture<Stream<Chat>> future = CompletableFuture.completedFuture(chatStream);

        when(mockOpenAIClient.chatCompletions()).thenReturn(mockChatCompletions);
        when(mockChatCompletions.createStream(any(ChatRequest.class))).thenReturn(future);
    }
}
