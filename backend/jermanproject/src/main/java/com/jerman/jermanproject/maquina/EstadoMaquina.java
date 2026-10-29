
package com.jerman.jermanproject.maquina;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        enumAsRef = true,
        description = """
                Estados operacionais possíveis:
                OPERANDO: máquina em operação.
                PARADA: máquina parada.
                MANUTENCAO: máquina em manutenção.
                FALHA: máquina em estado de falha.
                """
)
public enum EstadoMaquina {
    OPERANDO,
    PARADA,
    MANUTENCAO,
    FALHA
}
