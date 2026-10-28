
package com.jerman.jermanproject.maquina;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/maquinas")
@RequiredArgsConstructor
@Tag(name = "Máquinas", description = "Gerenciamento das máquinas da fábrica")
public class MaquinaController {

    private final MaquinaService maquinaService;

    @GetMapping
    @Operation(summary = "Listar todas as máquinas")
    public ResponseEntity<List<MaquinaEntity>> listarTodas() {
        return ResponseEntity.ok(maquinaService.listarTodas());
    }

    @GetMapping("/setor/{setorId}")
    @Operation(summary = "Listar máquinas de um setor")
    public ResponseEntity<List<MaquinaEntity>> listarPorSetor(
            @PathVariable Long setorId
    ) {
        return ResponseEntity.ok(
                maquinaService.listarPorSetor(setorId)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar máquina por ID")
    public ResponseEntity<MaquinaEntity> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(maquinaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar máquina")
    public ResponseEntity<MaquinaEntity> criar(
            @Valid @RequestBody MaquinaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(maquinaService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar máquina")
    public ResponseEntity<MaquinaEntity> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaquinaRequest request
    ) {
        return ResponseEntity.ok(
                maquinaService.atualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Alterar o estado operacional de uma máquina")
    public ResponseEntity<MaquinaEntity> alterarEstado(
            @PathVariable Long id,
            @Valid @RequestBody AlterarEstadoMaquinaRequest request
    ) {
        return ResponseEntity.ok(
                maquinaService.alterarEstado(
                        id,
                        request.estadoAtual()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar máquina")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id
    ) {
        maquinaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Reativar máquina")
    public ResponseEntity<Void> ativar(
            @PathVariable Long id
    ) {
        maquinaService.ativar(id);
        return ResponseEntity.noContent().build();
    }
}
