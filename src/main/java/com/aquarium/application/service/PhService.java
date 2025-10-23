package com.aquarium.application.service;

import com.aquarium.application.domain.PhReading;
import com.aquarium.application.port.in.QueryPhUseCase;
import com.aquarium.application.port.in.RecordPhUseCase;
import com.aquarium.application.port.out.RealTimeDataPort;
import com.aquarium.application.port.out.PhRepositoryPort;
import java.time.Instant;
import java.util.List;

public class PhService implements RecordPhUseCase, QueryPhUseCase {

    private final PhRepositoryPort phRepositoryPort;
    private final RealTimeDataPort realTimeDataPort;

    public PhService(PhRepositoryPort phRepositoryPort, RealTimeDataPort realTimeDataPort) {
        this.phRepositoryPort = phRepositoryPort;
        this.realTimeDataPort = realTimeDataPort;
    }

    @Override
    public void recordPh(String aquariumId, Double value) {
        PhReading reading = new PhReading(aquariumId, value, Instant.now());
        phRepositoryPort.save(reading);
        realTimeDataPort.publishPh(aquariumId, value);
    }

    @Override
    public List<PhReading> getPhReadings(String aquariumId, Instant start, Instant end) {
        return phRepositoryPort.findByAquariumIdAndTimestampBetween(aquariumId, start, end);
    }
}