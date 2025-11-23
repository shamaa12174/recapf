package com.example.textsummarizer.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class DOCXUtils {

    public static String extractText(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining(" "));
        }
    }
}
