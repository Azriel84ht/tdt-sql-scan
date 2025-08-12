package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.HomepageContent;
import com.tdtsqlscan.web.repository.HomepageContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    @Autowired
    private HomepageContentRepository contentRepository;

    public HomepageContent getContent() {
        return contentRepository.findById(1L).orElseGet(() -> {
            HomepageContent defaultContent = new HomepageContent();
            defaultContent.setId(1L);
            defaultContent.setHeroTitle("Understand Your Data Lineage");
            defaultContent.setHeroSubtitle("Our tool helps you visualize complex SQL scripts and data flows, turning convoluted code into clear, interactive diagrams. Untangle your ETL processes and gain clarity on your data's journey.");
            defaultContent.setNewsSection("");
            defaultContent.setPopupMessage("");
            defaultContent.setShowPopup(false);
            return contentRepository.save(defaultContent);
        });
    }

    public void saveContent(HomepageContent content) {
        content.setId(1L);
        contentRepository.save(content);
    }
}
