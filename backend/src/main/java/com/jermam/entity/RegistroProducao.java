package com.jermam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_producao")
@Getter @Setter @NoArgsConstructor
public class RegistroProducao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_producao_id", nullable = false)
    private OrdemProducao ordemProducao;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(nullable = false)
    private Integer quantidadeProduzida;

    @Column(nullable = false)
    private Integer quantidadeRefugada = 0;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal consumoEnergeticoKwh = BigDecimal.ZERO;

    @PrePersist
    void prePersist() {
        if (dataHora == null) dataHora = LocalDateTime.now();
        if (quantidadeRefugada == null) quantidadeRefugada = 0;
        if (consumoEnergeticoKwh == null) consumoEnergeticoKwh = BigDecimal.ZERO;
    }
}
