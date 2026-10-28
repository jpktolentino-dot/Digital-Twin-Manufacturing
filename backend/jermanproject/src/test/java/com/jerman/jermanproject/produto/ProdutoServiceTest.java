
package com.jerman.jermanproject.produto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoEntity produto;

    @BeforeEach
    void setUp() {
        produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Peça metálica");
        produto.setCodigo("PROD-01");
        produto.setEspecificacoes("Peça de aço");
        produto.setAtivo(true);
    }

    @Test
    void deveListarTodosOsProdutos() {
        ProdutoEntity produtoInativo = new ProdutoEntity();
        produtoInativo.setId(2L);
        produtoInativo.setNome("Peça plástica");
        produtoInativo.setCodigo("PROD-02");
        produtoInativo.setAtivo(false);

        when(produtoRepository.findAll())
                .thenReturn(List.of(produto, produtoInativo));

        List<ProdutoEntity> resultado = produtoService.listarTodos();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).isAtivo());
        assertFalse(resultado.get(1).isAtivo());

        verify(produtoRepository).findAll();
    }

    @Test
    void deveBuscarProdutoPorId() {
        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        ProdutoEntity resultado = produtoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Peça metálica", resultado.getNome());
        assertEquals("PROD-01", resultado.getCodigo());
    }

    @Test
    void deveLancarNotFoundQuandoProdutoNaoExiste() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> produtoService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deveCriarProduto() {
        ProdutoRequest request = new ProdutoRequest(
                " Nova peça ",
                " PROD-03 ",
                "Especificações da nova peça"
        );

        when(produtoRepository.existsByCodigoIgnoreCase("PROD-03"))
                .thenReturn(false);

        when(produtoRepository.save(any(ProdutoEntity.class)))
                .thenAnswer(invocation -> {
                    ProdutoEntity salvo = invocation.getArgument(0);
                    salvo.setId(3L);
                    return salvo;
                });

        ProdutoEntity resultado = produtoService.criar(request);

        assertEquals(3L, resultado.getId());
        assertEquals("Nova peça", resultado.getNome());
        assertEquals("PROD-03", resultado.getCodigo());
        assertEquals(
                "Especificações da nova peça",
                resultado.getEspecificacoes()
        );
        assertTrue(resultado.isAtivo());

        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    void naoDeveCriarProdutoComCodigoDuplicado() {
        ProdutoRequest request = new ProdutoRequest(
                "Outra peça",
                "PROD-01",
                "Outra especificação"
        );

        when(produtoRepository.existsByCodigoIgnoreCase("PROD-01"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> produtoService.criar(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(produtoRepository, never())
                .save(any(ProdutoEntity.class));
    }

    @Test
    void deveAtualizarProduto() {
        ProdutoRequest request = new ProdutoRequest(
                "Peça metálica atualizada",
                "PROD-01",
                "Aço inoxidável"
        );

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository
                .existsByCodigoIgnoreCaseAndIdNot("PROD-01", 1L))
                .thenReturn(false);

        when(produtoRepository.save(any(ProdutoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoEntity resultado =
                produtoService.atualizar(1L, request);

        assertEquals("Peça metálica atualizada", resultado.getNome());
        assertEquals("PROD-01", resultado.getCodigo());
        assertEquals("Aço inoxidável", resultado.getEspecificacoes());

        verify(produtoRepository).save(produto);
    }

    @Test
    void naoDeveAtualizarProdutoComCodigoDeOutroProduto() {
        ProdutoRequest request = new ProdutoRequest(
                "Peça atualizada",
                "PROD-02",
                "Especificações atualizadas"
        );

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository
                .existsByCodigoIgnoreCaseAndIdNot("PROD-02", 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> produtoService.atualizar(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(produtoRepository, never())
                .save(any(ProdutoEntity.class));
    }

    @Test
    void deveDesativarProdutoSemExclusaoFisica() {
        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository.save(any(ProdutoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        produtoService.desativar(1L);

        assertFalse(produto.isAtivo());

        verify(produtoRepository).save(produto);
        verify(produtoRepository, never()).delete(produto);
    }

    @Test
    void deveReativarProduto() {
        produto.setAtivo(false);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(produtoRepository.save(any(ProdutoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        produtoService.ativar(1L);

        assertTrue(produto.isAtivo());

        verify(produtoRepository).save(produto);
    }

    @Test
    void naoDeveDesativarProdutoInexistente() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> produtoService.desativar(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(produtoRepository, never())
                .save(any(ProdutoEntity.class));
    }

    @Test
    void naoDeveAtivarProdutoInexistente() {
        when(produtoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> produtoService.ativar(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(produtoRepository, never())
                .save(any(ProdutoEntity.class));
    }
}
