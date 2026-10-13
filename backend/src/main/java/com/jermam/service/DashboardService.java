package com.jermam.service;

import com.jermam.dto.DailyProductionResponse;
import com.jermam.dto.DashboardSummaryResponse;
import com.jermam.dto.MaquinaResponse;
import com.jermam.entity.HistoricoMaquina;
import com.jermam.entity.RegistroProducao;
import com.jermam.enums.EstadoMaquina;
import com.jermam.enums.EstagioOrdemProducao;
import com.jermam.repository.EventoRepository;
import com.jermam.repository.HistoricoMaquinaRepository;
import com.jermam.repository.MaquinaRepository;
import com.jermam.repository.OrdemProducaoRepository;
import com.jermam.repository.RegistroProducaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final MaquinaRepository maquinaRepository;
    private final OrdemProducaoRepository ordemRepository;
    private final RegistroProducaoRepository registroRepository;
    private final HistoricoMaquinaRepository historicoRepository;
    private final EventoRepository eventoRepository;
    private final MaquinaService maquinaService;

    public DashboardSummaryResponse resumo() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        List<RegistroProducao> registrosHoje = registroRepository.findByDataHoraBetween(inicioHoje, agora);
        int produzido = registrosHoje.stream().mapToInt(RegistroProducao::getQuantidadeProduzida).sum();
        int refugado = registrosHoje.stream().mapToInt(RegistroProducao::getQuantidadeRefugada).sum();
        BigDecimal energia = registrosHoje.stream().map(RegistroProducao::getConsumoEnergeticoKwh).reduce(BigDecimal.ZERO, BigDecimal::add);

        long segundosOperando = 0;
        long segundosTotal = 0;
        long minutosParada = 0;
        List<HistoricoMaquina> historicos = historicoRepository.findAll();
        for (HistoricoMaquina h : historicos) {
            LocalDateTime inicio = h.getDataHoraInicio().isBefore(inicioHoje) ? inicioHoje : h.getDataHoraInicio();
            LocalDateTime fimOriginal = h.getDataHoraFim() == null ? agora : h.getDataHoraFim();
            LocalDateTime fim = fimOriginal.isAfter(agora) ? agora : fimOriginal;
            if (!fim.isAfter(inicio)) continue;
            long segundos = Duration.between(inicio, fim).getSeconds();
            segundosTotal += segundos;
            if (h.getEstadoNovo() == EstadoMaquina.OPERANDO) segundosOperando += segundos;
            else minutosParada += segundos / 60;
        }

        long totalProduzidoReferencia = 0;
        for (RegistroProducao registro : registrosHoje) {
            totalProduzidoReferencia += (long) registro.getQuantidadeProduzida() * registro.getOrdemProducao().getProduto().getTempoCicloIdealSegundos();
        }
        double disponibilidade = segundosTotal == 0 ? 0 : 100.0 * segundosOperando / segundosTotal;
        double performance = segundosOperando == 0 ? 0 : Math.min(100.0, 100.0 * totalProduzidoReferencia / segundosOperando);
        double qualidade = produzido == 0 ? 0 : 100.0 * Math.max(0, produzido - refugado) / produzido;
        double oee = disponibilidade * performance * qualidade / 10000.0;

        return new DashboardSummaryResponse(
                maquinaRepository.count(),
                maquinaRepository.countByEstadoAtual(EstadoMaquina.OPERANDO),
                maquinaRepository.countByEstadoAtual(EstadoMaquina.PARADA),
                maquinaRepository.countByEstadoAtual(EstadoMaquina.MANUTENCAO),
                maquinaRepository.countByEstadoAtual(EstadoMaquina.FALHA),
                ordemRepository.findByEstagioAtualIn(List.of(EstagioOrdemProducao.EM_PRODUCAO)).size(),
                eventoRepository.countByResolvidoFalse(),
                produzido, produzido - refugado, refugado, energia,
                arredondar(disponibilidade), arredondar(performance), arredondar(qualidade), arredondar(oee),
                minutosParada, producaoUltimosSeteDias(agora), maquinaService.listar(null)
        );
    }

    private List<DailyProductionResponse> producaoUltimosSeteDias(LocalDateTime agora) {
        LocalDate hoje = agora.toLocalDate();
        LocalDate inicio = hoje.minusDays(6);
        List<RegistroProducao> registros = registroRepository.findByDataHoraBetween(inicio.atStartOfDay(), agora);
        Map<LocalDate, int[]> agrupados = new HashMap<>();
        for (RegistroProducao registro : registros) {
            LocalDate data = registro.getDataHora().toLocalDate();
            int[] contagem = agrupados.computeIfAbsent(data, ignorado -> new int[2]);
            contagem[0] += registro.getQuantidadeProduzida();
            contagem[1] += registro.getQuantidadeRefugada();
        }
        List<DailyProductionResponse> resultado = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate data = inicio.plusDays(i);
            int[] contagem = agrupados.getOrDefault(data, new int[2]);
            resultado.add(new DailyProductionResponse(data, contagem[0], contagem[1]));
        }
        return resultado;
    }

    private double arredondar(double valor) { return Math.round(valor * 10.0) / 10.0; }
}
