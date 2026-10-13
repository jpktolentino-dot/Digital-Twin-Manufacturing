package com.jermam.entity;

import com.jermam.enums.EstagioOrdemProducao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_producao")
@Getter @Setter @NoArgsConstructor
public class OrdemProducao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @Column(nullable = false)
    private Integer quantidadePlanejada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstagioOrdemProducao estagioAtual = EstagioOrdemProducao.PLANEJADA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
