package com.jermam.controller;

import com.jermam.dto.UsuarioRequest;
import com.jermam.dto.UsuarioResponse;
import com.jermam.enums.TipoUsuario;
import com.jermam.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping public List<UsuarioResponse> listar(@RequestParam(required = false) TipoUsuario tipo) { return service.listar(tipo); }
    @GetMapping("/operadores") public List<UsuarioResponse> listarOperadores() { return service.listar(TipoUsuario.OPERADOR).stream().filter(UsuarioResponse::ativo).toList(); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/{id}") public UsuarioResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) { return service.criar(request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}") public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) { return service.atualizar(id, request); }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id) { service.excluir(id); }
}
