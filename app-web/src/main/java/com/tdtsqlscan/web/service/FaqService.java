package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.Faq;
import com.tdtsqlscan.web.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    @Autowired
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public List<Faq> findAll() {
        return faqRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Faq> findById(Long id) {
        return faqRepository.findById(id);
    }

    @Transactional
    public Faq save(Faq faq) {
        faq.setUpdatedAt(LocalDateTime.now());
        return faqRepository.save(faq);
    }

    @Transactional
    public void deleteById(Long id) {
        faqRepository.deleteById(id);
    }
}
