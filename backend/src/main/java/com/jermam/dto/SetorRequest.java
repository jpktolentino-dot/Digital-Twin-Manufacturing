package com.jermam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorRequest(@NotBlank @Size(max = 100) String nome, @NotBlank @Size(max = 30) String codigo) {}
