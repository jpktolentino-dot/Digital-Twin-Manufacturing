package com.jermam.service;

import com.jermam.dto.SetorRequest;
import com.jermam.dto.SetorResponse;
import com.jermam.entity.Setor;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SetorService {
    private final SetorRepository repository;

    @Transactional(readOnly = true)
    public List<SetorResponse> listar() { return repository.findAll().stream().map(this::mapear).toList(); }

    @Transactional(readOnly = true)
    public SetorResponse buscar(Long id) { return mapear(obter(id)); }

    public SetorResponse criar(SetorRequest request) {
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCase(codigo)) throw new IllegalArgumentException("Já existe um setor com esse código.");
        Setor setor = new Setor();
        setor.setNome(request.nome().trim());
        setor.setCodigo(codigo);
        return mapear(repository.save(setor));
    }

    public SetorResponse atualizar(Long id, SetorRequest request) {
        Setor setor = obter(id);
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) throw new IllegalArgumentException("Já existe um setor com esse código.");
        setor.setNome(request.nome().trim());
        setor.setCodigo(codigo);
        return mapear(repository.save(setor));
    }

    public void excluir(Long id) { repository.delete(obter(id)); }

    public Setor obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado."));
    }

    private SetorResponse mapear(Setor setor) { return new SetorResponse(setor.getId(), setor.getNome(), setor.getCodigo()); }
}
