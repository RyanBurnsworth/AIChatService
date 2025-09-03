# AI Chat Service
A GPT-4o-mini-powered chatbot that answers user questions about anything, including images. It maintains short-term context by recalling up to the three most recent messages in the conversation.

## Download and deploy using Docker Compose
Step 1: Clone the project `git clone https://github.com/RyanBurnsworth/AIChatService`  
Step 2: Navigate to the AIChatService root directory  
Step 3: Create ```/src/main/resources/applications.properties```  
Step 4: Add the following environment variables with your OpenAI API key and ids.  
```
openai.api-key: YOUR-OPENAI-API-KEY
openai.organization-id: YOUR-OPENAI-ORGANIZATION-ID
openai.project-id: YOUR-OPENAI-PROJECT-ID
```

Step 5: Deploy docker-compose.yml `docker-compose up -d`  

## API Usage:
**POST** `http://localhost:8080/api/v1/chat`.  

### Request Body

```json
{
  "userInput": "Your input to the chat bot",
  "base64Image": "idsfj0sdjfosidjfs0d=="  
}
```

### Response Body
**200 OK**  
```json
{
  "response": "The response from the chatbot"
}
```
