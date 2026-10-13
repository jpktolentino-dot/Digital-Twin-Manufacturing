package com.jermam.dto;

import java.time.LocalDate;

public record DailyProductionResponse(LocalDate data, int quantidadeProduzida, int quantidadeRefugada) {}
