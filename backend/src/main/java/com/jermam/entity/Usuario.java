package com.jermam.entity;

import com.jermam.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String loginEmail;

    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUsuario tipo = TipoUsuario.OPERADOR;

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
        loginEmail = loginEmail == null ? null : loginEmail.trim().toLowerCase();
    }
}
