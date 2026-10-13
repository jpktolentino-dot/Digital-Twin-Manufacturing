package com.jermam.service;

import com.jermam.dto.HistoricoMaquinaResponse;
import com.jermam.entity.HistoricoMaquina;
import com.jermam.repository.HistoricoMaquinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoricoMaquinaService {
    private final HistoricoMaquinaRepository repository;
    private final MaquinaService maquinaService;

    public List<HistoricoMaquinaResponse> listar(Long maquinaId) {
        if (maquinaId != null) {
            maquinaService.obter(maquinaId);
            return repository.findByMaquinaIdOrderByDataHoraInicioDesc(maquinaId).stream().map(this::mapear).toList();
        }
        return repository.findAll().stream().map(this::mapear).toList();
    }

    private HistoricoMaquinaResponse mapear(HistoricoMaquina h) {
        LocalDateTime fim = h.getDataHoraFim() == null ? LocalDateTime.now() : h.getDataHoraFim();
        long minutos = Math.max(0, Duration.between(h.getDataHoraInicio(), fim).toMinutes());
        return new HistoricoMaquinaResponse(h.getId(), h.getEstadoAnterior(), h.getEstadoNovo(), h.getDataHoraInicio(), h.getDataHoraFim(), minutos,
                h.getMaquina().getId(), h.getMaquina().getCodigo());
    }
}
