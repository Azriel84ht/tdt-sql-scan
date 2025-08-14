package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private final PostService postService;

    @Autowired
    public BlogController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "blog/list";
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
