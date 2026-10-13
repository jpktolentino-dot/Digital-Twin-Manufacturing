package com.jermam.controller;

import com.jermam.dto.SetorRequest;
import com.jermam.dto.SetorResponse;
import com.jermam.service.SetorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/setores")
@RequiredArgsConstructor
public class SetorController {
    private final SetorService service;
    @GetMapping public List<SetorResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public SetorResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public SetorResponse criar(@Valid @RequestBody SetorRequest request) { return service.criar(request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}") public SetorResponse atualizar(@PathVariable Long id, @Valid @RequestBody SetorRequest request) { return service.atualizar(id, request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
