package com.example.textsummarizer.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummarizerService {

    private static final Set<String> STOPWORDS = Set.of(
            "a","an","the","is","in","at","on","of","for","and","or","to","with",
            "this","that","it","as","are","was","were","be","been","by","from"
    );

    public String summarize(String text, String summarySize) {
        if (text == null || text.isEmpty()) return "";

        // Split text into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        int totalSentences = sentences.length;

        // DYNAMIC LENGTH CALCULATION
        int limit;
        switch (summarySize.toLowerCase()) {
            case "short":
                // 25% of text, minimum 2 sentences
                limit = Math.max(2, (int) Math.ceil(totalSentences * 0.25));
                break;
            case "long":
                // 75% of text, minimum 6 sentences
                limit = Math.max(6, (int) Math.ceil(totalSentences * 0.75));
                break;
            case "medium":
            default:
                // 50% of text, minimum 4 sentences
                limit = Math.max(4, (int) Math.ceil(totalSentences * 0.50));
                break;
        }

        // Ensure we don't ask for more sentences than exist
        if (limit > totalSentences) {
            limit = totalSentences;
            // Optimization: If they want 100% or more, just return the original
            if (limit == totalSentences) return text;
        }

        // Word frequency map
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\W+")) {
            if (!STOPWORDS.contains(word) && word.length() > 1) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        // Score sentences
        Map<String, Integer> sentenceScores = new HashMap<>();
        for (String sentence : sentences) {
            int score = 0;
            for (String w : sentence.toLowerCase().split("\\W+")) {
                score += wordFreq.getOrDefault(w, 0);
            }
            sentenceScores.put(sentence, score);
        }

        // Pick top 'limit' sentences
        List<String> topSentences = sentenceScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Highlight keywords
        List<String> highlighted = new ArrayList<>();
        for (String sentence : topSentences) {
            for (String word : wordFreq.keySet()) {
                if (wordFreq.get(word) > 1) {
                    sentence = sentence.replaceAll("(?i)\\b" + word + "\\b",
                            "<span class='keyword'>" + word + "</span>");
                }
            }
            highlighted.add(sentence);
        }

        return String.join(" ", highlighted);
    }
}