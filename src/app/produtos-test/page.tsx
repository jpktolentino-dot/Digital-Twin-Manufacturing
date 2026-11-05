"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { produtosService } from "../services/products-service";
import type { Produto } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950`;

function StatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
        ativo
          ? "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300"
          : "bg-zinc-200 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400"
      }`}
    >
      {ativo ? "Ativo" : "Inativo"}
    </span>
  );
}

function Acoes({
  p,
  disabled,
  onEditar,
  onDesativar,
  onAtivar,
}: {
  p: Produto;
  disabled: boolean;
  onEditar: (p: Produto) => void;
  onDesativar: (p: Produto) => void;
  onAtivar: (p: Produto) => void;
}) {
  return (
    <div className="flex flex-wrap gap-2">
      <button onClick={() => onEditar(p)} disabled={disabled} className={btnSecundario}>
        Editar
      </button>
      {p.ativo ? (
        <button onClick={() => onDesativar(p)} disabled={disabled} className={btnPerigo}>
          Desativar
        </button>
      ) : (
        <button onClick={() => onAtivar(p)} disabled={disabled} className={btnSecundario}>
          Reativar
        </button>
      )}
    </div>
  );
}

export default function ProdutosPage() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // filtro por ID
  const [filtroId, setFiltroId] = useState("");

  // formulário compartilhado entre criar e editar
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [nome, setNome] = useState("");
  const [codigo, setCodigo] = useState("");
  const [especificacoes, setEspecificacoes] = useState("");

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
        setProdutos(await produtosService.listar());
      }),
    [executar]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? produtos.filter((p) => String(p.id) === termo) : produtos;
  }, [produtos, filtroId]);

  function iniciarEdicao(p: Produto) {
    setEditandoId(p.id);
    setNome(p.nome);
    setCodigo(p.codigo);
    setEspecificacoes(p.especificacoes ?? "");
  }

  function cancelarEdicao() {
    setEditandoId(null);
    setNome("");
    setCodigo("");
    setEspecificacoes("");
  }

  function salvar(e: React.FormEvent) {
    e.preventDefault();
    const dados = { nome, codigo, especificacoes };

    executar(async () => {
      if (editandoId) {
        await produtosService.atualizar(editandoId, dados);
      } else {
        await produtosService.criar(dados);
      }
      cancelarEdicao();
      setProdutos(await produtosService.listar());
    }, editandoId ? "Produto atualizado." : "Produto cadastrado.");
  }

  function desativar(p: Produto) {
    executar(async () => {
      await produtosService.desativar(p.id);
      setProdutos(await produtosService.listar());
    }, `Produto ${p.nome} desativado.`);
  }

  function ativar(p: Produto) {
    executar(async () => {
      await produtosService.ativar(p.id);
      setProdutos(await produtosService.listar());
    }, `Produto ${p.nome} reativado.`);
  }

  const vazio = filtroId ? "Nenhum produto com esse ID." : "Nenhum produto carregado.";

  return (
    <main className="mx-auto grid max-w-5xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Produtos</h1>

      {erro && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          Erro: {erro}
        </p>
      )}
      {aviso && (
        <p className="rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700 dark:border-green-900 dark:bg-green-950 dark:text-green-300">
          {aviso}
        </p>
      )}

      {/* FILTRO + LISTA */}
      <section className="grid gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-lg font-medium">Lista</h2>
          <button onClick={carregar} disabled={loading} className={btnSecundario}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>
          <div className="flex w-full gap-2 sm:ml-auto sm:w-auto">
            <input
              className={`${input} sm:w-40`}
              type="number"
              min={1}
              placeholder="Filtrar por ID"
              value={filtroId}
              onChange={(e) => setFiltroId(e.target.value)}
            />
            {filtroId && (
              <button onClick={() => setFiltroId("")} className={btnSecundario}>
                Limpar
              </button>
            )}
          </div>
        </div>

        {/* Cards (celular) */}
        <ul className="grid gap-3 md:hidden">
          {visiveis.map((p) => (
            <li
              key={p.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate font-medium">{p.nome}</p>
                  <p className="truncate font-mono text-xs text-zinc-600 dark:text-zinc-400">
                    {p.codigo}
                  </p>
                </div>
                <span className="text-xs text-zinc-500">#{p.id}</span>
              </div>
              {p.especificacoes && (
                <p className="line-clamp-2 text-sm text-zinc-600 dark:text-zinc-400">
                  {p.especificacoes}
                </p>
              )}
              <div>
                <StatusBadge ativo={p.ativo} />
              </div>
              <Acoes
                p={p}
                disabled={loading}
                onEditar={iniciarEdicao}
                onDesativar={desativar}
                onAtivar={ativar}
              />
            </li>
          ))}
          {visiveis.length === 0 && (
            <li className="rounded-lg border border-dashed border-zinc-300 p-4 text-center text-sm text-zinc-500 dark:border-zinc-700">
              {vazio}
            </li>
          )}
        </ul>

        {/* Tabela (md em diante) */}
        <div className="hidden overflow-x-auto rounded-lg border border-zinc-200 md:block dark:border-zinc-800">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
              <tr>
                <th className="px-3 py-2 font-medium">ID</th>
                <th className="px-3 py-2 font-medium">Nome</th>
                <th className="px-3 py-2 font-medium">Código</th>
                <th className="px-3 py-2 font-medium">Especificações</th>
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((p) => (
                <tr key={p.id}>
                  <td className="px-3 py-2">{p.id}</td>
                  <td className="px-3 py-2">{p.nome}</td>
                  <td className="px-3 py-2 font-mono text-xs">{p.codigo}</td>
                  <td
                    className="max-w-xs truncate px-3 py-2 text-zinc-600 dark:text-zinc-400"
                    title={p.especificacoes}
                  >
                    {p.especificacoes || "-"}
                  </td>
                  <td className="px-3 py-2">
                    <StatusBadge ativo={p.ativo} />
                  </td>
                  <td className="px-3 py-2">
                    <Acoes
                      p={p}
                      disabled={loading}
                      onEditar={iniciarEdicao}
                      onDesativar={desativar}
                      onAtivar={ativar}
                    />
                  </td>
                </tr>
              ))}
              {visiveis.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-4 text-center text-zinc-500">
                    {vazio}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* CRIAR / EDITAR */}
      <section className="grid gap-3">
        <h2 className="text-lg font-medium">
          {editandoId ? `Editar produto #${editandoId}` : "Cadastrar produto"}
        </h2>
        <form onSubmit={salvar} className="grid w-full gap-3 sm:max-w-md">
          <input
            className={input}
            placeholder="Nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />
          <input
            className={input}
            placeholder="Código"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            required
          />
          <textarea
            className={input}
            placeholder="Especificações"
            value={especificacoes}
            onChange={(e) => setEspecificacoes(e.target.value)}
            rows={3}
          />
          <div className="flex gap-2">
            <button disabled={loading} className={btnPrimario}>
              {editandoId ? "Salvar" : "Cadastrar"}
            </button>
            {editandoId && (
              <button type="button" onClick={cancelarEdicao} className={btnSecundario}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </section>
    </main>
  );
}