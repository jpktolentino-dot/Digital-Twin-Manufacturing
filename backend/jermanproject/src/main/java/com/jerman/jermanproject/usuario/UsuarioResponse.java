package com.jerman.jermanproject.usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        TipoUsuario tipo,
        boolean ativo
) {}