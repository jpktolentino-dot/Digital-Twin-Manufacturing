package com.jermam.controller;

import com.jermam.dto.ProdutoRequest;
import com.jermam.dto.ProdutoResponse;
import com.jermam.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {
    private final ProdutoService service;
    @GetMapping public List<ProdutoResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public ProdutoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) { return service.criar(request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}") public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) { return service.atualizar(id, request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
