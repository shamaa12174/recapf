package com.example.textsummarizer.repository;

import com.example.textsummarizer.model.SummaryHistory;
import com.example.textsummarizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SummaryHistoryRepository extends JpaRepository<SummaryHistory, Long> {
    // Fetch top 5 by user, ordered by date descending
    List<SummaryHistory> findTop5ByUserOrderByCreatedAtDesc(User user);
}