package com.jerman.jermanproject.setor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
@RequiredArgsConstructor
@Tag(name = "Setores", description = "Gerenciamento de setores da fábrica")
public class SetorController {

    private final SetorService setorService;

    @GetMapping
    @Operation(summary = "Listar todos os setores")
    public ResponseEntity<List<SetorEntity>> listarTodos() {
        return ResponseEntity.ok(setorService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar setor por ID")
    public ResponseEntity<SetorEntity> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(setorService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar um setor")
    public ResponseEntity<SetorEntity> criar(
            @Valid @RequestBody SetorRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(setorService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um setor")
    public ResponseEntity<SetorEntity> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SetorRequest request) {
        return ResponseEntity.ok(
                setorService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um setor")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        setorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}