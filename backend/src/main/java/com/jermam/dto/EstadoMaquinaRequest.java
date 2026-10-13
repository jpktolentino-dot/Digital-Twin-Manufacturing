package com.jermam.dto;

import com.jermam.enums.EstadoMaquina;
import jakarta.validation.constraints.NotNull;

public record EstadoMaquinaRequest(@NotNull EstadoMaquina estado) {}
