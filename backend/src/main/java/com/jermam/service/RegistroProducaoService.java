package com.jermam.service;

import com.jermam.dto.RegistroProducaoRequest;
import com.jermam.dto.RegistroProducaoResponse;
import com.jermam.entity.OrdemProducao;
import com.jermam.entity.RegistroProducao;
import com.jermam.enums.EstagioOrdemProducao;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.OrdemProducaoRepository;
import com.jermam.repository.RegistroProducaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistroProducaoService {
    private final RegistroProducaoRepository repository;
    private final OrdemProducaoRepository ordemRepository;

    public RegistroProducaoResponse registrar(Long ordemId, RegistroProducaoRequest request) {
        OrdemProducao ordem = ordemRepository.findById(ordemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de produção não encontrada."));
        if (ordem.getEstagioAtual() != EstagioOrdemProducao.EM_PRODUCAO) {
            throw new IllegalArgumentException("Só é possível apontar produção em uma OP em produção.");
        }
        if (request.quantidadeRefugada() > request.quantidadeProduzida()) {
            throw new IllegalArgumentException("A quantidade refugado não pode superar a quantidade produzida.");
        }
        RegistroProducao registro = new RegistroProducao();
        registro.setOrdemProducao(ordem);
        registro.setDataHora(LocalDateTime.now());
        registro.setQuantidadeProduzida(request.quantidadeProduzida());
        registro.setQuantidadeRefugada(request.quantidadeRefugada());
        registro.setConsumoEnergeticoKwh(request.consumoEnergeticoKwh());
        return mapear(repository.save(registro));
    }

    @Transactional(readOnly = true)
    public List<RegistroProducaoResponse> listar(Long ordemId) {
        List<RegistroProducao> registros;
        if (ordemId == null) registros = repository.findAll().stream().sorted((a, b) -> b.getDataHora().compareTo(a.getDataHora())).toList();
        else registros = repository.findByOrdemProducaoIdOrderByDataHoraDesc(ordemId);
        return registros.stream().map(this::mapear).toList();
    }

    private RegistroProducaoResponse mapear(RegistroProducao r) {
        return new RegistroProducaoResponse(r.getId(), r.getOrdemProducao().getId(), r.getOrdemProducao().getNumero(),
                r.getDataHora(), r.getQuantidadeProduzida(), r.getQuantidadeRefugada(), r.getConsumoEnergeticoKwh());
    }
}
