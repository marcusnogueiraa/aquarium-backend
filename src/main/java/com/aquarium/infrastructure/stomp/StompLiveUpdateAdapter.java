package com.aquarium.infrastructure.stomp;

import java.time.Instant;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.aquarium.application.port.out.RealTimeDataPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompLiveUpdateAdapter implements RealTimeDataPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishTemperature(String aquariumId, double value) {
        var payload = new LiveValueDTO("temperature", value, Instant.now());
        messagingTemplate.convertAndSend("/topic/aquarium/" + aquariumId + "/temperature", payload);
    }

    @Override
    public void publishPh(String aquariumId, double value) {
        var payload = new LiveValueDTO("ph", value, Instant.now());
        messagingTemplate.convertAndSend("/topic/aquarium/" + aquariumId + "/ph", payload);
    }

    @lombok.Value
    static class LiveValueDTO {
        String metric;
        double value;
        Instant ts; 
    }
}
