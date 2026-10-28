
package com.jerman.jermanproject.ordemproducao;

import java.util.LinkedHashSet;
import java.util.Set;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.produto.ProdutoEntity;
import com.jerman.jermanproject.usuario.UsuarioEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordem_producao")
@Getter
@Setter
@NoArgsConstructor
public class OrdemProducaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "quantidade_planejada", nullable = false)
    private Integer quantidadePlanejada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estagio_atual", nullable = false, length = 20)
    private EstagioOrdemProducao estagioAtual =
            EstagioOrdemProducao.PLANEJADA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private UsuarioEntity operador;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ordem_producao_maquina",
            joinColumns = @JoinColumn(name = "ordem_producao_id"),
            inverseJoinColumns = @JoinColumn(name = "maquina_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_ordem_producao_maquina",
                    columnNames = {
                            "ordem_producao_id",
                            "maquina_id"
                    }
            )
    )
    private Set<MaquinaEntity> maquinas = new LinkedHashSet<>();
}
