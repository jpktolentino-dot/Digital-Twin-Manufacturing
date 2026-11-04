// src/app/cadastro/page.tsx
"use client";

import { useState } from "react";
import { usuariosService } from "../services/user-service";
import type { Usuario } from "../types";

type Modo = "setup" | "usuario";

export default function CadastroPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [modo, setModo] = useState<Modo>("usuario");
  const [admin, setAdmin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [criado, setCriado] = useState<Usuario | null>(null);

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setErro(null);
    setCriado(null);

    try {
      const dados = { nome, email, senha };
      let usuario: Usuario;

      if (modo === "setup") {
        // Primeiro administrador (público, só funciona com o banco sem usuários)
        usuario = await usuariosService.setup(dados);
      } else {
        // Exige estar logado como ADMINISTRADOR
        usuario = await usuariosService.criar(dados);

        if (admin && usuario.tipo !== "ADMINISTRADOR") {
          usuario = await usuariosService.atualizar(usuario.id, {
            nome: usuario.nome,
            email: usuario.email,
            senha: "",
            tipoUsuario: "ADMINISTRADOR",
            ativo: true,
          });
        }
      }

      setCriado(usuario);
      setNome("");
      setEmail("");
      setSenha("");
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={{ maxWidth: 400, margin: "40px auto", display: "grid", gap: 12 }}>
      <h1>Cadastrar usuário</h1>

      <select value={modo} onChange={(e) => setModo(e.target.value as Modo)}>
        <option value="usuario">Novo usuário (requer admin logado)</option>
        <option value="setup">Primeiro administrador (setup inicial)</option>
      </select>

      <form onSubmit={enviar} style={{ display: "grid", gap: 8 }}>
        <input
          placeholder="Nome"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="E-mail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Senha (mín. 8 caracteres)"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          minLength={8}
          maxLength={72}
          required
        />

        {modo === "usuario" && (
          <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <input
              type="checkbox"
              checked={admin}
              onChange={(e) => setAdmin(e.target.checked)}
            />
            Tornar administrador
          </label>
        )}

        <button disabled={loading}>{loading ? "Enviando..." : "Cadastrar"}</button>
      </form>

      {erro && <p style={{ color: "crimson" }}>Erro: {erro}</p>}
      {criado && <pre>{JSON.stringify(criado, null, 2)}</pre>}
    </main>
  );
}