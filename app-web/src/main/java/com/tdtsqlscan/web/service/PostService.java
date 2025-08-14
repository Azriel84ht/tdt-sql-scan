package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.Post;
import com.tdtsqlscan.web.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PostService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Post> findBySlug(String slug) {
        return postRepository.findBySlug(slug);
    }

    @Transactional
    public Post save(Post post) {
        if (post.getSlug() == null || post.getSlug().isEmpty()) {
            post.setSlug(generateSlug(post.getTitle()));
        }
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional
    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    private String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
