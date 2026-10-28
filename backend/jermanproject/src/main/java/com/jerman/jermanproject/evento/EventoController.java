
package com.jerman.jermanproject.evento;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Tag(
        name = "Eventos",
        description = "Registro e consulta de ocorrências industriais"
)
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @Operation(summary = "Listar eventos")
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public ResponseEntity<EventoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @GetMapping("/maquina/{maquinaId}")
    @Operation(summary = "Listar eventos de uma máquina")
    public ResponseEntity<List<EventoResponse>> listarPorMaquina(
            @PathVariable Long maquinaId
    ) {
        return ResponseEntity.ok(
                eventoService.listarPorMaquina(maquinaId)
        );
    }

    @GetMapping("/ordem-producao/{ordemProducaoId}")
    @Operation(summary = "Listar eventos de uma ordem de produção")
    public ResponseEntity<List<EventoResponse>> listarPorOrdemProducao(
            @PathVariable Long ordemProducaoId
    ) {
        return ResponseEntity.ok(
                eventoService.listarPorOrdemProducao(ordemProducaoId)
        );
    }

    @PostMapping
    @Operation(summary = "Registrar evento industrial")
    public ResponseEntity<EventoResponse> criar(
            @Valid @RequestBody EventoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventoService.criar(request));
    }
}
