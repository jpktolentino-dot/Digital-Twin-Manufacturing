
package com.jerman.jermanproject.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.ordemproducao.OrdemProducaoEntity;
import com.jerman.jermanproject.usuario.UsuarioEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor
public class EventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "impacto_estimado", precision = 12, scale = 2)
    private BigDecimal impactoEstimado;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private MaquinaEntity maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_producao_id")
    private OrdemProducaoEntity ordemProducao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;
}
