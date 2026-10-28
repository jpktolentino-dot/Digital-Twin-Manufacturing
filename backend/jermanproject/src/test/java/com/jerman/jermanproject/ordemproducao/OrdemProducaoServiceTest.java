
package com.jerman.jermanproject.ordemproducao;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.produto.ProdutoEntity;
import com.jerman.jermanproject.produto.ProdutoRepository;
import com.jerman.jermanproject.setor.SetorEntity;
import com.jerman.jermanproject.usuario.TipoUsuario;
import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoServiceTest {

    @Mock
    private OrdemProducaoRepository ordemProducaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @InjectMocks
    private OrdemProducaoService ordemProducaoService;

    private ProdutoEntity produto;
    private UsuarioEntity operador;
    private SetorEntity setor;
    private MaquinaEntity maquina;

    @BeforeEach
    void setUp() {
        produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Peça metálica");
        produto.setCodigo("PROD-01");
        produto.setAtivo(true);

        operador = new UsuarioEntity();
        operador.setId(2L);
        operador.setNome("Operador de teste");
        operador.setEmail("operador@exemplo.com");
        operador.setTipo(TipoUsuario.OPERADOR);
        operador.setAtivo(true);

        setor = new SetorEntity();
        setor.setId(3L);
        setor.setNome("Usinagem");
        setor.setCodigo("USI");
        setor.setAtivo(true);

        maquina = new MaquinaEntity();
        maquina.setId(10L);
        maquina.setNome("Torno CNC");
        maquina.setCodigo("MAQ-01");
        maquina.setSetor(setor);
        maquina.setAtivo(true);
    }

    private OrdemProducaoRequest criarRequest(
            String numero
    ) {
        return new OrdemProducaoRequest(
                numero,
                100,
                1L,
                2L,
                List.of(10L)
        );
    }

    private void prepararReferenciasValidas() {
        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(operador));

        when(maquinaRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(maquina));
    }

    private OrdemProducaoEntity ordemExistente() {
        OrdemProducaoEntity ordem = new OrdemProducaoEntity();
        ordem.setId(1L);
        ordem.setNumero("OP-001");
        ordem.setQuantidadePlanejada(100);
        ordem.setEstagioAtual(EstagioOrdemProducao.PLANEJADA);
        ordem.setProduto(produto);
        ordem.setOperador(operador);
        ordem.setMaquinas(
                new LinkedHashSet<>(Set.of(maquina))
        );

        return ordem;
    }

    @Test
    void deveCriarOrdemDeProducaoPlanejada() {
        when(ordemProducaoRepository
                .existsByNumeroIgnoreCase("OP-001"))
                .thenReturn(false);

        prepararReferenciasValidas();

        when(ordemProducaoRepository.save(
                any(OrdemProducaoEntity.class)))
                .thenAnswer(invocation -> {
                    OrdemProducaoEntity ordem =
                            invocation.getArgument(0);
                    ordem.setId(1L);
                    return ordem;
                });

        OrdemProducaoResponse resposta =
                ordemProducaoService.criar(criarRequest("OP-001"));

        assertEquals(1L, resposta.id());
        assertEquals("OP-001", resposta.numero());
        assertEquals(100, resposta.quantidadePlanejada());
        assertEquals(
                EstagioOrdemProducao.PLANEJADA,
                resposta.estagioAtual()
        );
        assertEquals(1L, resposta.produtoId());
        assertEquals(2L, resposta.operadorId());
        assertEquals(List.of(10L), resposta.maquinasIds());

        verify(ordemProducaoRepository)
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void naoDeveCriarOrdemComNumeroDuplicado() {
        when(ordemProducaoRepository
                .existsByNumeroIgnoreCase("OP-001"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.criar(
                        criarRequest("OP-001")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));

        verifyNoInteractions(
                produtoRepository,
                usuarioRepository,
                maquinaRepository
        );
    }

    @Test
    void naoDeveCriarOrdemComProdutoInativo() {
        produto.setAtivo(false);

        when(ordemProducaoRepository
                .existsByNumeroIgnoreCase("OP-001"))
                .thenReturn(false);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.criar(
                        criarRequest("OP-001")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void naoDeveCriarOrdemComAdministradorComoOperador() {
        operador.setTipo(TipoUsuario.ADMINISTRADOR);

        when(ordemProducaoRepository
                .existsByNumeroIgnoreCase("OP-001"))
                .thenReturn(false);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(operador));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.criar(
                        criarRequest("OP-001")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void naoDeveCriarOrdemComMaquinaInativa() {
        maquina.setAtivo(false);

        when(ordemProducaoRepository
                .existsByNumeroIgnoreCase("OP-001"))
                .thenReturn(false);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(operador));

        when(maquinaRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(maquina));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.criar(
                        criarRequest("OP-001")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void deveBuscarOrdemPorId() {
        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordemExistente()));

        OrdemProducaoResponse resposta =
                ordemProducaoService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("OP-001", resposta.numero());
        assertEquals(
                EstagioOrdemProducao.PLANEJADA,
                resposta.estagioAtual()
        );
    }

    @Test
    void naoDeveBuscarOrdemInexistente() {
        when(ordemProducaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deveAtualizarOrdemPlanejada() {
        OrdemProducaoEntity ordem = ordemExistente();

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        when(ordemProducaoRepository
                .existsByNumeroIgnoreCaseAndIdNot(
                        "OP-002",
                        1L
                ))
                .thenReturn(false);

        prepararReferenciasValidas();

        when(ordemProducaoRepository.save(
                any(OrdemProducaoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrdemProducaoResponse resposta =
                ordemProducaoService.atualizar(
                        1L,
                        criarRequest("OP-002")
                );

        assertEquals("OP-002", resposta.numero());
        assertEquals(
                EstagioOrdemProducao.PLANEJADA,
                resposta.estagioAtual()
        );
    }

    @Test
    void naoDeveEditarOrdemEmProducao() {
        OrdemProducaoEntity ordem = ordemExistente();
        ordem.setEstagioAtual(
                EstagioOrdemProducao.EM_PRODUCAO
        );

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.atualizar(
                        1L,
                        criarRequest("OP-002")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void deveAlterarOrdemPlanejadaParaEmProducao() {
        OrdemProducaoEntity ordem = ordemExistente();

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        when(ordemProducaoRepository.save(
                any(OrdemProducaoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrdemProducaoResponse resposta =
                ordemProducaoService.alterarEstagio(
                        1L,
                        EstagioOrdemProducao.EM_PRODUCAO
                );

        assertEquals(
                EstagioOrdemProducao.EM_PRODUCAO,
                resposta.estagioAtual()
        );

        verify(ordemProducaoRepository).save(ordem);
    }

    @Test
    void naoDevePermitirTransicaoDiretaDePlanejadaParaConcluida() {
        OrdemProducaoEntity ordem = ordemExistente();

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.alterarEstagio(
                        1L,
                        EstagioOrdemProducao.CONCLUIDA
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }

    @Test
    void deveCancelarOrdemSemExcluirFisicamente() {
        OrdemProducaoEntity ordem = ordemExistente();
        ordem.setEstagioAtual(
                EstagioOrdemProducao.EM_PRODUCAO
        );

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        when(ordemProducaoRepository.save(
                any(OrdemProducaoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ordemProducaoService.cancelar(1L);

        assertEquals(
                EstagioOrdemProducao.CANCELADA,
                ordem.getEstagioAtual()
        );

        verify(ordemProducaoRepository).save(ordem);
        verify(ordemProducaoRepository, never())
                .delete(any(OrdemProducaoEntity.class));
    }

    @Test
    void naoDeveCancelarOrdemConcluida() {
        OrdemProducaoEntity ordem = ordemExistente();
        ordem.setEstagioAtual(
                EstagioOrdemProducao.CONCLUIDA
        );

        when(ordemProducaoRepository.findById(1L))
                .thenReturn(Optional.of(ordem));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ordemProducaoService.cancelar(1L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(ordemProducaoRepository, never())
                .save(any(OrdemProducaoEntity.class));
    }
}
