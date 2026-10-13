package com.jermam.dto;

import com.jermam.enums.EstadoMaquina;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaquinaRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 4000) String especificacoes,
        EstadoMaquina estadoAtual,
        @NotNull Long setorId,
        @NotNull Long operadorId
) {}
