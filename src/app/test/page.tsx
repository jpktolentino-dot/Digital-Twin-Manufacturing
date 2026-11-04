// src/app/cadastro/page.tsx
"use client";

import { useState } from "react";
import { usuariosService } from "../services/user-service";
import type { Usuario } from "../types";

export default function CadastroPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [criado, setCriado] = useState<Usuario | null>(null);

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setErro(null);
    setCriado(null);

    try {
      const usuario = await usuariosService.criar({ nome, email, senha });
      setCriado(usuario);
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={{ maxWidth: 400, margin: "40px auto", display: "grid", gap: 12 }}>
      <h1>Cadastrar usuário</h1>

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
        <button disabled={loading}>{loading ? "Enviando..." : "Cadastrar"}</button>
      </form>

      {erro && <p style={{ color: "crimson" }}>Erro: {erro}</p>}
      {criado && <pre>{JSON.stringify(criado, null, 2)}</pre>}
    </main>
  );
}