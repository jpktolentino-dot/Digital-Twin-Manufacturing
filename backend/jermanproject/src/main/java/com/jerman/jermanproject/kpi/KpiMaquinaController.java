
package com.jerman.jermanproject.kpi;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kpis/maquinas")
@RequiredArgsConstructor
@Tag(
        name = "KPIs de Máquinas",
        description = "Indicadores de operação e disponibilidade"
)
public class KpiMaquinaController {

    private final KpiMaquinaService kpiMaquinaService;

    @GetMapping("/{maquinaId}")
    @Operation(summary = "Calcular os indicadores de uma máquina por período")
    public ResponseEntity<KpiMaquinaResponse> calcularPorMaquina(
            @PathVariable Long maquinaId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fim
    ) {
        return ResponseEntity.ok(
                kpiMaquinaService.calcularPorMaquina(
                        maquinaId,
                        inicio,
                        fim
                )
        );
    }
}
