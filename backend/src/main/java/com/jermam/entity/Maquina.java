package com.jermam.entity;

import com.jermam.enums.EstadoMaquina;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "maquinas")
@Getter @Setter @NoArgsConstructor
public class Maquina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(columnDefinition = "text")
    private String especificacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMaquina estadoAtual = EstadoMaquina.PARADA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private Usuario operador;

    @Column(nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
    }
}
