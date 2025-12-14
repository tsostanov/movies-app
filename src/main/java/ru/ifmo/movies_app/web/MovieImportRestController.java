package ru.ifmo.movies_app.web;

import java.security.Principal;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ru.ifmo.movies_app.dto.ImportOperationDto;
import ru.ifmo.movies_app.dto.PageResponse;
import ru.ifmo.movies_app.service.importer.MovieImportService;

@RestController
@RequestMapping("/api/movies/import")
public class MovieImportRestController {

    private final MovieImportService movieImportService;

    public MovieImportRestController(MovieImportService movieImportService) {
        this.movieImportService = movieImportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportOperationDto importMovies(@RequestParam("file") MultipartFile file,
                                           Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return movieImportService.importYaml(file, username);
    }

    @GetMapping("/history")
    public PageResponse<ImportOperationDto> history(@RequestParam(value = "all", defaultValue = "false") boolean all,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size,
                                                    Principal principal,
                                                    Authentication authentication) {
        boolean adminView = all && (authentication == null || authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        String username = principal != null ? principal.getName() : null;
        return movieImportService.getHistory(username, adminView, page, size);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<InputStreamResource> download(@PathVariable("id") Long id) {
        var file = movieImportService.loadImportFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(new InputStreamResource(file.stream()));
    }
}
