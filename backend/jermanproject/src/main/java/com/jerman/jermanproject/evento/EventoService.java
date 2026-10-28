
package com.jerman.jermanproject.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.ordemproducao.OrdemProducaoEntity;
import com.jerman.jermanproject.ordemproducao.OrdemProducaoRepository;
import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final MaquinaRepository maquinaRepository;
    private final OrdemProducaoRepository ordemProducaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        return eventoRepository.findAllByOrderByDataHoraDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(Long id) {
        return toResponse(buscarEventoEntityPorId(id));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorMaquina(Long maquinaId) {
        maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Máquina não encontrada."
                ));

        return eventoRepository
                .findAllByMaquina_IdOrderByDataHoraDesc(maquinaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorOrdemProducao(
            Long ordemProducaoId
    ) {
        ordemProducaoRepository.findById(ordemProducaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ordem de produção não encontrada."
                ));

        return eventoRepository
                .findAllByOrdemProducao_IdOrderByDataHoraDesc(
                        ordemProducaoId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        MaquinaEntity maquina = maquinaRepository
                .findById(request.maquinaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Máquina não encontrada."
                ));

        OrdemProducaoEntity ordem = null;

        if (request.ordemProducaoId() != null) {
            ordem = ordemProducaoRepository
                    .findById(request.ordemProducaoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ordem de produção não encontrada."
                    ));

            boolean maquinaAssociada = ordem.getMaquinas()
                    .stream()
                    .anyMatch(item ->
                            item.getId().equals(maquina.getId())
                    );

            if (!maquinaAssociada) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A máquina informada não pertence à ordem de produção."
                );
            }
        }

        UsuarioEntity usuario = null;

        if (request.usuarioId() != null) {
            usuario = usuarioRepository.findById(request.usuarioId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Usuário não encontrado."
                    ));
        }

        EventoEntity evento = new EventoEntity();
        evento.setCategoria(request.categoria().trim());
        evento.setDescricao(request.descricao().trim());
        evento.setImpactoEstimado(request.impactoEstimado());
        evento.setDataHora(
                request.dataHora() != null
                        ? request.dataHora()
                        : LocalDateTime.now()
        );
        evento.setMaquina(maquina);
        evento.setOrdemProducao(ordem);
        evento.setUsuario(usuario);

        return toResponse(eventoRepository.save(evento));
    }

    private EventoEntity buscarEventoEntityPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evento não encontrado."
                ));
    }

    private EventoResponse toResponse(EventoEntity evento) {
        OrdemProducaoEntity ordem = evento.getOrdemProducao();
        UsuarioEntity usuario = evento.getUsuario();

        return new EventoResponse(
                evento.getId(),
                evento.getCategoria(),
                evento.getDescricao(),
                evento.getImpactoEstimado(),
                evento.getDataHora(),
                evento.getMaquina().getId(),
                evento.getMaquina().getNome(),
                ordem != null ? ordem.getId() : null,
                ordem != null ? ordem.getNumero() : null,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : null
        );
    }
}
