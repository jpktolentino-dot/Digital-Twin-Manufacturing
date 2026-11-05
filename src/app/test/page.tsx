// src/app/cadastro/page.tsx
"use client";

import Link from "next/link";
import { useState } from "react";
import { usuariosService } from "../services/user-service";
import type { Usuario } from "../types";

type Modo = "setup" | "usuario";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;

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
    <main className="mx-auto grid max-w-md gap-5 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-xl font-semibold sm:text-2xl">Cadastrar usuário</h1>
      </div>

      <label className="grid gap-1 text-sm text-zinc-600 dark:text-zinc-400">
        Tipo de cadastro
        <select
          className={input}
          value={modo}
          onChange={(e) => setModo(e.target.value as Modo)}
        >
          <option value="usuario">Novo usuário (requer admin logado)</option>
          <option value="setup">Primeiro administrador (setup inicial)</option>
        </select>
      </label>

      <form onSubmit={enviar} className="grid gap-3">
        <input
          className={input}
          placeholder="Nome"
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          required
        />
        <input
          className={input}
          type="email"
          placeholder="E-mail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className={input}
          type="password"
          placeholder="Senha (mín. 8 caracteres)"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          minLength={8}
          maxLength={72}
          required
        />

        {modo === "usuario" && (
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={admin}
              onChange={(e) => setAdmin(e.target.checked)}
            />
            Tornar administrador
          </label>
        )}

        <button disabled={loading} className={`${btnPrimario} w-full`}>
          {loading ? "Enviando..." : "Cadastrar"}
        </button>
      </form>

      {erro && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          Erro: {erro}
        </p>
      )}

      {criado && (
        <div className="grid gap-2 rounded-md border border-green-200 bg-green-50 p-3 text-sm text-green-800 dark:border-green-900 dark:bg-green-950 dark:text-green-300">
          <p className="font-medium">Usuário cadastrado com sucesso.</p>
          <dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1">
            <dt className="opacity-70">ID</dt>
            <dd>{criado.id}</dd>
            <dt className="opacity-70">Nome</dt>
            <dd className="break-words">{criado.nome}</dd>
            <dt className="opacity-70">E-mail</dt>
            <dd className="break-all">{criado.email}</dd>
            <dt className="opacity-70">Tipo</dt>
            <dd>{criado.tipo}</dd>
          </dl>
          <Link
            href="/usuarios"
            className="mt-1 text-sm underline-offset-2 hover:underline"
          >
            Ver lista de usuários
          </Link>
        </div>
      )}
    </main>
  );
}