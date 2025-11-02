package ru.ifmo.movies_app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.ifmo.movies_app.service.LocationService;

@Controller
@RequestMapping("/locations")
public class LocationViewController {

    private final LocationService locationService;

    public LocationViewController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", locationService.getAll());
        return "locations/list";
    }
}
