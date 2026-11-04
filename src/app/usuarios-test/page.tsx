"use client";

import { useCallback, useEffect, useState } from "react";
import { usuariosService } from "../services/user-service";
import type { TipoUsuario, Usuario } from "../types";

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // buscar por ID
  const [idBusca, setIdBusca] = useState("");
  const [encontrado, setEncontrado] = useState<Usuario | null>(null);

  // edição
  const [editando, setEditando] = useState<Usuario | null>(null);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [tipo, setTipo] = useState<TipoUsuario>("OPERADOR");
  const [ativo, setAtivo] = useState(true);

  const executar = useCallback(async (fn: () => Promise<void>, msg?: string) => {
    setLoading(true);
    setErro(null);
    setAviso(null);
    try {
      await fn();
      if (msg) setAviso(msg);
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setLoading(false);
    }
  }, []);

  const carregar = useCallback(
    () =>
      executar(async () => {
        setUsuarios(await usuariosService.listar());
      }),
    [executar]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  function buscarPorId(e: React.FormEvent) {
    e.preventDefault();
    setEncontrado(null);
    executar(async () => {
      setEncontrado(await usuariosService.buscarPorId(Number(idBusca)));
    });
  }

  function iniciarEdicao(u: Usuario) {
    setEditando(u);
    setNome(u.nome);
    setEmail(u.email);
    setSenha("");
    setTipo(u.tipo);
    setAtivo(u.ativo);
  }

  function salvarEdicao(e: React.FormEvent) {
    e.preventDefault();
    if (!editando) return;
    executar(async () => {
      await usuariosService.atualizar(editando.id, {
        nome,
        email,
        senha, // vazio mantém a senha atual
        tipoUsuario: tipo,
        ativo,
      });
      setEditando(null);
      setUsuarios(await usuariosService.listar());
    }, "Usuário atualizado.");
  }

  function desativar(u: Usuario) {
    executar(async () => {
      await usuariosService.desativar(u.id);
      setUsuarios(await usuariosService.listar());
    }, `Usuário ${u.nome} desativado.`);
  }

  function ativar(u: Usuario) {
    executar(async () => {
      await usuariosService.ativar(u.id);
      setUsuarios(await usuariosService.listar());
    }, `Usuário ${u.nome} reativado.`);
  }

  const celula = { padding: "6px 10px", borderBottom: "1px solid #ddd", textAlign: "left" as const };

  return (
    <main style={{ maxWidth: 900, margin: "40px auto", display: "grid", gap: 20, padding: "0 16px" }}>
      <h1>Usuários</h1>

      {erro && <p style={{ color: "crimson" }}>Erro: {erro}</p>}
      {aviso && <p style={{ color: "green" }}>{aviso}</p>}

      {/* LISTAR */}
      <section style={{ display: "grid", gap: 8 }}>
        <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Lista</h2>
          <button onClick={carregar} disabled={loading}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>
        </div>

        <div style={{ overflowX: "auto" }}>
          <table style={{ borderCollapse: "collapse", width: "100%" }}>
            <thead>
              <tr>
                <th style={celula}>ID</th>
                <th style={celula}>Nome</th>
                <th style={celula}>E-mail</th>
                <th style={celula}>Tipo</th>
                <th style={celula}>Ativo</th>
                <th style={celula}>Ações</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id}>
                  <td style={celula}>{u.id}</td>
                  <td style={celula}>{u.nome}</td>
                  <td style={celula}>{u.email}</td>
                  <td style={celula}>{u.tipo}</td>
                  <td style={celula}>{u.ativo ? "Sim" : "Não"}</td>
                  <td style={{ ...celula, display: "flex", gap: 6 }}>
                    <button onClick={() => iniciarEdicao(u)} disabled={loading}>
                      Editar
                    </button>
                    {u.ativo ? (
                      <button onClick={() => desativar(u)} disabled={loading}>
                        Desativar
                      </button>
                    ) : (
                      <button onClick={() => ativar(u)} disabled={loading}>
                        Reativar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {usuarios.length === 0 && (
                <tr>
                  <td style={celula} colSpan={6}>
                    Nenhum usuário carregado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* BUSCAR POR ID */}
      <section style={{ display: "grid", gap: 8 }}>
        <h2 style={{ margin: 0 }}>Buscar por ID</h2>
        <form onSubmit={buscarPorId} style={{ display: "flex", gap: 8 }}>
          <input
            type="number"
            min={1}
            placeholder="ID"
            value={idBusca}
            onChange={(e) => setIdBusca(e.target.value)}
            required
          />
          <button disabled={loading}>Buscar</button>
        </form>
        {encontrado && <pre>{JSON.stringify(encontrado, null, 2)}</pre>}
      </section>

      {/* EDITAR */}
      {editando && (
        <section style={{ display: "grid", gap: 8 }}>
          <h2 style={{ margin: 0 }}>Editar usuário #{editando.id}</h2>
          <form onSubmit={salvarEdicao} style={{ display: "grid", gap: 8, maxWidth: 400 }}>
            <input placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <input type="email" placeholder="E-mail" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <input
              type="password"
              placeholder="Nova senha (deixe vazio para manter)"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              maxLength={72}
            />
            <select value={tipo} onChange={(e) => setTipo(e.target.value as TipoUsuario)}>
              <option value="OPERADOR">OPERADOR</option>
              <option value="ADMINISTRADOR">ADMINISTRADOR</option>
            </select>
            <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <input type="checkbox" checked={ativo} onChange={(e) => setAtivo(e.target.checked)} />
              Ativo
            </label>
            <div style={{ display: "flex", gap: 8 }}>
              <button disabled={loading}>Salvar</button>
              <button type="button" onClick={() => setEditando(null)}>
                Cancelar
              </button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}