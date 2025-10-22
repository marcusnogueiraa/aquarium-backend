package com.aquarium.infrastructure.influxdb;

import com.aquarium.application.domain.TemperatureReading;
import com.aquarium.application.port.out.TemperatureRepositoryPort;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InfluxDBTemperatureRepositoryAdapter implements TemperatureRepositoryPort {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    @Override
    public void save(TemperatureReading reading) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("temperature")
                .addTag("aquariumId", reading.getAquariumId())
                .addField("value", reading.getValue())
                .time(reading.getTimestamp(), WritePrecision.NS);

        writeApi.writePoint(bucket, org, point);
    }

    @Override
    public List<TemperatureReading> findByAquariumIdAndTimestampBetween(String aquariumId, Instant start, Instant end) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"temperature\") " +
                "|> filter(fn: (r) => r.aquariumId == \"%s\")",
                bucket, start.toString(), end.toString(), aquariumId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, org);
        List<TemperatureReading> readings = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                readings.add(new TemperatureReading(
                        aquariumId,
                        (Double) record.getValueByKey("_value"),
                        record.getTime()
                ));
            }
        }
        return readings;
    }
}