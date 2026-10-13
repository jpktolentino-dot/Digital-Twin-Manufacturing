package com.jermam.repository;

import com.jermam.entity.HistoricoMaquina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HistoricoMaquinaRepository extends JpaRepository<HistoricoMaquina, Long> {
    List<HistoricoMaquina> findByMaquinaIdOrderByDataHoraInicioDesc(Long maquinaId);
    Optional<HistoricoMaquina> findFirstByMaquinaIdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(Long maquinaId);
}
