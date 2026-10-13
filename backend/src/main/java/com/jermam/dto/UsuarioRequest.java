package com.jermam.dto;

import com.jermam.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String loginEmail,
        @Size(min = 6, max = 100) String senha,
        @NotNull TipoUsuario tipo,
        Boolean ativo
) {}
