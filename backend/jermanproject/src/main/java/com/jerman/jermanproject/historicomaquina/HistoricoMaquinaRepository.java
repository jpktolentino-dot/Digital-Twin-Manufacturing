
package com.jerman.jermanproject.historicomaquina;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoMaquinaRepository
        extends JpaRepository<HistoricoMaquinaEntity, Long> {

    @EntityGraph(attributePaths = "maquina")
    List<HistoricoMaquinaEntity>
            findAllByOrderByDataHoraInicioDesc();

    @EntityGraph(attributePaths = "maquina")
    List<HistoricoMaquinaEntity>
            findAllByMaquina_IdOrderByDataHoraInicioDesc(
                    Long maquinaId
            );

    Optional<HistoricoMaquinaEntity>
            findFirstByMaquina_IdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(
                    Long maquinaId
            );
}
