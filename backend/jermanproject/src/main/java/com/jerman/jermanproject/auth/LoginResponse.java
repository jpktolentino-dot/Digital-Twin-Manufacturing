package com.jerman.jermanproject.auth;

import com.jerman.jermanproject.usuario.TipoUsuario;

public record LoginResponse(
        String token,
        String tipoToken,
        long expiresIn,
        Long usuarioId,
        String nome,
        String email,
        TipoUsuario tipo
) {}