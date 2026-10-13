package com.jermam.entity;

import com.jermam.enums.EstadoMaquina;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "historicos_maquina")
@Getter @Setter @NoArgsConstructor
public class HistoricoMaquina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoMaquina estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMaquina estadoNovo;

    @Column(nullable = false)
    private LocalDateTime dataHoraInicio;

    private LocalDateTime dataHoraFim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;
}
