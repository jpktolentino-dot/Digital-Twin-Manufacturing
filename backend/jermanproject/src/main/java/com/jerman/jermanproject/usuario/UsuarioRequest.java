package com.jerman.jermanproject.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank
        @Size(max = 100)
        String nome,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String senha,

        @Schema(
                description = "Perfil atribuído pelo administrador. Se omitido, será OPERADOR.",
                defaultValue = "OPERADOR"
        )
        TipoUsuario tipo
) {
    public UsuarioRequest {
        if (tipo == null) {
            tipo = TipoUsuario.OPERADOR;
        }
    }

    // Mantém compatibilidade com chamadas Java que ainda usam os três parâmetros.
    public UsuarioRequest(String nome, String email, String senha) {
        this(nome, email, senha, TipoUsuario.OPERADOR);
    }
}
