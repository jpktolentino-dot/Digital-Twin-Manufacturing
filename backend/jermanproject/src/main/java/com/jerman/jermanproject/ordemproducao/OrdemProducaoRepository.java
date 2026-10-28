
package com.jerman.jermanproject.ordemproducao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemProducaoRepository
        extends JpaRepository<OrdemProducaoEntity, Long> {

    boolean existsByNumeroIgnoreCase(String numero);

    boolean existsByNumeroIgnoreCaseAndIdNot(
            String numero,
            Long id
    );
}
