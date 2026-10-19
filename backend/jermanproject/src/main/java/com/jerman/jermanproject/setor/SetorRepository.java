package com.jerman.jermanproject.setor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository
        extends JpaRepository<SetorEntity, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );
} 