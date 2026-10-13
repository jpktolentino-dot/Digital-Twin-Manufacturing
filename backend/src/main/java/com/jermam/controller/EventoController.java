package com.jermam.controller;

import com.jermam.dto.EventoRequest;
import com.jermam.dto.EventoResponse;
import com.jermam.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {
    private final EventoService service;
    @GetMapping public List<EventoResponse> listar(@RequestParam(defaultValue = "false") boolean ativos, @RequestParam(required = false) Long maquinaId) { return service.listar(ativos, maquinaId); }
    @GetMapping("/{id}") public EventoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MANUTENCAO')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public EventoResponse criar(@Valid @RequestBody EventoRequest request) { return service.criar(request); }
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MANUTENCAO')")
    @PatchMapping("/{id}/resolver") public EventoResponse resolver(@PathVariable Long id) { return service.resolver(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
