package ru.ifmo.movies_app.web;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.movies_app.domain.Location;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.service.LocationService;

@RestController
@RequestMapping("/api/locations")
public class LocationRestController {

    private final LocationService locationService;

    public LocationRestController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationDto> list() {
        return locationService.getAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto create(@Valid @RequestBody LocationDto dto) {
        Location created = locationService.create(dto);
        return toDto(created);
    }

    @PutMapping("/{id}")
    public LocationDto update(@PathVariable Long id, @Valid @RequestBody LocationDto dto) {
        Location updated = locationService.update(id, dto);
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }

    private LocationDto toDto(Location location) {
        return new LocationDto(location.getId(), location.getX(), location.getY(), location.getName());
    }
}
