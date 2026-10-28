package com.jerman.jermanproject.maquina;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaquinaRepository
        extends JpaRepository<MaquinaEntity, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );

    List<MaquinaEntity> findAllBySetor_Id(Long setorId);
}