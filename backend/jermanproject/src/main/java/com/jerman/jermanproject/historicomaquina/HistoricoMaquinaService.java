
package com.jerman.jermanproject.historicomaquina;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.maquina.EstadoMaquina;
import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoMaquinaService {

    private final HistoricoMaquinaRepository historicoMaquinaRepository;
    private final MaquinaRepository maquinaRepository;

    @Transactional(readOnly = true)
    public List<HistoricoMaquinaResponse> listarTodos() {
        return historicoMaquinaRepository
                .findAllByOrderByDataHoraInicioDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoricoMaquinaResponse buscarPorId(Long id) {
        HistoricoMaquinaEntity historico =
                buscarHistoricoEntityPorId(id);

        return toResponse(historico);
    }

    @Transactional(readOnly = true)
    public List<HistoricoMaquinaResponse> listarPorMaquina(
            Long maquinaId
    ) {
        maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Máquina não encontrada."
                ));

        return historicoMaquinaRepository
                .findAllByMaquina_IdOrderByDataHoraInicioDesc(
                        maquinaId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void registrarTransicao(
            MaquinaEntity maquina,
            EstadoMaquina estadoAnterior,
            EstadoMaquina estadoNovo
    ) {
        if (estadoAnterior == estadoNovo) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();

        historicoMaquinaRepository
                .findFirstByMaquina_IdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(
                        maquina.getId()
                )
                .ifPresent(historicoAberto -> {
                    historicoAberto.setDataHoraFim(agora);
                    historicoMaquinaRepository.save(historicoAberto);
                });

        HistoricoMaquinaEntity novoHistorico =
                new HistoricoMaquinaEntity();

        novoHistorico.setMaquina(maquina);
        novoHistorico.setEstadoAnterior(estadoAnterior);
        novoHistorico.setEstadoNovo(estadoNovo);
        novoHistorico.setDataHoraInicio(agora);
        novoHistorico.setDataHoraFim(null);

        historicoMaquinaRepository.save(novoHistorico);
    }

    private HistoricoMaquinaEntity buscarHistoricoEntityPorId(
            Long id
    ) {
        return historicoMaquinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Histórico da máquina não encontrado."
                ));
    }

    private HistoricoMaquinaResponse toResponse(
            HistoricoMaquinaEntity historico
    ) {
        MaquinaEntity maquina = historico.getMaquina();

        return new HistoricoMaquinaResponse(
                historico.getId(),
                historico.getEstadoAnterior(),
                historico.getEstadoNovo(),
                historico.getDataHoraInicio(),
                historico.getDataHoraFim(),
                maquina.getId(),
                maquina.getNome()
        );
    }
}
