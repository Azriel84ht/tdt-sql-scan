package com.tdtsqlscan.web.repository;

import com.tdtsqlscan.web.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
}
