package ru.ifmo.movies_app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootViewController {

    @GetMapping("/")
    public String redirectToMovies() {
        return "redirect:/movies";
    }
}
