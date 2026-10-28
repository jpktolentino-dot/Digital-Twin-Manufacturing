package com.jerman.jermanproject.maquina;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.setor.SetorEntity;
import com.jerman.jermanproject.setor.SetorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaquinaService {

    private final MaquinaRepository maquinaRepository;
    private final SetorRepository setorRepository;

    @Transactional(readOnly = true)
    public List<MaquinaEntity> listarTodas() {
        return maquinaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MaquinaEntity buscarPorId(Long id) {
        return maquinaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Máquina não encontrada."
                ));
    }

    @Transactional
    public MaquinaEntity criar(MaquinaRequest request) {
        String codigo = request.codigo().trim();

        verificarCodigoDuplicado(codigo);

        SetorEntity setor = buscarSetor(request.setorId());

        MaquinaEntity maquina = new MaquinaEntity();
        preencherMaquina(maquina, request, setor, codigo);

        return maquinaRepository.save(maquina);
    }

    @Transactional
    public MaquinaEntity atualizar(Long id, MaquinaRequest request) {
        MaquinaEntity maquina = buscarPorId(id);
        String codigo = request.codigo().trim();

        if (maquinaRepository.existsByCodigoIgnoreCaseAndIdNot(
                codigo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outra máquina com esse código."
            );
        }

        SetorEntity setor = buscarSetor(request.setorId());

        preencherMaquina(maquina, request, setor, codigo);

        return maquinaRepository.save(maquina);
    }

    @Transactional
    public void desativar(Long id) {
        MaquinaEntity maquina = buscarPorId(id);

        maquina.setAtivo(false);

        maquinaRepository.save(maquina);
    }

    @Transactional
    public void ativar(Long id) {
        MaquinaEntity maquina = buscarPorId(id);

        maquina.setAtivo(true);

        maquinaRepository.save(maquina);
    }

    private void verificarCodigoDuplicado(String codigo) {
        if (maquinaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma máquina com esse código."
            );
        }
    }

    private SetorEntity buscarSetor(Long setorId) {
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setor não encontrado."
                ));
    }

    private void preencherMaquina(
            MaquinaEntity maquina,
            MaquinaRequest request,
            SetorEntity setor,
            String codigo
    ) {
        maquina.setNome(request.nome().trim());
        maquina.setCodigo(codigo);
        maquina.setEspecificacoes(request.especificacoes());
        maquina.setEstadoAtual(request.estadoAtual());
        maquina.setSetor(setor);
        maquina.setOperadorId(request.operadorId());
    }
}