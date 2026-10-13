package com.jermam.dto;

import com.jermam.enums.EstadoMaquina;
import java.time.LocalDateTime;

public record MaquinaResponse(Long id, String nome, String codigo, String especificacoes,
                              EstadoMaquina estadoAtual, Long setorId, String setorNome,
                              Long operadorId, String operadorNome, LocalDateTime dataCadastro) {}
