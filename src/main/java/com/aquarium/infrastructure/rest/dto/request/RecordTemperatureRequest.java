package com.aquarium.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordTemperatureRequest {
    @NotBlank(message = "O ID do aquário não pode ser vazio.")
    private String aquariumId;

    @NotNull(message = "O valor da temperatura não pode ser nulo.")
    private Double value;
}