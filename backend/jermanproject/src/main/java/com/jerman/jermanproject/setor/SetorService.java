package com.jerman.jermanproject.setor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SetorService {

    private final SetorRepository setorRepository;

    @Transactional(readOnly = true)
    public List<SetorEntity> listarTodos() {
        return setorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SetorEntity buscarPorId(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setor não encontrado."
                ));
    }

    @Transactional
    public SetorEntity criar(SetorRequest request) {
        String codigo = request.codigo().trim();

        if (setorRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um setor com esse código."
            );
        }

        SetorEntity setor = new SetorEntity();
        setor.setNome(request.nome().trim());
        setor.setCodigo(codigo);

        return setorRepository.save(setor);
    }

    @Transactional
    public SetorEntity atualizar(Long id, SetorRequest request) {
        SetorEntity setor = buscarPorId(id);
        String codigo = request.codigo().trim();

        if (setorRepository.existsByCodigoIgnoreCaseAndIdNot(
                codigo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro setor com esse código."
            );
        }

        setor.setNome(request.nome().trim());
        setor.setCodigo(codigo);

        return setorRepository.save(setor);
    }

    @Transactional
    public void desativar(Long id) {
        SetorEntity setor = buscarPorId(id);

        setor.setAtivo(false);

        setorRepository.save(setor);
    }

    @Transactional
    public void ativar(Long id) {
        SetorEntity setor = buscarPorId(id);

        setor.setAtivo(true);

        setorRepository.save(setor);
    }

}