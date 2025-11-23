package com.example.textsummarizer.controller;

import com.example.textsummarizer.model.TextRequest;
import com.example.textsummarizer.model.TextSummary;
import com.example.textsummarizer.service.SummarizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summarizer")
public class SummarizerController {

    @Autowired
    private SummarizerService summarizerService;

    @PostMapping("/summarize")
    public ResponseEntity<TextSummary> summarize(@RequestBody TextRequest request) {
        // Default to "medium" if the user doesn't specify a size
        String size = (request.getSize() != null && !request.getSize().isEmpty())
                ? request.getSize()
                : "medium";

        // Call the service with the new String parameter
        String summary = summarizerService.summarize(request.getText(), size);

        return ResponseEntity.ok(new TextSummary(summary));
    }
}