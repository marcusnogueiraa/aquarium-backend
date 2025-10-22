package com.aquarium.infrastructure.rest.dto.response;

import com.aquarium.application.domain.TemperatureReading;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor 
public class TemperatureResponse {

    private String aquariumId;
    private Double value;
    private Instant timestamp;

    public static TemperatureResponse fromDomain(TemperatureReading domainObject) {
        return new TemperatureResponse(
                domainObject.getAquariumId(),
                domainObject.getValue(),
                domainObject.getTimestamp()
        );
    }
}