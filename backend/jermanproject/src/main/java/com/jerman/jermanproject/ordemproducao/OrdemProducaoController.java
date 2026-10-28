
package com.jerman.jermanproject.ordemproducao;

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
@RequestMapping("/api/ordens-producao")
@RequiredArgsConstructor
@Tag(
        name = "Ordens de Produção",
        description = "Planejamento e gerenciamento da produção industrial"
)
public class OrdemProducaoController {

    private final OrdemProducaoService ordemProducaoService;

    @GetMapping
    @Operation(summary = "Listar todas as ordens de produção")
    public ResponseEntity<List<OrdemProducaoResponse>> listarTodas() {
        return ResponseEntity.ok(
                ordemProducaoService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ordem de produção por ID")
    public ResponseEntity<OrdemProducaoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemProducaoService.buscarPorId(id)
        );
    }

    @PostMapping
    @Operation(summary = "Criar ordem de produção")
    public ResponseEntity<OrdemProducaoResponse> criar(
            @Valid @RequestBody OrdemProducaoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ordemProducaoService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ordem de produção planejada")
    public ResponseEntity<OrdemProducaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrdemProducaoRequest request
    ) {
        return ResponseEntity.ok(
                ordemProducaoService.atualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estagio")
    @Operation(summary = "Alterar estágio da ordem de produção")
    public ResponseEntity<OrdemProducaoResponse> alterarEstagio(
            @PathVariable Long id,
            @Valid @RequestBody AlterarEstagioOrdemRequest request
    ) {
        return ResponseEntity.ok(
                ordemProducaoService.alterarEstagio(
                        id,
                        request.estagioAtual()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar ordem de produção")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        ordemProducaoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
