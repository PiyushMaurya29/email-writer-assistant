package com.email.writer.service;

import com.email.writer.dto.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailGeneratorService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.provider:groq}")
    private String aiProvider;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.model}")
    private String groqApiModel;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<String> generateEmailReply(EmailRequest request) {

        if ("gemini".equalsIgnoreCase(aiProvider)) {
            return generateWithGemini(request);
        }

        return generateWithGroq(request)
                .onErrorResume(error -> {
                    if (!(error instanceof IllegalStateException) && hasText(geminiApiKey)) {
                        return generateWithGemini(request);
                    }

                    return handleGenerationError("Groq", error);
                });
    }

    private Mono<String> generateWithGroq(EmailRequest request) {

        String apiKey = groqApiKey == null ? "" : groqApiKey.trim();

        if (apiKey.isBlank()) {
            return Mono.error(new IllegalStateException("Groq API key is missing. Set GROQ_API_KEY before starting the backend."));
        }

        String prompt = buildPrompt(request);

        WebClient webClient = webClientBuilder.build();

        Map<String, Object> requestBody = Map.of(
                "model", groqApiModel,
                "messages", new Object[]{
                        Map.of(
                                "role", "system",
                                "content", "You write concise, natural, professional email replies. Return only the email reply text."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                },
                "temperature", 0.4,
                "max_tokens", 256
        );

        return webClient.post()
                .uri(groqApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(apiKey))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractGroqResponse);
    }

    private Mono<String> generateWithGemini(EmailRequest request) {

        String apiKey = geminiApiKey == null ? "" : geminiApiKey.trim();

        if (apiKey.isBlank()) {
            return Mono.just("Gemini API key is missing. Set GEMINI_API_KEY before starting the backend.");
        }

        String prompt = buildPrompt(request);

        WebClient webClient = webClientBuilder.build();

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                },
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "maxOutputTokens", 256
                )
        );

        return webClient.post()
                .uri(geminiApiUrl + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractGeminiResponse)
                .onErrorResume(error -> handleGenerationError("Gemini", error));
    }

    private String buildPrompt(EmailRequest request) {

        String tone = request.getTone();

        if (tone == null || tone.isBlank()) {
            tone = "professional";
        }

        return """
                Generate a professional email reply.

                Tone: %s

                Original Email:
                %s

                Keep the reply concise, natural, and professional.
                """
                .formatted(
                        tone,
                        request.getEmailContent()
                );
    }

    private String extractGeminiResponse(String response) {

        try {

            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode candidatesNode = rootNode.path("candidates");

            if (!candidatesNode.isArray() || candidatesNode.isEmpty()) {
                return "No AI response generated.";
            }

            JsonNode partsNode = candidatesNode
                    .get(0)
                    .path("content")
                    .path("parts");

            if (!partsNode.isArray() || partsNode.isEmpty()) {
                return "No AI response generated.";
            }

            JsonNode textNode = partsNode.get(0).path("text");

            if (textNode.isMissingNode()) {
                return "No AI response generated.";
            }

            return textNode.asText();

        } catch (Exception exception) {

            exception.printStackTrace();

            return "Failed to parse AI response.";
        }
    }

    private String extractGroqResponse(String response) {

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode contentNode = rootNode
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return "No AI response generated.";
            }

            return contentNode.asText();
        } catch (Exception exception) {
            exception.printStackTrace();

            return "Failed to parse AI response.";
        }
    }

    private Mono<String> handleGenerationError(String provider, Throwable throwable) {

        if (throwable instanceof IllegalStateException exception) {
            return Mono.just(exception.getMessage());
        }

        if (throwable instanceof WebClientResponseException exception) {
            String detail = extractErrorDetail(exception.getResponseBodyAsString());

            if (exception.getStatusCode().value() == 404) {
                return Mono.just("Error generating AI reply: " + provider + " model was not found. " + detail);
            }

            if (exception.getStatusCode().value() == 429) {
                return Mono.just("Error generating AI reply: " + provider + " quota or rate limit reached. " + detail);
            }

            return Mono.just("Error generating AI reply: " + provider + " returned " + exception.getStatusCode() + ". " + detail);
        }

        return Mono.just("Error generating AI reply. Make sure the backend can reach " + provider + ".");
    }

    private String extractErrorDetail(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            return "No extra details returned by Gemini.";
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String message = rootNode.path("error").path("message").asText();

            if (!message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
            return responseBody.length() > 300 ? responseBody.substring(0, 300) : responseBody;
        }

        return "No extra details returned by Gemini.";
    }

    private boolean hasText(String value) {

        return value != null && !value.trim().isBlank();
    }
}
