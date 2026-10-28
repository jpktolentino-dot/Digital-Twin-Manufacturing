
package com.jerman.jermanproject.historicomaquina;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/historicos-maquinas")
@RequiredArgsConstructor
@Tag(
        name = "Histórico de Máquinas",
        description = "Consulta do histórico de estados das máquinas"
)
public class HistoricoMaquinaController {

    private final HistoricoMaquinaService historicoMaquinaService;

    @GetMapping
    @Operation(summary = "Listar todo o histórico de máquinas")
    public ResponseEntity<List<HistoricoMaquinaResponse>> listarTodos() {
        return ResponseEntity.ok(
                historicoMaquinaService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um registro de histórico por ID")
    public ResponseEntity<HistoricoMaquinaResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                historicoMaquinaService.buscarPorId(id)
        );
    }

    @GetMapping("/maquina/{maquinaId}")
    @Operation(summary = "Consultar o histórico de uma máquina")
    public ResponseEntity<List<HistoricoMaquinaResponse>>
            listarPorMaquina(@PathVariable Long maquinaId) {
        return ResponseEntity.ok(
                historicoMaquinaService.listarPorMaquina(maquinaId)
        );
    }
}
