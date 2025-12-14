package ru.ifmo.movies_app.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "locations", key = "#id")
    public Location getById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Локация %d не найдена".formatted(id)));
    }

    @Transactional
    @Caching(
            put = @CachePut(cacheNames = "locations", key = "#result.id", condition = "#result != null"),
            evict = {
                    @CacheEvict(cacheNames = "persons", allEntries = true),
                    @CacheEvict(cacheNames = "movies", allEntries = true)
            }
    )
    public Location create(LocationDto dto) {
        Location location = new Location();
        apply(dto, location);
        return locationRepository.save(location);
    }

    @Transactional
    @Caching(
            put = @CachePut(cacheNames = "locations", key = "#id"),
            evict = {
                    @CacheEvict(cacheNames = "persons", allEntries = true),
                    @CacheEvict(cacheNames = "movies", allEntries = true)
            }
    )
    public Location update(Long id, LocationDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Локация %d не найдена".formatted(id)));
        apply(dto, location);
        return locationRepository.save(location);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "locations", key = "#id"),
            @CacheEvict(cacheNames = "persons", allEntries = true),
            @CacheEvict(cacheNames = "movies", allEntries = true)
    })
    public void delete(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Локация %d не найдена".formatted(id)));
        locationRepository.delete(location);
    }

    private void apply(LocationDto dto, Location location) {
        location.setName(dto.getName());
        location.setX(dto.getX());
        location.setY(dto.getY());
    }
}
