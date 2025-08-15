package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.Post;
import com.tdtsqlscan.web.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
    private final PostService postService;

    @Autowired
    public BlogController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String listPosts(Model model) {
        try {
            logger.info("Finding all posts");
            List<Post> posts = postService.findAll();
            logger.info("Found {} posts", posts.size());
            model.addAttribute("posts", posts);
            return "blog/list";
        } catch (Exception e) {
            logger.error("Error finding posts", e);
            return "error/500";
        }
    }

    @GetMapping("/{slug}")
    public String viewPost(@PathVariable String slug, Model model) {
        return postService.findBySlug(slug)
                .map(post -> {
                    model.addAttribute("post", post);
                    return "blog/post";
                })
                .orElse("error/404"); // Or a more specific "post not found" page
    }
}
