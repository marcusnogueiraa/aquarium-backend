package com.aquarium.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

import com.aquarium.application.domain.PhReading;

@Data
@AllArgsConstructor 
public class PhResponse {

    private String aquariumId;
    private Double value;
    private Instant timestamp;

    public static PhResponse fromDomain(PhReading domainObject) {
        return new PhResponse(
                domainObject.getAquariumId(),
                domainObject.getValue(),
                domainObject.getTimestamp()
        );
    }
}