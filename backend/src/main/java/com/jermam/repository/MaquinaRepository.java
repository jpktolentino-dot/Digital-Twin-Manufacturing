package com.jermam.repository;

import com.jermam.entity.Maquina;
import com.jermam.enums.EstadoMaquina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    long countByEstadoAtual(EstadoMaquina estadoAtual);
    List<Maquina> findBySetorId(Long setorId);
}
