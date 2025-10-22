package com.aquarium.application.port.in;

public interface RecordTemperatureUseCase {
    void recordTemperature(String aquariumId, Double value);
}
