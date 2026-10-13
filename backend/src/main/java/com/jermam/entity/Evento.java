package com.jermam.entity;

import com.jermam.enums.CategoriaEvento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Getter @Setter @NoArgsConstructor
public class Evento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private CategoriaEvento categoria;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal impactoEstimadoMinutos = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(nullable = false)
    private boolean resolvido = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_producao_id")
    private OrdemProducao ordemProducao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
