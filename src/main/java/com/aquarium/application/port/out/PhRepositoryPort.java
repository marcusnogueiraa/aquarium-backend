package com.aquarium.application.port.out;

import java.time.Instant;
import java.util.List;

import com.aquarium.application.domain.PhReading;

public interface PhRepositoryPort {
    void save(PhReading reading);
    List<PhReading> findByAquariumIdAndTimestampBetween(String aquariumId, Instant start, Instant end);
}
