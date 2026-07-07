# Email Writer Assistant

AI-powered Gmail reply assistant built with a Spring Boot backend and a Chrome Extension. The extension injects an **AI Reply** button into Gmail, sends the current email thread to the backend, and inserts a professional AI-generated response into the reply box.

## Features

- Gmail Chrome Extension with one-click reply generation
- Spring Boot WebFlux backend with reactive `WebClient`
- Groq Llama model integration as the primary AI provider
- Optional Gemini fallback provider
- Request validation and user-friendly error handling
- Environment-based API key configuration for safer GitHub publishing

## Tech Stack

- Java 17
- Spring Boot 3
- Spring WebFlux
- Maven
- Chrome Extension Manifest V3
- Groq API
- Optional Google Gemini API

## Project Structure

```text
email-writer-assistant/
  backend/      Spring Boot API
  extension/    Chrome Extension for Gmail
```

## Backend Setup

1. Open PowerShell in the backend folder:

```powershell
cd C:\Users\Maury\OneDrive\Desktop\email-writer-assistant\backend
```

2. Set your Groq API key:

```powershell
$env:GROQ_API_KEY="your_groq_api_key_here"
```

3. Run the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8081
```

## Chrome Extension Setup

1. Open Chrome and go to:

```text
chrome://extensions
```

2. Enable **Developer mode**.
3. Click **Load unpacked**.
4. Select the `extension` folder.
5. Open Gmail, open an email, click reply, then click **AI Reply**.

## Configuration

Main config file:

```text
backend/src/main/resources/application.properties
```

Default provider:

```properties
ai.provider=groq
```

Groq configuration:

```properties
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.api.model=llama-3.3-70b-versatile
groq.api.key=${GROQ_API_KEY:}
```

Optional Gemini fallback:

```properties
gemini.api.key=${GEMINI_API_KEY:}
```

## API Endpoint

```http
POST /api/email/generate
Content-Type: application/json
```

Request body:

```json
{
  "emailContent": "Hello, can we schedule a meeting tomorrow?",
  "tone": "professional"
}
```

Response:

```text
Sure, I would be happy to schedule a meeting tomorrow...
```

## Run Tests

```powershell
cd backend
.\mvnw.cmd test
```

## Resume Highlights

- Built a full-stack AI productivity tool integrating a Chrome Extension with a Spring Boot backend.
- Implemented secure API-key configuration using environment variables.
- Integrated an OpenAI-compatible LLM provider with reactive HTTP calls via Spring WebFlux.
- Designed provider switching to support Groq as primary AI provider and Gemini as optional fallback.
- Added validation, CORS configuration, error handling, and extension-side Gmail DOM integration.

## Security Notes

Do not commit real API keys. Use environment variables locally and keep `.env` files out of GitHub.
