package com.tdtsqlscan.web.repository;

import com.tdtsqlscan.web.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findAllByOrderByCreatedAtDesc();
}
