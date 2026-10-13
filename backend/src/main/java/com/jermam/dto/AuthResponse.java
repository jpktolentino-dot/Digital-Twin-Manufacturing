package com.jermam.dto;

import com.jermam.enums.TipoUsuario;

public record AuthResponse(String token, String tipoToken, Long id, String nome, String loginEmail, TipoUsuario tipo) {}
