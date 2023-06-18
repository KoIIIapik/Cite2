package com.example.demo.controllers;


import com.example.demo.models.Post;
import com.example.demo.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public class MainContoller {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    public MainContoller(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        Iterable<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        model.addAttribute("title", "Главная страница");
        return "home";
    }

    @GetMapping("/add")
    public String homeAdd(Model model) {
        return "home-add";
    }

    @PostMapping("/add")
    public String blogPostAdd(@RequestParam("file") MultipartFile file, @RequestParam String title, @RequestParam String anons, @RequestParam String full_text, Model model) {
        try {
            Post post = new Post(title, anons, full_text);
            post.setImage_name(file.getOriginalFilename());
            post.setData(file.getBytes());
            postRepository.save(post);
            return "redirect:/";
        } catch (IOException e) {
            // Обработка ошибок загрузки изображения
            return "error";
        }
    }

    @GetMapping("/{id}/view")
    public String homeView(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)) {
            return "redirect:/";
        }

        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "home-view";
    }

    @GetMapping("/{id}/edit")
    public String homeEdit(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)) {
            return "redirect:/";
        }

        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "home-edit";
    }

    @PostMapping("/{id}/edit")
    public String blogPostEdit(@RequestParam String title, @PathVariable(value = "id") long id, @RequestParam String anons, @RequestParam String full_text,Model model) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);
        postRepository.save(post);
        return "redirect:/";
    }

    @PostMapping("/{id}/remove")
    public String blogPostDelete(@PathVariable(value = "id") long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow();
        postRepository.delete(post);
        return "redirect:/";
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file, @RequestParam String title, @RequestParam String anons, @RequestParam String full_text, Model model) {
        Post post = new Post(title, anons, full_text);
        postRepository.save(post);
        try {
            post.setImage_name(file.getOriginalFilename());
            post.setData(file.getBytes());
            postRepository.save(post);

            // Дополнительная обработка или представление модели

            return "redirect:/";
        } catch (IOException e) {
            // Обработка ошибок загрузки изображения
            return "error";
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<Post> imageOptional = postRepository.findById(id);

        if (imageOptional.isPresent()) {
            Post image = imageOptional.get();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
