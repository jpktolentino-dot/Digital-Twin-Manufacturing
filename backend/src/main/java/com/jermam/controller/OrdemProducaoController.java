package com.jermam.controller;

import com.jermam.dto.OrdemProducaoRequest;
import com.jermam.dto.OrdemProducaoResponse;
import com.jermam.dto.RegistroProducaoRequest;
import com.jermam.dto.RegistroProducaoResponse;
import com.jermam.enums.EstagioOrdemProducao;
import com.jermam.service.OrdemProducaoService;
import com.jermam.service.RegistroProducaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/ordens-producao")
@RequiredArgsConstructor
public class OrdemProducaoController {
    private final OrdemProducaoService service;
    private final RegistroProducaoService registroService;
    @GetMapping public List<OrdemProducaoResponse> listar(@RequestParam(required = false) EstagioOrdemProducao estagio) { return service.listar(estagio); }
    @GetMapping("/{id}") public OrdemProducaoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public OrdemProducaoResponse criar(@Valid @RequestBody OrdemProducaoRequest request) { return service.criar(request); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR')")
    @PutMapping("/{id}") public OrdemProducaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody OrdemProducaoRequest request) { return service.atualizar(id, request); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR', 'SUPERVISOR')")
    @PostMapping("/{id}/iniciar") public OrdemProducaoResponse iniciar(@PathVariable Long id) { return service.iniciar(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR', 'SUPERVISOR')")
    @PostMapping("/{id}/pausar") public OrdemProducaoResponse pausar(@PathVariable Long id) { return service.pausar(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR', 'SUPERVISOR')")
    @PostMapping("/{id}/concluir") public OrdemProducaoResponse concluir(@PathVariable Long id) { return service.concluir(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR')")
    @PostMapping("/{id}/cancelar") public OrdemProducaoResponse cancelar(@PathVariable Long id) { return service.cancelar(id); }
    @GetMapping("/{id}/registros") public List<RegistroProducaoResponse> registros(@PathVariable Long id) { return registroService.listar(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR', 'SUPERVISOR')")
    @PostMapping("/{id}/registros") @ResponseStatus(HttpStatus.CREATED) public RegistroProducaoResponse registrar(@PathVariable Long id, @Valid @RequestBody RegistroProducaoRequest request) { return registroService.registrar(id, request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
