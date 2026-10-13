package com.jermam.service;

import com.jermam.dto.EventoRequest;
import com.jermam.dto.EventoResponse;
import com.jermam.entity.Evento;
import com.jermam.entity.Maquina;
import com.jermam.entity.OrdemProducao;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.EventoRepository;
import com.jermam.repository.MaquinaRepository;
import com.jermam.repository.OrdemProducaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventoService {
    private final EventoRepository repository;
    private final MaquinaRepository maquinaRepository;
    private final OrdemProducaoRepository ordemRepository;

    @Transactional(readOnly = true)
    public List<EventoResponse> listar(boolean apenasAtivos, Long maquinaId) {
        List<Evento> eventos;
        if (maquinaId != null) eventos = repository.findByMaquinaIdOrderByDataHoraDesc(maquinaId);
        else if (apenasAtivos) eventos = repository.findByResolvidoFalseOrderByDataHoraDesc();
        else eventos = repository.findAllByOrderByDataHoraDesc();
        return eventos.stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscar(Long id) { return mapear(obter(id)); }

    public EventoResponse criar(EventoRequest request) {
        Maquina maquina = maquinaRepository.findById(request.maquinaId()).orElseThrow(() -> new RecursoNaoEncontradoException("Máquina não encontrada."));
        OrdemProducao ordem = null;
        if (request.ordemProducaoId() != null) ordem = ordemRepository.findById(request.ordemProducaoId()).orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de produção não encontrada."));
        Evento evento = new Evento();
        evento.setCategoria(request.categoria()); evento.setDescricao(request.descricao().trim());
        evento.setImpactoEstimadoMinutos(request.impactoEstimadoMinutos()); evento.setDataHora(LocalDateTime.now());
        evento.setMaquina(maquina); evento.setOrdemProducao(ordem);
        return mapear(repository.save(evento));
    }

    public EventoResponse resolver(Long id) { Evento e = obter(id); e.setResolvido(true); return mapear(repository.save(e)); }
    public void excluir(Long id) { repository.delete(obter(id)); }
    public Evento obter(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado.")); }

    public EventoResponse registrarSimulado(com.jermam.enums.CategoriaEvento categoria, String descricao, Long maquinaId, Long ordemId) {
        EventoRequest request = new EventoRequest(categoria, descricao, BigDecimal.ZERO, maquinaId, ordemId);
        return criar(request);
    }

    private EventoResponse mapear(Evento e) {
        return new EventoResponse(e.getId(), e.getCategoria(), e.getDescricao(), e.getImpactoEstimadoMinutos(), e.getDataHora(), e.isResolvido(),
                e.getMaquina().getId(), e.getMaquina().getNome(),
                e.getOrdemProducao() == null ? null : e.getOrdemProducao().getId(),
                e.getOrdemProducao() == null ? null : e.getOrdemProducao().getNumero(),
                e.getUsuario() == null ? null : e.getUsuario().getNome());
    }
}
