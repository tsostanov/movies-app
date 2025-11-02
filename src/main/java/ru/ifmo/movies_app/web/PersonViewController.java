package ru.ifmo.movies_app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.ifmo.movies_app.domain.Color;
import ru.ifmo.movies_app.domain.Country;
import ru.ifmo.movies_app.service.PersonService;
import ru.ifmo.movies_app.service.LocationService;

@Controller
@RequestMapping("/persons")
public class PersonViewController {

    private final PersonService personService;
    private final LocationService locationService;

    public PersonViewController(PersonService personService,
                                LocationService locationService) {
        this.personService = personService;
        this.locationService = locationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", personService.getAllPersons());
        model.addAttribute("persons", personService.getAllSummaries());
        model.addAttribute("colors", Color.values());
        model.addAttribute("countries", Country.values());
        model.addAttribute("locations", locationService.getAll());
        return "persons/list";
    }
}
