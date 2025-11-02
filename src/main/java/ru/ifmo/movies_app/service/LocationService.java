package ru.ifmo.movies_app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.movies_app.domain.Location;
import ru.ifmo.movies_app.dto.LocationDto;
import ru.ifmo.movies_app.repository.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    @Transactional
    public Location create(LocationDto dto) {
        Location location = new Location();
        apply(dto, location);
        return locationRepository.save(location);
    }

    @Transactional
    public Location update(Long id, LocationDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location %d not found".formatted(id)));
        apply(dto, location);
        return locationRepository.save(location);
    }

    @Transactional
    public void delete(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location %d not found".formatted(id)));
        locationRepository.delete(location);
    }

    private void apply(LocationDto dto, Location location) {
        location.setName(dto.getName());
        location.setX(dto.getX());
        location.setY(dto.getY());
    }
}
