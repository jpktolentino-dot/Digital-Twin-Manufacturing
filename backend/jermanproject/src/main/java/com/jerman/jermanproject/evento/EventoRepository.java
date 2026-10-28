
package com.jerman.jermanproject.evento;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository
        extends JpaRepository<EventoEntity, Long> {

    @EntityGraph(attributePaths = {"maquina", "ordemProducao", "usuario"})
    List<EventoEntity> findAllByOrderByDataHoraDesc();

    @EntityGraph(attributePaths = {"maquina", "ordemProducao", "usuario"})
    List<EventoEntity> findAllByMaquina_IdOrderByDataHoraDesc(
            Long maquinaId
    );

    @EntityGraph(attributePaths = {"maquina", "ordemProducao", "usuario"})
    List<EventoEntity> findAllByOrdemProducao_IdOrderByDataHoraDesc(
            Long ordemProducaoId
    );
}
