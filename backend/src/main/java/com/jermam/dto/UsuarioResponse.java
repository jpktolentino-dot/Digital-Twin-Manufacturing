package com.jermam.dto;

import com.jermam.enums.TipoUsuario;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nome, String loginEmail, LocalDateTime dataCadastro, boolean ativo, TipoUsuario tipo) {}
