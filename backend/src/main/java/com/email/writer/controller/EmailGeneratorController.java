package com.email.writer.controller;

import com.email.writer.dto.EmailRequest;
import com.email.writer.service.EmailGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    public Mono<String> generateReply(
            @Valid @RequestBody EmailRequest request
    ) {

        return emailGeneratorService.generateEmailReply(request);
    }
}
