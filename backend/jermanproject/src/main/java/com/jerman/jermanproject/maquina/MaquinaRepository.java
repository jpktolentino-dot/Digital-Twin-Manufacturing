package com.jerman.jermanproject.maquina;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaquinaRepository
        extends JpaRepository<MaquinaEntity, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );
}