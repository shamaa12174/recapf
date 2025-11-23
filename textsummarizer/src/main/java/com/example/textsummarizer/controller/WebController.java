package com.example.textsummarizer.controller;

import com.example.textsummarizer.model.MCQ;
import com.example.textsummarizer.model.SummaryHistory;
import com.example.textsummarizer.model.User;
import com.example.textsummarizer.repository.SummaryHistoryRepository;
import com.example.textsummarizer.repository.UserRepository;
import com.example.textsummarizer.service.MCQService;
import com.example.textsummarizer.service.SummarizerService;
import com.example.textsummarizer.utils.DOCXUtils;
import com.example.textsummarizer.utils.PDFUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private SummarizerService summarizerService;
    @Autowired
    private MCQService mcqService;
    @Autowired
    private SummaryHistoryRepository historyRepository;
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private void saveHistory(String original, String summary) {
        User user = getCurrentUser();
        if (user != null) {
            SummaryHistory history = new SummaryHistory();
            history.setUser(user);
            history.setOriginalText(original.length() > 500 ? original.substring(0, 497) + "..." : original);
            history.setSummaryText(summary);
            history.setCreatedAt(LocalDateTime.now());
            historyRepository.save(history);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            return PDFUtils.extractText(file);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            return DOCXUtils.extractText(file);
        } else {
            return new String(file.getBytes());
        }
    }

    private void prepareToolPage(Model model, String activeMode) {
        User user = getCurrentUser();
        if (user != null) {
            List<SummaryHistory> history = historyRepository.findTop5ByUserOrderByCreatedAtDesc(user);
            model.addAttribute("history", history);
            model.addAttribute("username", user.getUsername());
        }
        model.addAttribute("mode", activeMode);
    }

    @GetMapping("/")
    public String landingPage(Model model) {
        User user = getCurrentUser();
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        return "landing";
    }

    @GetMapping("/summarizer")
    public String summarizerPage(Model model) {
        prepareToolPage(model, "summary");
        return "tool";
    }

    @GetMapping("/mcq")
    public String mcqPage(Model model) {
        prepareToolPage(model, "mcq");
        return "tool";
    }

    // --- Action Routes ---

    @PostMapping("/summarize")
    public String summarizeText(@RequestParam("text") String text,
                                @RequestParam(value = "size", defaultValue = "medium") String size,
                                Model model) {
        try {
            String summary = summarizerService.summarize(text, size);
            model.addAttribute("summary", summary);
            saveHistory(text, summary);
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }

        prepareToolPage(model, "summary");
        return "tool";
    }

    @PostMapping("/upload/summarize")
    public String summarizeFile(@RequestParam("file") MultipartFile file,
                                @RequestParam(value = "size", defaultValue = "medium") String size,
                                Model model) {
        try {
            String text = extractTextFromFile(file);
            String summary = summarizerService.summarize(text, size);
            model.addAttribute("summary", summary);
            saveHistory(text, summary);
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        prepareToolPage(model, "summary");
        return "tool";
    }

    @PostMapping("/mcq/text")
    public String generateMcqText(@RequestParam("text") String text, Model model) {
        List<MCQ> mcqs = mcqService.generateMCQ(text);
        model.addAttribute("mcqs", mcqs);
        prepareToolPage(model, "mcq");
        return "tool";
    }

    @PostMapping("/mcq/upload")
    public String generateMcqFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String text = extractTextFromFile(file);
            List<MCQ> mcqs = mcqService.generateMCQ(text);
            model.addAttribute("mcqs", mcqs);
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        prepareToolPage(model, "mcq");
        return "tool";
    }
}