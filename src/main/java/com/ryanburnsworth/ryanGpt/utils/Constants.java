package com.ryanburnsworth.ryanGpt.utils;

public class Constants {
    public final static String FILENAME = "file.jpg";
    public final static String GPT_MODEL = "gpt-4o-mini";
    public final static String CHAT_ENDPOINT = "/api/v1/chat";
    public final static String FILE_DIRECTORY = "src/main/resources/static";
    public final static String FILE_LOCATION = FILE_DIRECTORY + "/file.jpg";
    public final static String REDIS_CHAT_KEY = "chat_messages";
    public final static int MAX_CHAT_MESSAGES = 3;
    public final static double TEMPERATURE = 0.75;
    public final static int MAX_COMPLETION_TOKENS = 300;
}
