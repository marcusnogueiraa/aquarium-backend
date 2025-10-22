package com.aquarium.infrastructure.rest;

import com.aquarium.application.port.in.RecordTemperatureUseCase;
import com.aquarium.infrastructure.rest.dto.request.RecordTemperatureRequest;
import com.aquarium.infrastructure.rest.dto.response.TemperatureResponse;
import com.aquarium.application.port.in.QueryTemperatureUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/temperatures")
@RequiredArgsConstructor
public class TemperatureController {

    private final RecordTemperatureUseCase recordTemperatureUseCase;
    private final QueryTemperatureUseCase queryTemperatureUseCase;

    @PostMapping
    public ResponseEntity<Void> recordTemperature(@RequestBody RecordTemperatureRequest request) {
        recordTemperatureUseCase.recordTemperature(request.getAquariumId(), request.getValue());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{aquariumId}")
    public ResponseEntity<List<TemperatureResponse>> getTemperatures(
            @PathVariable String aquariumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        List<TemperatureResponse> responses = queryTemperatureUseCase.getTemperatureReadings(aquariumId, start, end)
                .stream()
                .map(TemperatureResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}