package com.jerman.jermanproject.setor; 

import jakarta.persistence.*;

import lombok.Getter; 
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "setor")
@Getter
@Setter
@NoArgsConstructor
public class SetorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;
}