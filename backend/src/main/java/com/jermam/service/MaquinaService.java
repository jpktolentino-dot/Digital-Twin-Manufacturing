package com.jermam.service;

import com.jermam.dto.MaquinaRequest;
import com.jermam.dto.MaquinaResponse;
import com.jermam.entity.HistoricoMaquina;
import com.jermam.entity.Maquina;
import com.jermam.entity.Setor;
import com.jermam.entity.Usuario;
import com.jermam.enums.EstadoMaquina;
import com.jermam.enums.TipoUsuario;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.HistoricoMaquinaRepository;
import com.jermam.repository.MaquinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaquinaService {
    private final MaquinaRepository repository;
    private final SetorService setorService;
    private final UsuarioService usuarioService;
    private final HistoricoMaquinaRepository historicoRepository;

    @Transactional(readOnly = true)
    public List<MaquinaResponse> listar(Long setorId) {
        List<Maquina> maquinas = setorId == null ? repository.findAll() : repository.findBySetorId(setorId);
        return maquinas.stream().map(this::mapear).toList();
    }
    @Transactional(readOnly = true)
    public MaquinaResponse buscar(Long id) { return mapear(obter(id)); }

    public MaquinaResponse criar(MaquinaRequest request) {
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCase(codigo)) throw new IllegalArgumentException("Já existe uma máquina com esse código.");
        Maquina maquina = new Maquina();
        preencher(maquina, request);
        if (maquina.getEstadoAtual() == null) maquina.setEstadoAtual(EstadoMaquina.PARADA);
        maquina = repository.save(maquina);
        registrarTransicao(maquina, null, maquina.getEstadoAtual());
        return mapear(maquina);
    }

    public MaquinaResponse atualizar(Long id, MaquinaRequest request) {
        Maquina maquina = obter(id);
        if (repository.existsByCodigoIgnoreCaseAndIdNot(request.codigo().trim(), id)) throw new IllegalArgumentException("Já existe uma máquina com esse código.");
        EstadoMaquina anterior = maquina.getEstadoAtual();
        preencher(maquina, request);
        if (request.estadoAtual() == null) maquina.setEstadoAtual(anterior);
        maquina = repository.save(maquina);
        if (anterior != maquina.getEstadoAtual()) registrarTransicao(maquina, anterior, maquina.getEstadoAtual());
        return mapear(maquina);
    }

    public MaquinaResponse alterarEstado(Long id, EstadoMaquina novoEstado) {
        Maquina maquina = obter(id);
        if (maquina.getEstadoAtual() != novoEstado) {
            EstadoMaquina anterior = maquina.getEstadoAtual();
            maquina.setEstadoAtual(novoEstado);
            repository.save(maquina);
            registrarTransicao(maquina, anterior, novoEstado);
        }
        return mapear(maquina);
    }

    public void excluir(Long id) { repository.delete(obter(id)); }
    public Maquina obter(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Máquina não encontrada.")); }

    private void preencher(Maquina maquina, MaquinaRequest request) {
        Setor setor = setorService.obter(request.setorId());
        Usuario operador = usuarioService.obter(request.operadorId());
        if (operador.getTipo() != TipoUsuario.OPERADOR) throw new IllegalArgumentException("A máquina deve ser vinculada a um usuário do tipo OPERADOR.");
        maquina.setNome(request.nome().trim());
        maquina.setCodigo(request.codigo().trim());
        maquina.setEspecificacoes(request.especificacoes());
        maquina.setSetor(setor);
        maquina.setOperador(operador);
        if (request.estadoAtual() != null) maquina.setEstadoAtual(request.estadoAtual());
    }

    private void registrarTransicao(Maquina maquina, EstadoMaquina anterior, EstadoMaquina novo) {
        LocalDateTime agora = LocalDateTime.now();
        historicoRepository.findFirstByMaquinaIdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(maquina.getId())
                .ifPresent(aberto -> { aberto.setDataHoraFim(agora); historicoRepository.save(aberto); });
        HistoricoMaquina historico = new HistoricoMaquina();
        historico.setMaquina(maquina);
        historico.setEstadoAnterior(anterior);
        historico.setEstadoNovo(novo);
        historico.setDataHoraInicio(agora);
        historicoRepository.save(historico);
    }

    private MaquinaResponse mapear(Maquina m) {
        return new MaquinaResponse(m.getId(), m.getNome(), m.getCodigo(), m.getEspecificacoes(), m.getEstadoAtual(),
                m.getSetor().getId(), m.getSetor().getNome(), m.getOperador().getId(), m.getOperador().getNome(), m.getDataCadastro());
    }
}
