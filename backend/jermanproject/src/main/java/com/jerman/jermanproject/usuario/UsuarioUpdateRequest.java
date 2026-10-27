package com.jerman.jermanproject.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(

        @NotBlank
        @Size(max = 100)
        String nome,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Pattern(
            regexp = "^$|^.{8,72}$",
            message = "A senha deve ter entre 8 e 72 caracteres."
        )
        String senha,

        TipoUsuario tipoUsuario,

        @NotNull
        Boolean ativo
) {}