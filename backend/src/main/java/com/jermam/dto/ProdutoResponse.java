package com.jermam.dto;

public record ProdutoResponse(Long id, String nome, String codigo, String especificacoes, Integer tempoCicloIdealSegundos) {}
