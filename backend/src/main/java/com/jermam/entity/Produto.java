package com.jermam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter @Setter @NoArgsConstructor
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(columnDefinition = "text")
    private String especificacoes;

    @Column(nullable = false)
    private Integer tempoCicloIdealSegundos = 60;
}
