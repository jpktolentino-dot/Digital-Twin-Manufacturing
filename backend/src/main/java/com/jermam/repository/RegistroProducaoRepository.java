package com.jermam.repository;

import com.jermam.entity.RegistroProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface RegistroProducaoRepository extends JpaRepository<RegistroProducao, Long> {
    List<RegistroProducao> findByOrdemProducaoIdOrderByDataHoraDesc(Long ordemProducaoId);
    List<RegistroProducao> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<RegistroProducao> findByDataHoraGreaterThanEqualAndDataHoraLessThan(LocalDateTime inicio, LocalDateTime fim);
}
