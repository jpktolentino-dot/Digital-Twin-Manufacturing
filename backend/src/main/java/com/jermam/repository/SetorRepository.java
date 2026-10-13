package com.jermam.repository;

import com.jermam.entity.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
