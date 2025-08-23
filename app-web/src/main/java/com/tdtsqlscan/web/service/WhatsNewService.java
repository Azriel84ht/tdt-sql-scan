package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.WhatsNew;
import com.tdtsqlscan.web.repository.WhatsNewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WhatsNewService {

    private final WhatsNewRepository whatsNewRepository;

    @Autowired
    public WhatsNewService(WhatsNewRepository whatsNewRepository) {
        this.whatsNewRepository = whatsNewRepository;
    }

    @Transactional(readOnly = true)
    public List<WhatsNew> findAll() {
        return whatsNewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<WhatsNew> findById(Long id) {
        return whatsNewRepository.findById(id);
    }

    @Transactional
    public WhatsNew save(WhatsNew whatsNew) {
        whatsNew.setUpdatedAt(LocalDateTime.now());
        return whatsNewRepository.save(whatsNew);
    }

    @Transactional
    public void deleteById(Long id) {
        whatsNewRepository.deleteById(id);
    }
}
