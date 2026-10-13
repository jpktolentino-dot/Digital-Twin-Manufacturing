package com.jermam.dto;

import com.jermam.enums.CategoriaEvento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoResponse(Long id, CategoriaEvento categoria, String descricao,
                             BigDecimal impactoEstimadoMinutos, LocalDateTime dataHora,
                             boolean resolvido, Long maquinaId, String maquinaNome,
                             Long ordemProducaoId, String numeroOrdem, String usuarioNome) {}
