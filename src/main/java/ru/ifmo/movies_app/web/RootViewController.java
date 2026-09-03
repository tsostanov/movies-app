package ru.ifmo.movies_app.web;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.ifmo.movies_app.domain.ImportOperation;
import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.repository.ImportOperationRepository;
import ru.ifmo.movies_app.service.LocationService;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

@Controller
public class RootViewController {

    private final MovieService movieService;
    private final PersonService personService;
    private final LocationService locationService;
    private final ImportOperationRepository importOperationRepository;

    public RootViewController(MovieService movieService,
                              PersonService personService,
                              LocationService locationService,
                              ImportOperationRepository importOperationRepository) {
        this.movieService = movieService;
        this.personService = personService;
        this.locationService = locationService;
        this.importOperationRepository = importOperationRepository;
    }

    @GetMapping("/")
    public String index(Principal principal, Authentication authentication, Model model) {
        String username = principal != null ? principal.getName() : "anonymous";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        long importCount = isAdmin
                ? importOperationRepository.countAll()
                : importOperationRepository.countByUsername(username);
        List<ImportOperation> recentImports = isAdmin
                ? importOperationRepository.findAll(0, 5)
                : importOperationRepository.findByUsername(username, 0, 5);

        model.addAttribute("currentUsername", username);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("movieCount", movieService.getMovies(new MovieTableFilter(), PageRequest.of(0, 1))
                .getTotalElements());
        model.addAttribute("personCount", personService.getAllSummaries().size());
        model.addAttribute("locationCount", locationService.getAll().size());
        model.addAttribute("importCount", importCount);
        model.addAttribute("recentImports", recentImports);
        return "home/index";
    }
}
