package com.example.textsummarizer.model;

import lombok.Data;
import java.util.List;

@Data
public class MCQ {
    private String question;
    private List<String> options;
    private String correctAnswer;
}
