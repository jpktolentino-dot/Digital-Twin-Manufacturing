package com.jermam.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, BadCredentialsException.class})
    ResponseEntity<Map<String, Object>> requisicaoInvalida(RuntimeException ex) {
        String mensagem = ex instanceof BadCredentialsException ? "E-mail ou senha inválidos." : ex.getMessage();
        return resposta(HttpStatus.BAD_REQUEST, mensagem == null ? "Requisição inválida." : mensagem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> acessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para realizar esta operação.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> statusHttp(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return resposta(status, ex.getReason() == null ? "Requisição não autorizada." : ex.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> conflito(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Não foi possível concluir a operação porque o registro possui dados duplicados ou está relacionado a outros registros.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erro", "Dados inválidos.");
        body.put("detalhes", erros);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> erroInterno(Exception ex) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Verifique os logs da API.");
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("erro", mensagem);
        return ResponseEntity.status(status).body(body);
    }
}
