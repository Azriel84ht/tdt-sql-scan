package com.tdtsqlscan.web.repository;

import com.tdtsqlscan.web.domain.WhatsNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsNewRepository extends JpaRepository<WhatsNew, Long> {

    List<WhatsNew> findAllByOrderByCreatedAtDesc();
}
