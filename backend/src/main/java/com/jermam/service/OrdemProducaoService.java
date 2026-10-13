package com.jermam.service;

import com.jermam.dto.OrdemProducaoRequest;
import com.jermam.dto.OrdemProducaoResponse;
import com.jermam.dto.RegistroProducaoResponse;
import com.jermam.entity.Maquina;
import com.jermam.entity.OrdemProducao;
import com.jermam.entity.Produto;
import com.jermam.entity.RegistroProducao;
import com.jermam.enums.EstadoMaquina;
import com.jermam.enums.EstagioOrdemProducao;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.OrdemProducaoRepository;
import com.jermam.repository.ProdutoRepository;
import com.jermam.repository.RegistroProducaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdemProducaoService {
    private final OrdemProducaoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final MaquinaService maquinaService;
    private final RegistroProducaoRepository registroRepository;

    @Transactional(readOnly = true)
    public List<OrdemProducaoResponse> listar(EstagioOrdemProducao estagio) {
        List<OrdemProducao> ordens = repository.findAll().stream()
                .filter(o -> estagio == null || o.getEstagioAtual() == estagio)
                .sorted((a, b) -> b.getId().compareTo(a.getId())).toList();
        return ordens.stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public OrdemProducaoResponse buscar(Long id) { return mapear(obter(id)); }

    public OrdemProducaoResponse criar(OrdemProducaoRequest request) {
        if (repository.existsByNumeroIgnoreCase(request.numero().trim())) throw new IllegalArgumentException("Já existe uma OP com esse número.");
        Produto produto = produtoRepository.findById(request.produtoId()).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        Maquina maquina = maquinaService.obter(request.maquinaId());
        OrdemProducao ordem = new OrdemProducao();
        ordem.setNumero(request.numero().trim());
        ordem.setQuantidadePlanejada(request.quantidadePlanejada());
        ordem.setProduto(produto);
        ordem.setMaquina(maquina);
        ordem.setEstagioAtual(EstagioOrdemProducao.PLANEJADA);
        return mapear(repository.save(ordem));
    }

    public OrdemProducaoResponse atualizar(Long id, OrdemProducaoRequest request) {
        OrdemProducao ordem = obter(id);
        if (ordem.getEstagioAtual() == EstagioOrdemProducao.EM_PRODUCAO || ordem.getEstagioAtual() == EstagioOrdemProducao.CONCLUIDA) {
            throw new IllegalArgumentException("Não é possível editar uma OP em produção ou concluída.");
        }
        if (repository.existsByNumeroIgnoreCaseAndIdNot(request.numero().trim(), id)) throw new IllegalArgumentException("Já existe uma OP com esse número.");
        Produto produto = produtoRepository.findById(request.produtoId()).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
        Maquina maquina = maquinaService.obter(request.maquinaId());
        ordem.setNumero(request.numero().trim()); ordem.setQuantidadePlanejada(request.quantidadePlanejada());
        ordem.setProduto(produto); ordem.setMaquina(maquina);
        return mapear(repository.save(ordem));
    }

    public OrdemProducaoResponse iniciar(Long id) {
        OrdemProducao ordem = obter(id);
        if (ordem.getEstagioAtual() != EstagioOrdemProducao.PLANEJADA && ordem.getEstagioAtual() != EstagioOrdemProducao.PAUSADA) {
            throw new IllegalArgumentException("Apenas OPs planejadas ou pausadas podem ser iniciadas.");
        }
        Maquina maquina = maquinaService.obter(ordem.getMaquina().getId());
        if (maquina.getEstadoAtual() == EstadoMaquina.FALHA || maquina.getEstadoAtual() == EstadoMaquina.MANUTENCAO) {
            throw new IllegalArgumentException("A máquina está em falha ou manutenção.");
        }
        boolean outraEmProducao = repository.findByMaquinaIdAndEstagioAtualIn(maquina.getId(), List.of(EstagioOrdemProducao.EM_PRODUCAO))
                .stream().anyMatch(op -> !op.getId().equals(id));
        if (outraEmProducao) throw new IllegalArgumentException("Já existe outra OP em produção nessa máquina.");
        ordem.setEstagioAtual(EstagioOrdemProducao.EM_PRODUCAO);
        if (ordem.getDataInicio() == null) ordem.setDataInicio(LocalDateTime.now());
        maquinaService.alterarEstado(maquina.getId(), EstadoMaquina.OPERANDO);
        return mapear(repository.save(ordem));
    }

    public OrdemProducaoResponse pausar(Long id) {
        OrdemProducao ordem = obter(id);
        if (ordem.getEstagioAtual() != EstagioOrdemProducao.EM_PRODUCAO) throw new IllegalArgumentException("A OP não está em produção.");
        ordem.setEstagioAtual(EstagioOrdemProducao.PAUSADA);
        maquinaService.alterarEstado(ordem.getMaquina().getId(), EstadoMaquina.PARADA);
        return mapear(repository.save(ordem));
    }

    public OrdemProducaoResponse concluir(Long id) {
        OrdemProducao ordem = obter(id);
        if (ordem.getEstagioAtual() != EstagioOrdemProducao.EM_PRODUCAO && ordem.getEstagioAtual() != EstagioOrdemProducao.PAUSADA) {
            throw new IllegalArgumentException("A OP precisa estar em produção ou pausada para ser concluída.");
        }
        ordem.setEstagioAtual(EstagioOrdemProducao.CONCLUIDA);
        ordem.setDataFim(LocalDateTime.now());
        maquinaService.alterarEstado(ordem.getMaquina().getId(), EstadoMaquina.PARADA);
        OrdemProducao salva = repository.save(ordem);
        return mapear(salva);
    }

    public OrdemProducaoResponse cancelar(Long id) {
        OrdemProducao ordem = obter(id);
        if (ordem.getEstagioAtual() == EstagioOrdemProducao.CONCLUIDA) throw new IllegalArgumentException("Uma OP concluída não pode ser cancelada.");
        if (ordem.getEstagioAtual() == EstagioOrdemProducao.EM_PRODUCAO) maquinaService.alterarEstado(ordem.getMaquina().getId(), EstadoMaquina.PARADA);
        ordem.setEstagioAtual(EstagioOrdemProducao.CANCELADA);
        ordem.setDataFim(LocalDateTime.now());
        return mapear(repository.save(ordem));
    }

    public void excluir(Long id) { repository.delete(obter(id)); }
    public OrdemProducao obter(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de produção não encontrada.")); }

    private OrdemProducaoResponse mapear(OrdemProducao o) {
        List<RegistroProducao> registros = registroRepository.findByOrdemProducaoIdOrderByDataHoraDesc(o.getId());
        int total = registros.stream().mapToInt(RegistroProducao::getQuantidadeProduzida).sum();
        int refugadas = registros.stream().mapToInt(RegistroProducao::getQuantidadeRefugada).sum();
        BigDecimal energia = registros.stream().map(RegistroProducao::getConsumoEnergeticoKwh).reduce(BigDecimal.ZERO, BigDecimal::add);
        double percentual = o.getQuantidadePlanejada() == null || o.getQuantidadePlanejada() == 0 ? 0.0 : Math.min(100.0, total * 100.0 / o.getQuantidadePlanejada());
        return new OrdemProducaoResponse(o.getId(), o.getNumero(), o.getQuantidadePlanejada(), total, total - refugadas, refugadas, energia,
                percentual, o.getEstagioAtual(), o.getProduto().getId(), o.getProduto().getNome(), o.getMaquina().getId(),
                o.getMaquina().getNome(), o.getMaquina().getSetor().getNome(), o.getDataInicio(), o.getDataFim());
    }
}
