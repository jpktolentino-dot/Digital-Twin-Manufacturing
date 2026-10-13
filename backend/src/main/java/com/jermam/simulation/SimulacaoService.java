package com.jermam.simulation;

import com.jermam.dto.RegistroProducaoRequest;
import com.jermam.dto.SimulacaoConfigRequest;
import com.jermam.dto.SimulacaoConfigResponse;
import com.jermam.entity.Maquina;
import com.jermam.entity.OrdemProducao;
import com.jermam.enums.CategoriaEvento;
import com.jermam.enums.EstadoMaquina;
import com.jermam.enums.EstagioOrdemProducao;
import com.jermam.repository.MaquinaRepository;
import com.jermam.repository.OrdemProducaoRepository;
import com.jermam.service.EventoService;
import com.jermam.service.MaquinaService;
import com.jermam.service.RegistroProducaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulacaoService {
    private final MaquinaRepository maquinaRepository;
    private final OrdemProducaoRepository ordemRepository;
    private final MaquinaService maquinaService;
    private final RegistroProducaoService registroService;
    private final EventoService eventoService;

    private volatile boolean ativa = false;
    private volatile int intervaloSegundos = 15;
    private volatile double probabilidadeFalha = 0.03;
    private volatile long ultimaExecucaoMillis = 0;

    public SimulacaoConfigResponse status() {
        return new SimulacaoConfigResponse(ativa, intervaloSegundos, probabilidadeFalha,
                ativa ? "Simulação ativa: estados e apontamentos são gerados automaticamente." : "Simulação pausada.");
    }

    public SimulacaoConfigResponse iniciar() { ativa = true; ultimaExecucaoMillis = 0; return status(); }
    public SimulacaoConfigResponse parar() { ativa = false; return status(); }

    public SimulacaoConfigResponse configurar(SimulacaoConfigRequest request) {
        intervaloSegundos = request.intervaloSegundos();
        probabilidadeFalha = request.probabilidadeFalha();
        return status();
    }

    @Scheduled(fixedDelay = 1000)
    public void executarCicloSeNecessario() {
        if (!ativa) return;
        long agora = System.currentTimeMillis();
        if (agora - ultimaExecucaoMillis < intervaloSegundos * 1000L) return;
        ultimaExecucaoMillis = agora;
        try { executarCiclo(); }
        catch (Exception ex) { log.warn("Falha ao executar um ciclo da simulação: {}", ex.getMessage()); }
    }

    @Transactional
    public void executarCiclo() {
        List<Maquina> maquinas = maquinaRepository.findAll();
        for (Maquina maquina : maquinas) {
            double sorteio = ThreadLocalRandom.current().nextDouble();
            List<OrdemProducao> ativas = ordemRepository.findByMaquinaIdAndEstagioAtualIn(maquina.getId(), List.of(EstagioOrdemProducao.EM_PRODUCAO));
            OrdemProducao ordemAtiva = ativas.isEmpty() ? null : ativas.get(0);
            if (maquina.getEstadoAtual() == EstadoMaquina.OPERANDO) {
                if (sorteio < probabilidadeFalha) {
                    maquinaService.alterarEstado(maquina.getId(), EstadoMaquina.FALHA);
                    eventoService.registrarSimulado(CategoriaEvento.FALHA, "Falha simulada no equipamento " + maquina.getCodigo(), maquina.getId(), ordemAtiva == null ? null : ordemAtiva.getId());
                    continue;
                }
                if (ordemAtiva != null) {
                    int quantidade = ThreadLocalRandom.current().nextInt(1, 5);
                    int refugadas = ThreadLocalRandom.current().nextDouble() < 0.06 ? 1 : 0;
                    BigDecimal energia = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.3, 1.8)).setScale(3, java.math.RoundingMode.HALF_UP);
                    registroService.registrar(ordemAtiva.getId(), new RegistroProducaoRequest(quantidade, refugadas, energia));
                }
            } else if (maquina.getEstadoAtual() == EstadoMaquina.FALHA && sorteio < 0.30) {
                maquinaService.alterarEstado(maquina.getId(), EstadoMaquina.PARADA);
                eventoService.registrarSimulado(CategoriaEvento.RETOMADA, "Equipamento " + maquina.getCodigo() + " liberado após falha simulada.", maquina.getId(), ordemAtiva == null ? null : ordemAtiva.getId());
            } else if (maquina.getEstadoAtual() == EstadoMaquina.MANUTENCAO && sorteio < 0.15) {
                maquinaService.alterarEstado(maquina.getId(), EstadoMaquina.PARADA);
                eventoService.registrarSimulado(CategoriaEvento.MANUTENCAO, "Manutenção simulada finalizada no equipamento " + maquina.getCodigo(), maquina.getId(), null);
            } else if (maquina.getEstadoAtual() == EstadoMaquina.PARADA && ordemAtiva != null && sorteio < 0.25) {
                maquinaService.alterarEstado(maquina.getId(), EstadoMaquina.OPERANDO);
            }
        }
    }
}
