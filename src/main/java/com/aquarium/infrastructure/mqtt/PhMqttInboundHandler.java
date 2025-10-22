package com.aquarium.infrastructure.mqtt;

import com.aquarium.application.port.in.RecordPhUseCase;
import com.aquarium.application.port.in.RecordTemperatureUseCase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PhMqttInboundHandler {

    private final RecordPhUseCase recordPhUseCase;

    private Pattern topicPattern;

    @Value("${mqtt.topics.ph}")
    private String topicPatternConfig;

    @PostConstruct
    void initPattern() {
        String regex = "^" + topicPatternConfig.replace("+", "([^/]+)") + "$";
        topicPattern = Pattern.compile(regex);
    }

    @Bean
    @ServiceActivator(inputChannel = "phInputChannel")
    public MessageHandler phMessageHandler() {
        return (Message<?> message) -> {
            try {
                String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);

                Matcher matcher = topicPattern.matcher(topic);
                if (!matcher.matches()) {
                    return;
                }

                String aquariumId = matcher.group(1);
                String payload = asString(message.getPayload());

                double value = Double.parseDouble(payload);

                recordPhUseCase.recordPh(aquariumId, value);

            } catch (Exception e) {
                // TODO: fazer LOG depois
                System.out.println("Falha processando MQTT PH: " + e.getMessage());
            }
        };
    }

    private String asString(Object payload) {
        if (payload instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8).trim();
        }
        return String.valueOf(payload).trim();
    }
}
