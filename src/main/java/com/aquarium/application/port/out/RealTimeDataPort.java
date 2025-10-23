package com.aquarium.application.port.out;

public interface RealTimeDataPort {
    void publishTemperature(String aquariumId, double value);
    void publishPh(String aquariumId, double value);
}

