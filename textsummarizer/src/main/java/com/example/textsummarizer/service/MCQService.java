package com.example.textsummarizer.service;

import com.example.textsummarizer.model.MCQ;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MCQService {

    public List<MCQ> generateMCQ(String text) {
        List<MCQ> mcqs = new ArrayList<>();
        if (text == null || text.isEmpty()) return mcqs;

        // 1. Split text into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");

        // 2. Create a pool of words for "wrong answers" (distractors)
        List<String> wordPool = Arrays.stream(text.split("\\W+"))
                .filter(w -> w.length() > 3)
                .distinct()
                .collect(Collectors.toList());

        Random random = new Random();
        int validSentenceCount = 0; // Counter to track valid sentences

        // 3. Iterate through sentences
        for (String sentence : sentences) {
            // Ignore very short sentences
            if (sentence.split(" ").length < 4) continue;

            validSentenceCount++;

            // CHECK: Only generate a question for every 2nd valid sentence
            // This logic picks #1, skips #2, picks #3, skips #4...
            if (validSentenceCount % 2 == 0) {
                continue;
            }

            String[] words = sentence.split(" ");
            String keyword = "";

            // Find a suitable keyword (longest word > 3 chars)
            for (String w : words) {
                String cleanWord = w.replaceAll("[^a-zA-Z]", "");
                if (cleanWord.length() > 3 && cleanWord.length() > keyword.length()) {
                    keyword = cleanWord;
                }
            }

            if (!keyword.isEmpty()) {
                MCQ mcq = new MCQ();
                mcq.setQuestion(sentence.replace(keyword, "_______"));
                mcq.setCorrectAnswer(keyword);

                List<String> options = new ArrayList<>();
                options.add(keyword);

                // Add 3 random wrong answers
                int attempts = 0;
                while (options.size() < 4 && attempts < 50) {
                    String distractor = wordPool.get(random.nextInt(wordPool.size()));
                    if (!options.contains(distractor) && !distractor.equalsIgnoreCase(keyword)) {
                        options.add(distractor);
                    }
                    attempts++;
                }

                if (options.size() < 4) continue;

                Collections.shuffle(options);
                mcq.setOptions(options);
                mcqs.add(mcq);
            }
        }

        return mcqs;
    }
}