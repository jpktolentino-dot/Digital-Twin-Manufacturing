
package com.jerman.jermanproject.ordemproducao;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.produto.ProdutoEntity;
import com.jerman.jermanproject.produto.ProdutoRepository;
import com.jerman.jermanproject.usuario.TipoUsuario;
import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdemProducaoService {

    private final OrdemProducaoRepository ordemProducaoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MaquinaRepository maquinaRepository;

    @Transactional(readOnly = true)
    public List<OrdemProducaoResponse> listarTodas() {
        return ordemProducaoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdemProducaoResponse buscarPorId(Long id) {
        return toResponse(buscarOrdemEntityPorId(id));
    }

    @Transactional
    public OrdemProducaoResponse criar(
            OrdemProducaoRequest request
    ) {
        String numero = request.numero().trim();

        verificarNumeroDuplicado(numero);

        ProdutoEntity produto =
                buscarProdutoAtivo(request.produtoId());

        UsuarioEntity operador =
                buscarOperadorAtivo(request.operadorId());

        List<MaquinaEntity> maquinas =
                buscarMaquinasAtivas(request.maquinasIds());

        OrdemProducaoEntity ordem = new OrdemProducaoEntity();
        ordem.setNumero(numero);
        ordem.setQuantidadePlanejada(
                request.quantidadePlanejada()
        );
        ordem.setEstagioAtual(EstagioOrdemProducao.PLANEJADA);
        ordem.setProduto(produto);
        ordem.setOperador(operador);
        ordem.setMaquinas(new LinkedHashSet<>(maquinas));

        return toResponse(ordemProducaoRepository.save(ordem));
    }

    @Transactional
    public OrdemProducaoResponse atualizar(
            Long id,
            OrdemProducaoRequest request
    ) {
        OrdemProducaoEntity ordem = buscarOrdemEntityPorId(id);

        if (ordem.getEstagioAtual()
                != EstagioOrdemProducao.PLANEJADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Só é possível editar uma ordem planejada."
            );
        }

        String numero = request.numero().trim();

        if (ordemProducaoRepository
                .existsByNumeroIgnoreCaseAndIdNot(numero, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outra ordem com esse número."
            );
        }

        ProdutoEntity produto =
                buscarProdutoAtivo(request.produtoId());

        UsuarioEntity operador =
                buscarOperadorAtivo(request.operadorId());

        List<MaquinaEntity> maquinas =
                buscarMaquinasAtivas(request.maquinasIds());

        ordem.setNumero(numero);
        ordem.setQuantidadePlanejada(
                request.quantidadePlanejada()
        );
        ordem.setProduto(produto);
        ordem.setOperador(operador);
        ordem.setMaquinas(new LinkedHashSet<>(maquinas));

        return toResponse(ordemProducaoRepository.save(ordem));
    }

    @Transactional
    public OrdemProducaoResponse alterarEstagio(
            Long id,
            EstagioOrdemProducao novoEstagio
    ) {
        OrdemProducaoEntity ordem = buscarOrdemEntityPorId(id);

        if (!transicaoPermitida(
                ordem.getEstagioAtual(),
                novoEstagio
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transição de estágio não permitida."
            );
        }

        ordem.setEstagioAtual(novoEstagio);

        return toResponse(ordemProducaoRepository.save(ordem));
    }

    @Transactional
    public void cancelar(Long id) {
        OrdemProducaoEntity ordem = buscarOrdemEntityPorId(id);

        if (ordem.getEstagioAtual()
                == EstagioOrdemProducao.CANCELADA) {
            return;
        }

        if (ordem.getEstagioAtual()
                == EstagioOrdemProducao.CONCLUIDA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Uma ordem concluída não pode ser cancelada."
            );
        }

        ordem.setEstagioAtual(EstagioOrdemProducao.CANCELADA);

        ordemProducaoRepository.save(ordem);
    }

    private boolean transicaoPermitida(
            EstagioOrdemProducao atual,
            EstagioOrdemProducao destino
    ) {
        if (atual == destino) {
            return true;
        }

        return switch (atual) {
            case PLANEJADA ->
                    destino == EstagioOrdemProducao.EM_PRODUCAO
                    || destino == EstagioOrdemProducao.CANCELADA;

            case EM_PRODUCAO ->
                    destino == EstagioOrdemProducao.PAUSADA
                    || destino == EstagioOrdemProducao.CONCLUIDA
                    || destino == EstagioOrdemProducao.CANCELADA;

            case PAUSADA ->
                    destino == EstagioOrdemProducao.EM_PRODUCAO
                    || destino == EstagioOrdemProducao.CANCELADA;

            case CONCLUIDA, CANCELADA -> false;
        };
    }

    private void verificarNumeroDuplicado(String numero) {
        if (ordemProducaoRepository
                .existsByNumeroIgnoreCase(numero)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma ordem com esse número."
            );
        }
    }

    private ProdutoEntity buscarProdutoAtivo(Long produtoId) {
        ProdutoEntity produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                ));

        if (!produto.isAtivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível utilizar um produto inativo."
            );
        }

        return produto;
    }

    private UsuarioEntity buscarOperadorAtivo(Long operadorId) {
        UsuarioEntity operador = usuarioRepository.findById(operadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Operador não encontrado."
                ));

        if (!operador.isAtivo()
                || operador.getTipo() != TipoUsuario.OPERADOR) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O responsável deve ser um operador ativo."
            );
        }

        return operador;
    }

    private List<MaquinaEntity> buscarMaquinasAtivas(
            List<Long> maquinasIds
    ) {
        if (maquinasIds == null || maquinasIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe pelo menos uma máquina."
            );
        }

        Set<Long> idsUnicos = new HashSet<>(maquinasIds);

        if (idsUnicos.size() != maquinasIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A mesma máquina não pode ser informada mais de uma vez."
            );
        }

        List<MaquinaEntity> maquinas =
                maquinaRepository.findAllById(maquinasIds);

        if (maquinas.size() != maquinasIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uma ou mais máquinas não foram encontradas."
            );
        }

        for (MaquinaEntity maquina : maquinas) {
            if (!maquina.isAtivo()
                    || !maquina.getSetor().isAtivo()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Todas as máquinas devem estar ativas e vinculadas a setores ativos."
                );
            }
        }

        return maquinas;
    }

    private OrdemProducaoEntity buscarOrdemEntityPorId(Long id) {
        return ordemProducaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ordem de produção não encontrada."
                ));
    }

    private OrdemProducaoResponse toResponse(
            OrdemProducaoEntity ordem
    ) {
        List<Long> maquinasIds = ordem.getMaquinas()
                .stream()
                .map(MaquinaEntity::getId)
                .sorted()
                .toList();

        return new OrdemProducaoResponse(
                ordem.getId(),
                ordem.getNumero(),
                ordem.getQuantidadePlanejada(),
                ordem.getEstagioAtual(),
                ordem.getProduto().getId(),
                ordem.getProduto().getNome(),
                ordem.getOperador().getId(),
                ordem.getOperador().getNome(),
                maquinasIds
        );
    }
}
