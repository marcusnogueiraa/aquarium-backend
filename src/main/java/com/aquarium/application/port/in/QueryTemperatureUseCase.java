package com.aquarium.application.port.in;

import com.aquarium.application.domain.TemperatureReading;
import java.time.Instant;
import java.util.List;

public interface QueryTemperatureUseCase {
    List<TemperatureReading> getTemperatureReadings(String aquariumId, Instant start, Instant end);
}