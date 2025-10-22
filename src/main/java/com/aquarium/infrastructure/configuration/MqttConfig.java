package com.aquarium.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(
            @Value("${mqtt.brokerUri}") String brokerUri,
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password
    ) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{ brokerUri });
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (!username.isBlank()) options.setUserName(username);
        if (!password.isBlank()) options.setPassword(password.toCharArray());

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel temperatureInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer temperatureInboundAdapter(
            MqttPahoClientFactory factory,
            @Value("${mqtt.clientId}") String clientId,
            @Value("${mqtt.topics.temperature}") String topic,
            @Value("${mqtt.qos:1}") int qos
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-temp-in",
                        factory,
                        topic
                );
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(temperatureInputChannel());
        return adapter;
    }
}
