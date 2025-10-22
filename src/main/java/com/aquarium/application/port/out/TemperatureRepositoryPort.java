package com.aquarium.application.port.out;

import com.aquarium.application.domain.TemperatureReading;
import java.time.Instant;
import java.util.List;

public interface TemperatureRepositoryPort {
    void save(TemperatureReading reading);
    List<TemperatureReading> findByAquariumIdAndTimestampBetween(String aquariumId, Instant start, Instant end);
}