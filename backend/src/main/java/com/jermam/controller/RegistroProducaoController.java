package com.jermam.controller;

import com.jermam.dto.RegistroProducaoResponse;
import com.jermam.service.RegistroProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registros-producao")
@RequiredArgsConstructor
public class RegistroProducaoController {
    private final RegistroProducaoService service;
    @GetMapping public List<RegistroProducaoResponse> listar(@RequestParam(required = false) Long ordemId) { return service.listar(ordemId); }
}
