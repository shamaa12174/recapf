package com.example.textsummarizer.model;

public class TextRequest {
    private String text;
    private String size; // Changed from 'int limit' to 'String size'

    public TextRequest() {
    }

    public TextRequest(String text, String size) {
        this.text = text;
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}