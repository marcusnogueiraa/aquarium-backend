package com.aquarium.application.port.in;

import com.aquarium.application.domain.PhReading;
import java.time.Instant;
import java.util.List;

public interface QueryPhUseCase {
    List<PhReading> getPhReadings(String aquariumId, Instant start, Instant end);
}