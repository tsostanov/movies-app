package ru.ifmo.movies_app.web;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.ifmo.movies_app.domain.MovieGenre;

@Controller
@RequestMapping("/analytics")
public class AnalyticsViewController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("genres", Arrays.asList(MovieGenre.values()));
        return "analytics/index";
    }
}
