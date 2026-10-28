
package com.jerman.jermanproject.produto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<ProdutoEntity> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProdutoEntity buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                ));
    }

    @Transactional
    public ProdutoEntity criar(ProdutoRequest request) {
        String codigo = request.codigo().trim();

        verificarCodigoDuplicado(codigo);

        ProdutoEntity produto = new ProdutoEntity();
        preencherProduto(produto, request, codigo);

        return produtoRepository.save(produto);
    }

    @Transactional
    public ProdutoEntity atualizar(
            Long id,
            ProdutoRequest request
    ) {
        ProdutoEntity produto = buscarPorId(id);
        String codigo = request.codigo().trim();

        if (produtoRepository.existsByCodigoIgnoreCaseAndIdNot(
                codigo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro produto com esse código."
            );
        }

        preencherProduto(produto, request, codigo);

        return produtoRepository.save(produto);
    }

    @Transactional
    public void desativar(Long id) {
        ProdutoEntity produto = buscarPorId(id);

        produto.setAtivo(false);

        produtoRepository.save(produto);
    }

    @Transactional
    public void ativar(Long id) {
        ProdutoEntity produto = buscarPorId(id);

        produto.setAtivo(true);

        produtoRepository.save(produto);
    }

    private void verificarCodigoDuplicado(String codigo) {
        if (produtoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um produto com esse código."
            );
        }
    }

    private void preencherProduto(
            ProdutoEntity produto,
            ProdutoRequest request,
            String codigo
    ) {
        produto.setNome(request.nome().trim());
        produto.setCodigo(codigo);
        produto.setEspecificacoes(request.especificacoes());
    }
}
