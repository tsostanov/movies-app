package ru.ifmo.movies_app.web;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.ifmo.movies_app.domain.MovieGenre;
import ru.ifmo.movies_app.domain.MpaaRating;
import ru.ifmo.movies_app.dto.MovieTableFilter;
import ru.ifmo.movies_app.service.MovieService;
import ru.ifmo.movies_app.service.PersonService;

@Controller
@RequestMapping("/movies")
public class MovieViewController {

    private static final List<String> ALLOWED_SORTS = List.of(
            "name", "directorName", "screenwriterName", "operatorName", "genre", "mpaaRating");

    private static final Map<String, String> SORT_OPTION_LABELS = createSortOptions();

    private final MovieService movieService;
    private final PersonService personService;

    public MovieViewController(MovieService movieService,
                               PersonService personService) {
        this.movieService = movieService;
        this.personService = personService;
    }

    @GetMapping
    public String list(@ModelAttribute("filter") MovieTableFilter filter,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Principal principal,
                       Authentication authentication,
                       Model model) {
        model.addAttribute("genres", Arrays.asList(MovieGenre.values()));
        model.addAttribute("mpaaRatings", Arrays.asList(MpaaRating.values()));
        model.addAttribute("persons", personService.getAllSummaries());
        Pageable pageable = createPageRequest(filter, page, size);
        model.addAttribute("page", movieService.getMovies(filter, pageable));
        model.addAttribute("sortOptions", SORT_OPTION_LABELS.entrySet());
        model.addAttribute("sortDirections", List.of("asc", "desc"));
        String currentUsername = principal != null ? principal.getName() : "anonymous";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isAdmin", isAdmin);
        return "movies/list";
    }

    private Pageable createPageRequest(MovieTableFilter filter, int page, int size) {
        String sortBy = filter.getSortBy();
        if (!StringUtils.hasText(sortBy) || !ALLOWED_SORTS.contains(sortBy)) {
            return PageRequest.of(page, size);
        }
        String sortDirection = filter.getSortDirection();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private static Map<String, String> createSortOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("name", "Фильм");
        options.put("directorName", "Режиссёр");
        options.put("screenwriterName", "Сценарист");
        options.put("operatorName", "Оператор");
        options.put("genre", "Жанр");
        options.put("mpaaRating", "MPAA");
        return options;
    }
}
