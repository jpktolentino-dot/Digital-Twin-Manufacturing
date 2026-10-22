package com.jerman.jermanproject.maquina;

import com.jerman.jermanproject.setor.SetorEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maquina")
@Getter
@Setter
@NoArgsConstructor
public class MaquinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(columnDefinition = "text")
    private String especificacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_atual", nullable = false, length = 20)
    private EstadoMaquina estadoAtual = EstadoMaquina.PARADA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private SetorEntity setor;

    // Referência temporária até a implementação do módulo usuario.
    @Column(name = "operador_id", nullable = false)
    private Long operadorId;
}