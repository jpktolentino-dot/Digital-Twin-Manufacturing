package com.jermam.service;

import com.jermam.dto.ProdutoRequest;
import com.jermam.dto.ProdutoResponse;
import com.jermam.entity.Produto;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {
    private final ProdutoRepository repository;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() { return repository.findAll().stream().map(this::mapear).toList(); }
    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) { return mapear(obter(id)); }

    public ProdutoResponse criar(ProdutoRequest request) {
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCase(codigo)) throw new IllegalArgumentException("Já existe um produto com esse código.");
        Produto p = new Produto(); preencher(p, request); return mapear(repository.save(p));
    }

    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto p = obter(id);
        if (repository.existsByCodigoIgnoreCaseAndIdNot(request.codigo().trim(), id)) throw new IllegalArgumentException("Já existe um produto com esse código.");
        preencher(p, request); return mapear(repository.save(p));
    }

    public void excluir(Long id) { repository.delete(obter(id)); }
    public Produto obter(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado.")); }
    private void preencher(Produto p, ProdutoRequest r) {
        p.setNome(r.nome().trim()); p.setCodigo(r.codigo().trim()); p.setEspecificacoes(r.especificacoes()); p.setTempoCicloIdealSegundos(r.tempoCicloIdealSegundos());
    }
    private ProdutoResponse mapear(Produto p) { return new ProdutoResponse(p.getId(), p.getNome(), p.getCodigo(), p.getEspecificacoes(), p.getTempoCicloIdealSegundos()); }
}
