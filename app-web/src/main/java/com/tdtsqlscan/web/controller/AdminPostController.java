package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.Post;
import com.tdtsqlscan.web.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final PostService postService;

    @Value("${tinymce.api.key}")
    private String tinymceApiKey;

    @Autowired
    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "admin/posts/list"; // Thymeleaf template path
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("tinymceApiKey", tinymceApiKey);
        return "admin/posts/form"; // Thymeleaf template path
    }

    @PostMapping
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        post.setAuthor(currentUserName);
        postService.save(post);
        redirectAttributes.addFlashAttribute("successMessage", "Post created successfully!");
        return "redirect:/admin/posts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return postService.findById(id)
                .map(post -> {
                    model.addAttribute("post", post);
                    model.addAttribute("tinymceApiKey", tinymceApiKey);
                    return "admin/posts/form"; // Thymeleaf template path
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Post not found!");
                    return "redirect:/admin/posts";
                });
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        return postService.findById(id)
                .map(existingPost -> {
                    existingPost.setTitle(post.getTitle());
                    existingPost.setContent(post.getContent());
                    // Regenerate slug if title changes
                    existingPost.setSlug(null); // Service will regenerate it
                    existingPost.setUpdatedAt(LocalDateTime.now());
                    postService.save(existingPost);
                    redirectAttributes.addFlashAttribute("successMessage", "Post updated successfully!");
                    return "redirect:/admin/posts";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Post not found!");
                    return "redirect:/admin/posts";
                });
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        postService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Post deleted successfully!");
        return "redirect:/admin/posts";
    }
}
