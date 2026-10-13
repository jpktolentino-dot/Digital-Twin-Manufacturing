package com.jermam.controller;

import com.jermam.dto.EstadoMaquinaRequest;
import com.jermam.dto.HistoricoMaquinaResponse;
import com.jermam.dto.MaquinaRequest;
import com.jermam.dto.MaquinaResponse;
import com.jermam.service.HistoricoMaquinaService;
import com.jermam.service.MaquinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/maquinas")
@RequiredArgsConstructor
public class MaquinaController {
    private final MaquinaService service;
    private final HistoricoMaquinaService historicoService;
    @GetMapping public List<MaquinaResponse> listar(@RequestParam(required = false) Long setorId) { return service.listar(setorId); }
    @GetMapping("/{id}") public MaquinaResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MaquinaResponse criar(@Valid @RequestBody MaquinaRequest request) { return service.criar(request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}") public MaquinaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MaquinaRequest request) { return service.atualizar(id, request); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR', 'MANUTENCAO')")
    @PatchMapping("/{id}/estado") public MaquinaResponse alterarEstado(@PathVariable Long id, @Valid @RequestBody EstadoMaquinaRequest request) { return service.alterarEstado(id, request.estado()); }
    @GetMapping("/{id}/historico") public List<HistoricoMaquinaResponse> historico(@PathVariable Long id) { return historicoService.listar(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
