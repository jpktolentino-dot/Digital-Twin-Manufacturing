package com.jerman.jermanproject.maquina;

import com.jerman.jermanproject.setor.SetorEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;
}