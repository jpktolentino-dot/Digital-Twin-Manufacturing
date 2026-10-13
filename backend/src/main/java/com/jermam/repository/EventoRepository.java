package com.jermam.repository;

import com.jermam.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findAllByOrderByDataHoraDesc();
    List<Evento> findByResolvidoFalseOrderByDataHoraDesc();
    List<Evento> findByMaquinaIdOrderByDataHoraDesc(Long maquinaId);
    long countByResolvidoFalse();
}
