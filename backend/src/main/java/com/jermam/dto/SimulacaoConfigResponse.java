package com.jermam.dto;

public record SimulacaoConfigResponse(boolean ativa, int intervaloSegundos, double probabilidadeFalha, String mensagem) {}
