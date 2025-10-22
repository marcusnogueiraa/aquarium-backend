package com.aquarium.application.service;

import com.aquarium.application.domain.PhReading;
import com.aquarium.application.port.in.QueryPhUseCase;
import com.aquarium.application.port.in.RecordPhUseCase;
import com.aquarium.application.port.out.PhRepositoryPort;
import java.time.Instant;
import java.util.List;

public class PhService implements RecordPhUseCase, QueryPhUseCase {

    private final PhRepositoryPort phRepositoryPort;

    public PhService(PhRepositoryPort phRepositoryPort) {
        this.phRepositoryPort = phRepositoryPort;
    }

    @Override
    public void recordPh(String aquariumId, Double value) {
        PhReading reading = new PhReading(aquariumId, value, Instant.now());
        phRepositoryPort.save(reading);
    }

    @Override
    public List<PhReading> getPhReadings(String aquariumId, Instant start, Instant end) {
        return phRepositoryPort.findByAquariumIdAndTimestampBetween(aquariumId, start, end);
    }
}