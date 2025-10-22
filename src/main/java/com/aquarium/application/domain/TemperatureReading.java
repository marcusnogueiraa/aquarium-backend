package com.aquarium.application.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

@Data @AllArgsConstructor
public class TemperatureReading {
    private String aquariumId;
    private Double value;
    private Instant timestamp;
}