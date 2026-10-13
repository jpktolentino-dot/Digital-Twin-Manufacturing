package com.jermam.controller;

import com.jermam.dto.SimulacaoConfigRequest;
import com.jermam.dto.SimulacaoConfigResponse;
import com.jermam.simulation.SimulacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/simulacao")
@RequiredArgsConstructor
public class SimulacaoController {
    private final SimulacaoService service;
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping public SimulacaoConfigResponse status() { return service.status(); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/iniciar") public SimulacaoConfigResponse iniciar() { return service.iniciar(); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/parar") public SimulacaoConfigResponse parar() { return service.parar(); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/configuracao") public SimulacaoConfigResponse configurar(@Valid @RequestBody SimulacaoConfigRequest request) { return service.configurar(request); }
}
