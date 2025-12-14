package ru.ifmo.movies_app.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.movies_app.service.importer.MovieImportService;
import ru.ifmo.movies_app.support.CacheStatsLoggingToggle;
import ru.ifmo.movies_app.support.ImportFailpointState;
import ru.ifmo.movies_app.support.ImportFailpointToggle;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final CacheStatsLoggingToggle cacheStatsLoggingToggle;
    private final MovieImportService movieImportService;
    private final ImportFailpointToggle importFailpointToggle;

    public AdminRestController(CacheStatsLoggingToggle cacheStatsLoggingToggle,
                               MovieImportService movieImportService,
                               ImportFailpointToggle importFailpointToggle) {
        this.cacheStatsLoggingToggle = cacheStatsLoggingToggle;
        this.movieImportService = movieImportService;
        this.importFailpointToggle = importFailpointToggle;
    }

    @PostMapping("/cache-stats-logging")
    public CacheLoggingResponse toggleCacheStatsLogging(@RequestParam("enabled") boolean enabled) {
        cacheStatsLoggingToggle.setEnabled(enabled);
        return new CacheLoggingResponse(cacheStatsLoggingToggle.isEnabled());
    }

    @PostMapping("/recover-imports")
    public RecoveryResponse recoverImports(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        int safeLimit = Math.max(limit, 1);
        int recovered = movieImportService.recoverPendingFileCommits(safeLimit);
        return new RecoveryResponse(recovered);
    }

    @PostMapping("/import-failpoint")
    public FailpointResponse setFailpoint(
            @RequestParam(value = "value", defaultValue = "NONE") ImportFailpointState state) {
        importFailpointToggle.setState(state);
        return new FailpointResponse(importFailpointToggle.getState());
    }

    public record CacheLoggingResponse(boolean enabled) {
    }

    public record RecoveryResponse(int recovered) {
    }

    public record FailpointResponse(ImportFailpointState state) {
    }
}
