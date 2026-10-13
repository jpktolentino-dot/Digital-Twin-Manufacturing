package com.jermam.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalMaquinas, long maquinasOperando, long maquinasParadas,
        long maquinasEmManutencao, long maquinasComFalha,
        long ordensEmProducao, long eventosAtivos,
        int producaoHoje, int pecasBoasHoje, int pecasRefugadasHoje,
        BigDecimal consumoEnergeticoHojeKwh,
        double disponibilidadePercentual, double performancePercentual,
        double qualidadePercentual, double oeePercentual,
        long tempoParadaHojeMinutos,
        List<DailyProductionResponse> producaoUltimosSeteDias,
        List<MaquinaResponse> maquinas
) {}
