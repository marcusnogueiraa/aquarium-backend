package com.aquarium.infrastructure.rest;

import com.aquarium.application.port.in.RecordTemperatureUseCase;
import com.aquarium.infrastructure.rest.dto.response.PhResponse;
import com.aquarium.infrastructure.rest.dto.response.TemperatureResponse;
import com.aquarium.application.port.in.QueryPhUseCase;
import com.aquarium.application.port.in.QueryTemperatureUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phs")
@RequiredArgsConstructor
public class PhController {

    private final QueryPhUseCase queryPhUseCase;

    @GetMapping("/{aquariumId}")
    public ResponseEntity<List<PhResponse>> getPhs(
            @PathVariable String aquariumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        List<PhResponse> responses = queryPhUseCase.getPhReadings(aquariumId, start, end)
                .stream()
                .map(PhResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}