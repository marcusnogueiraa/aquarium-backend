# Aquarium Backend (Spring Boot • Hexagonal • MQTT • InfluxDB • STOMP)

> Coleta e serve **telemetria de aquários** (temperatura e pH), com histórico via REST e **tempo real** via WebSocket/STOMP.
> Arquitetura **Hexagonal (Ports & Adapters)**: domínio puro, adaptadores para HTTP, MQTT, InfluxDB e WebSocket.

## Sumário

* [Arquitetura](#arquitetura)
* [Stack](#stack)
* [Estrutura do projeto](#estrutura-do-projeto)
* [Configuração](#configuração)
* [Como rodar](#como-rodar)
* [MQTT (telemetria da ESP)](#mqtt-telemetria-da-esp)
* [REST (histórico e CRUD)](#rest-histórico-e-crud)
* [Tempo real (WebSocket/STOMP)](#tempo-real-websocketstomp)
* [Banco de dados de séries (InfluxDB)](#banco-de-dados-de-séries-influxdb)
* [Testes rápidos](#testes-rápidos)

---

## Arquitetura

**Hexagonal (Ports & Adapters)**

* **Domínio / Casos de Uso (application)**: não sabem de HTTP, MQTT, DB.
* **Ports IN**: `RecordTemperatureUseCase`, `RecordPhUseCase` — são **chamadas** pelos adapters de entrada (HTTP, MQTT).
* **Ports OUT**: `TemperatureRepositoryPort`, `PhRepositoryPort`, `RealTimeDataPort` — são **implementadas** pelos adapters de saída (InfluxDB, WebSocket).

Fluxo principal:

```
ESP → MQTT Broker
        ↓
[Mqtt Inbound Adapter] → recordTemperature/recordPh (Use Cases)
        ↓
RepositoryPort (InfluxDB Adapter) — persiste histórico
RealTimeDataPort (STOMP Adapter) — envia realtime aos clientes
```

---

## Stack

* **Java 17**, **Spring Boot 3.5.x**
* **Spring Web**, **Spring Integration MQTT**, **STOMP/WebSocket**
* **InfluxDB** (cliente: `influxdb-client-java`)
* **Jackson** (JSR-310 para `Instant`)
* **Lombok**, **Bean Validation**

---

## Estrutura do projeto

```
./src/main/java/com/aquarium/
├── application
│   ├── domain
│   │   ├── PhReading.java
│   │   └── TemperatureReading.java
│   ├── port
│   │   ├── in
│   │   │   ├── QueryPhUseCase.java
│   │   │   ├── QueryTemperatureUseCase.java
│   │   │   ├── RecordPhUseCase.java
│   │   │   └── RecordTemperatureUseCase.java
│   │   └── out
│   │       ├── PhRepositoryPort.java
│   │       ├── RealTimeDataPort.java
│   │       └── TemperatureRepositoryPort.java
│   └── service
│       ├── PhService.java
│       └── TemperatureService.java
├── AquariumApplication.java
└── infrastructure
    ├── configuration
    │   ├── ApplicationConfig.java
    │   ├── InfluxDBConfig.java
    │   ├── MqttConfig.java
    │   └── WebSocketConfig.java
    ├── influxdb
    │   ├── InfluxDbPhRepositoryAdapter.java
    │   └── InfluxDbTemperatureRepositoryAdapter.java
    ├── mqtt
    │   ├── PhMqttInboundHandler.java
    │   └── TemperatureMqttInboundHandler.java
    ├── rest
    │   ├── dto
    │   │   └── response
    │   │       ├── PhResponse.java
    │   │       └── TemperatureResponse.java
    │   ├── PhController.java
    │   └── TemperatureController.java
    └── stomp
        └── StompLiveUpdateAdapter.java
```

---

## Configuração

`src/main/resources/application.properties` (exemplo):

```properties
# MQTT
mqtt.brokerUri=tcp://localhost:1883
mqtt.clientId=aquarium-backend
mqtt.qos=1
mqtt.topics.temperature=aquarium/+/telemetry/temperature
mqtt.topics.ph=aquarium/+/telemetry/ph

# InfluxDB
influx.url=http://localhost:8086
influx.org=your-org
influx.bucket=aquarium
influx.token=your-token

# Server
server.port=8080
```

---

## Como rodar

### Pré-requisitos

* Java 17
* Mosquitto (MQTT) e InfluxDB rodando localmente

### Rodando a aplicação

```bash
mvn clean spring-boot:run
```

### (Opcional) Docker Compose para dev

`docker-compose.yml` (exemplo mínimo):

```yaml
version: '3.9'
services:
  mqtt:
    image: eclipse-mosquitto:2
    ports: ["1883:1883"]
    volumes:
      - ./mosquitto.conf:/mosquitto/config/mosquitto.conf

  influxdb:
    image: influxdb:2
    ports: ["8086:8086"]
    environment:
      - DOCKER_INFLUXDB_INIT_MODE=setup
      - DOCKER_INFLUXDB_INIT_USERNAME=admin
      - DOCKER_INFLUXDB_INIT_PASSWORD=admin123
      - DOCKER_INFLUXDB_INIT_ORG=your-org
      - DOCKER_INFLUXDB_INIT_BUCKET=aquarium
      - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=your-token
    volumes:
      - influxdb-data:/var/lib/influxdb2

volumes:
  influxdb-data:
```

`mosquitto.conf` mínimo:

```
listener 1883
allow_anonymous true
```

---

## MQTT (telemetria da ESP)

* **Tópicos**:

  * Temperatura (ESP → backend): `aquarium/{id}/telemetry/temperature`
  * pH (ESP → backend): `aquarium/{id}/telemetry/ph`
* **QoS**: 0 ou 1 (recomendado 1 em dev)
* **Payload**: **número puro** (ex.: `"26.7"`, `"7.21"`)

**Publicar (teste):**

```bash
# Temperatura
mosquitto_pub -h localhost -t 'aquarium/AQ-001/telemetry/temperature' -m '26.9' -q 1

# pH
mosquitto_pub -h localhost -t 'aquarium/AQ-001/telemetry/ph' -m '7.21' -q 1
```

---

## REST (histórico e CRUD)

**Temperatura — GET (histórico):**

```
GET /api/temperatures/{aquariumId}?start=2025-10-20T00:00:00Z&end=2025-10-20T23:59:59Z
```

* `start` e `end` são **Instant ISO-8601 UTC** (termina com `Z`), sem quebras de linha.

**Temperatura — POST (ESP/diagnóstico):**

```
POST /api/temperatures
Content-Type: application/json

{
  "aquariumId": "AQ-001",
  "value": 26.7
}
```

> pH segue o mesmo padrão quando o endpoint REST for exposto (`/api/ph/...`).

**Resposta (exemplo – `TemperatureResponse`)**

```json
[
  { "aquariumId":"AQ-001", "value":26.7, "timestamp":"2025-10-22T21:44:22Z" }
]
```

---

## Tempo real (WebSocket/STOMP)

**Endpoint WS**: `/ws`
**Broker interno**: prefixo `/topic`

**Destinos para assinar (cliente):**

* Temperatura atual: `/topic/aquarium/{id}/temperature`
* pH atual: `/topic/aquarium/{id}/ph`

**Payload enviado pelo backend (ex.)**

```json
{ "metric":"temperature", "value":26.9, "ts":"2025-10-22T21:55:10Z" }
```

### Teste rápido com HTML

Crie `stomp-test.html` e abra no navegador:

```html
<!doctype html>
<html>
<body>
  <h3>STOMP test</h3>
  <pre id="log"></pre>
  <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
  <script>
    const log = m => document.getElementById('log').textContent += m + '\n';
    const client = new StompJs.Client({
      brokerURL: "ws://localhost:8080/ws",
      reconnectDelay: 3000,
      debug: str => log(str),
      onConnect: () => {
        log('connected');
        client.subscribe('/topic/aquarium/AQ-001/temperature', msg => log('msg: ' + msg.body));
      }
    });
    client.activate();
  </script>
</body>
</html>
```

Depois publique via MQTT:

```bash
mosquitto_pub -h localhost -t 'aquarium/AQ-001/telemetry/temperature' -m '27.1' -q 1
```

---

## Banco de dados de séries (InfluxDB)

* **Bucket**: `aquarium`
* **Measurement**: `temperature` / `ph`
* **Tags**: `aquariumId`
* **Fields**: `value` (double)
* **Timestamp**: salvo pelo backend (`receivedAt = Instant.now()`)

Adapters `InfluxDBTemperatureRepositoryAdapter` e `InfluxDBPhRepositoryAdapter` implementam as **ports OUT** de repositório.

---

## Testes rápidos

* **Assinar tudo no MQTT (debug):**

  ```bash
  mosquitto_sub -h localhost -t 'aquarium/#' -v
  ```

* **Chamar endpoint REST (histórico):**

  ```bash
  curl "http://localhost:8080/api/temperatures/AQ-001?start=2025-10-20T00:00:00Z&end=2025-10-20T23:59:59Z"
  ```

* **Ver tempo real com STOMP:** usar `stomp-test.html` acima.
