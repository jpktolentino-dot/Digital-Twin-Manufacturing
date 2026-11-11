"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

type Modulo = {
  titulo: string;
  descricao: string;
  href?: string; // sem href = ainda não implementado
  adminOnly?: boolean;
};

// Ordem: Análise, Produção, Cadastros
const GRUPOS: { nome: string; modulos: Modulo[] }[] = [
  {
    nome: "Análise",
    modulos: [
      { titulo: "Dashboard", descricao: "Visão por máquina: KPIs, histórico, ordem de produção e produto.", href: "/dashboard" },
      { titulo: "Histórico de máquinas", descricao: "Transições de estado de cada máquina.", href: "/historico-maquina" },
    ],
  },
  {
    nome: "Produção",
    modulos: [
      { titulo: "Ordens de produção", descricao: "Planejamento, estágios e cancelamento.", href: "/ordem-producao" },
      { titulo: "Eventos", descricao: "Registro de eventos industriais.", href: "/eventos" },
    ],
  },
  {
    nome: "Cadastros",
    modulos: [
      { titulo: "Usuários", descricao: "Listar, editar, ativar e desativar usuários.", href: "/usuarios", adminOnly: true },
      { titulo: "Novo usuário", descricao: "Cadastrar usuário ou primeiro administrador.", href: "/test", adminOnly: true },
      { titulo: "Setores", descricao: "Setores da fábrica.", href: "/setores" },
      { titulo: "Máquinas", descricao: "Máquinas, estado operacional e setor.", href: "/maquinas" },
      { titulo: "Produtos", descricao: "Produtos fabricados.", href: "/produtos" },
    ],
  },
];

const base = "rounded-lg border p-4 transition";
const ativo = `${base} border-zinc-200 bg-white hover:border-blue-500 hover:shadow-sm dark:border-zinc-800 dark:bg-zinc-900 dark:hover:border-blue-500`;
const inativo = `${base} cursor-not-allowed border-dashed border-zinc-300 opacity-60 dark:border-zinc-700`;

export default function HomePage() {
  // null = ainda carregando (não mostra cards de admin até saber o perfil)
  const [tipo, setTipo] = useState<string | null>(null);

  useEffect(() => {
    fetch("/auth/me")
      .then((r) => (r.ok ? r.json() : null))
      .then((d) => setTipo(d?.tipo ?? "OPERADOR"))
      .catch(() => setTipo("OPERADOR"));
  }, []);

  const ehAdmin = tipo === "ADMINISTRADOR";

  const grupos = GRUPOS.map((g) => ({
    ...g,
    modulos: g.modulos.filter((m) => !m.adminOnly || ehAdmin),
  })).filter((g) => g.modulos.length > 0);

  return (
    <main className="mx-auto grid max-w-5xl gap-8 px-4 py-10 text-zinc-900 dark:text-zinc-100">
      <div>
        <h1 className="text-2xl font-semibold">Digital Twin Manufacturing</h1>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Selecione um módulo para começar.
        </p>
      </div>

      {grupos.map((grupo) => (
        <section key={grupo.nome} className="grid gap-3">
          <h2 className="text-sm font-medium uppercase tracking-wide text-zinc-500">
            {grupo.nome}
          </h2>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {grupo.modulos.map((m) =>
              m.href ? (
                <Link key={m.titulo} href={m.href} className={ativo}>
                  <h3 className="font-medium">{m.titulo}</h3>
                  <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">{m.descricao}</p>
                </Link>
              ) : (
                <div key={m.titulo} className={inativo} aria-disabled="true">
                  <h3 className="font-medium">{m.titulo}</h3>
                  <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">{m.descricao}</p>
                </div>
              )
            )}
          </div>
        </section>
      ))}
    </main>
  );
}