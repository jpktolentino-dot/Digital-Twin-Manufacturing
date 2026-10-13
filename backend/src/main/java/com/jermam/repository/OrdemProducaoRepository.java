package com.jermam.repository;

import com.jermam.entity.OrdemProducao;
import com.jermam.enums.EstagioOrdemProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface OrdemProducaoRepository extends JpaRepository<OrdemProducao, Long> {
    boolean existsByNumeroIgnoreCase(String numero);
    boolean existsByNumeroIgnoreCaseAndIdNot(String numero, Long id);
    List<OrdemProducao> findByMaquinaIdAndEstagioAtualIn(Long maquinaId, Collection<EstagioOrdemProducao> estagios);
    List<OrdemProducao> findByEstagioAtualIn(Collection<EstagioOrdemProducao> estagios);
}
