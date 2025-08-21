package com.tdtsqlscan.web.controller;

import com.tdtsqlscan.web.domain.Post;
import com.tdtsqlscan.web.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.List;

@Controller
public class SitemapController {

    @Autowired
    private PostService postService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String getSitemap(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();

        StringWriter stringWriter = new StringWriter();
        stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        stringWriter.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // URLs estáticas
        addUrl(stringWriter, baseUrl, "/");
        addUrl(stringWriter, baseUrl, "/login");
        addUrl(stringWriter, baseUrl, "/register");
        addUrl(stringWriter, baseUrl, "/blog");

        // URLs dinámicas del blog
        List<Post> posts = postService.findAll();
        for (Post post : posts) {
            addUrl(stringWriter, baseUrl, "/blog/" + post.getSlug());
        }

        stringWriter.write("</urlset>");
        return stringWriter.toString();
    }

    private void addUrl(StringWriter sw, String baseUrl, String path) {
        sw.write("  <url>\n");
        sw.write("    <loc>" + baseUrl + path + "</loc>\n");
        sw.write("  </url>\n");
    }
}
