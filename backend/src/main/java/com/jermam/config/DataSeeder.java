package com.jermam.config;

import com.jermam.entity.*;
import com.jermam.enums.*;
import com.jermam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    @Bean
    CommandLineRunner popularDados(SetorRepository setores, UsuarioRepository usuarios, ProdutoRepository produtos,
                                   MaquinaRepository maquinas, OrdemProducaoRepository ordens,
                                   RegistroProducaoRepository registros, HistoricoMaquinaRepository historicos,
                                   EventoRepository eventos, PasswordEncoder encoder) {
        return args -> {
            criarUsuarioSeAusente(usuarios, encoder, "Administrador JERMAM", "admin@jermam.com", "admin123", TipoUsuario.ADMINISTRADOR);
            criarUsuarioSeAusente(usuarios, encoder, "João Operador", "operador@jermam.com", "demo123", TipoUsuario.OPERADOR);
            criarUsuarioSeAusente(usuarios, encoder, "Equipe de Manutenção", "manutencao@jermam.com", "demo123", TipoUsuario.MANUTENCAO);
            criarUsuarioSeAusente(usuarios, encoder, "Gestor Industrial", "gestor@jermam.com", "demo123", TipoUsuario.GESTOR);
            if (setores.count() > 0) return;

            Setor usinagem = new Setor(); usinagem.setNome("Usinagem"); usinagem.setCodigo("SET-01"); usinagem = setores.save(usinagem);
            Setor montagem = new Setor(); montagem.setNome("Montagem"); montagem.setCodigo("SET-02"); montagem = setores.save(montagem);
            Usuario operador = usuarios.findByLoginEmailIgnoreCase("operador@jermam.com").orElseThrow();
            Produto produto = new Produto(); produto.setNome("Engrenagem padrão"); produto.setCodigo("PRD-001"); produto.setEspecificacoes("Peça metálica para conjunto de transmissão"); produto.setTempoCicloIdealSegundos(60); produto = produtos.save(produto);

            Maquina torno = novaMaquina("Torno CNC 01", "M-001", usinagem, operador, EstadoMaquina.OPERANDO);
            Maquina fresadora = novaMaquina("Fresadora 02", "M-002", usinagem, operador, EstadoMaquina.PARADA);
            Maquina montagemMaquina = novaMaquina("Estação de montagem", "M-003", montagem, operador, EstadoMaquina.MANUTENCAO);
            torno = maquinas.save(torno); fresadora = maquinas.save(fresadora); montagemMaquina = maquinas.save(montagemMaquina);
            LocalDateTime agora = LocalDateTime.now();
            historicos.save(novoHistorico(torno, null, EstadoMaquina.OPERANDO, agora.minusHours(3)));
            historicos.save(novoHistorico(fresadora, null, EstadoMaquina.PARADA, agora.minusHours(2)));
            historicos.save(novoHistorico(montagemMaquina, null, EstadoMaquina.MANUTENCAO, agora.minusHours(1)));

            OrdemProducao op = new OrdemProducao(); op.setNumero("OP-2026-001"); op.setQuantidadePlanejada(250); op.setProduto(produto); op.setMaquina(torno);
            op.setEstagioAtual(EstagioOrdemProducao.EM_PRODUCAO); op.setDataInicio(agora.minusHours(2)); op = ordens.save(op);
            adicionarRegistro(registros, op, agora.minusMinutes(45), 24, 1, "2.450");
            adicionarRegistro(registros, op, agora.minusMinutes(20), 18, 0, "1.820");
            adicionarRegistro(registros, op, agora.minusMinutes(4), 6, 0, "0.620");

            Evento falha = new Evento(); falha.setCategoria(CategoriaEvento.FALHA); falha.setDescricao("Vibração acima do padrão detectada durante a operação (evento de demonstração).");
            falha.setImpactoEstimadoMinutos(BigDecimal.valueOf(12)); falha.setDataHora(agora.minusMinutes(18)); falha.setMaquina(fresadora); falha.setResolvido(false); eventos.save(falha);
            Evento manutencao = new Evento(); manutencao.setCategoria(CategoriaEvento.MANUTENCAO); manutencao.setDescricao("Inspeção preventiva programada (evento de demonstração).");
            manutencao.setImpactoEstimadoMinutos(BigDecimal.valueOf(30)); manutencao.setDataHora(agora.minusMinutes(50)); manutencao.setMaquina(montagemMaquina); manutencao.setResolvido(false); eventos.save(manutencao);
        };
    }

    private void criarUsuarioSeAusente(UsuarioRepository repo, PasswordEncoder encoder, String nome, String email, String senha, TipoUsuario tipo) {
        if (repo.findByLoginEmailIgnoreCase(email).isPresent()) return;
        Usuario u = new Usuario(); u.setNome(nome); u.setLoginEmail(email); u.setSenha(encoder.encode(senha)); u.setAtivo(true); u.setTipo(tipo); repo.save(u);
    }
    private Maquina novaMaquina(String nome, String codigo, Setor setor, Usuario operador, EstadoMaquina estado) {
        Maquina m = new Maquina(); m.setNome(nome); m.setCodigo(codigo); m.setSetor(setor); m.setOperador(operador); m.setEstadoAtual(estado); m.setEspecificacoes("Equipamento demonstrativo"); return m;
    }
    private HistoricoMaquina novoHistorico(Maquina m, EstadoMaquina anterior, EstadoMaquina novo, LocalDateTime inicio) {
        HistoricoMaquina h = new HistoricoMaquina(); h.setMaquina(m); h.setEstadoAnterior(anterior); h.setEstadoNovo(novo); h.setDataHoraInicio(inicio); return h;
    }
    private void adicionarRegistro(RegistroProducaoRepository repo, OrdemProducao op, LocalDateTime data, int qtd, int refugadas, String energia) {
        RegistroProducao r = new RegistroProducao(); r.setOrdemProducao(op); r.setDataHora(data); r.setQuantidadeProduzida(qtd); r.setQuantidadeRefugada(refugadas); r.setConsumoEnergeticoKwh(new BigDecimal(energia)); repo.save(r);
    }
}
