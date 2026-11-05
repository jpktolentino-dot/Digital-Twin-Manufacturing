"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { setoresService } from "../services/sectors-service";
import type { Setor } from "../types";

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
  s,
  disabled,
  onEditar,
  onDesativar,
  onAtivar,
}: {
  s: Setor;
  disabled: boolean;
  onEditar: (s: Setor) => void;
  onDesativar: (s: Setor) => void;
  onAtivar: (s: Setor) => void;
}) {
  return (
    <div className="flex flex-wrap gap-2">
      <button onClick={() => onEditar(s)} disabled={disabled} className={btnSecundario}>
        Editar
      </button>
      {s.ativo ? (
        <button onClick={() => onDesativar(s)} disabled={disabled} className={btnPerigo}>
          Desativar
        </button>
      ) : (
        <button onClick={() => onAtivar(s)} disabled={disabled} className={btnSecundario}>
          Reativar
        </button>
      )}
    </div>
  );
}

export default function SetoresPage() {
  const [setores, setSetores] = useState<Setor[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // filtro por ID
  const [filtroId, setFiltroId] = useState("");

  // formulário compartilhado entre criar e editar
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [nome, setNome] = useState("");
  const [codigo, setCodigo] = useState("");

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
        setSetores(await setoresService.listar());
      }),
    [executar]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? setores.filter((s) => String(s.id) === termo) : setores;
  }, [setores, filtroId]);

  function iniciarEdicao(s: Setor) {
    setEditandoId(s.id);
    setNome(s.nome);
    setCodigo(s.codigo);
  }

  function cancelarEdicao() {
    setEditandoId(null);
    setNome("");
    setCodigo("");
  }

  function salvar(e: React.FormEvent) {
    e.preventDefault();
    const dados = { nome, codigo };

    executar(async () => {
      if (editandoId) {
        await setoresService.atualizar(editandoId, dados);
      } else {
        await setoresService.criar(dados);
      }
      cancelarEdicao();
      setSetores(await setoresService.listar());
    }, editandoId ? "Setor atualizado." : "Setor cadastrado.");
  }

  function desativar(s: Setor) {
    executar(async () => {
      await setoresService.desativar(s.id);
      setSetores(await setoresService.listar());
    }, `Setor ${s.nome} desativado.`);
  }

  function ativar(s: Setor) {
    executar(async () => {
      await setoresService.ativar(s.id);
      setSetores(await setoresService.listar());
    }, `Setor ${s.nome} reativado.`);
  }

  const vazio = filtroId ? "Nenhum setor com esse ID." : "Nenhum setor carregado.";

  return (
    <main className="mx-auto grid max-w-4xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Setores</h1>

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
          {visiveis.map((s) => (
            <li
              key={s.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate font-medium">{s.nome}</p>
                  <p className="truncate font-mono text-xs text-zinc-600 dark:text-zinc-400">
                    {s.codigo}
                  </p>
                </div>
                <span className="text-xs text-zinc-500">#{s.id}</span>
              </div>
              <div>
                <StatusBadge ativo={s.ativo} />
              </div>
              <Acoes
                s={s}
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
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((s) => (
                <tr key={s.id}>
                  <td className="px-3 py-2">{s.id}</td>
                  <td className="px-3 py-2">{s.nome}</td>
                  <td className="px-3 py-2 font-mono text-xs">{s.codigo}</td>
                  <td className="px-3 py-2">
                    <StatusBadge ativo={s.ativo} />
                  </td>
                  <td className="px-3 py-2">
                    <Acoes
                      s={s}
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
                  <td colSpan={5} className="px-3 py-4 text-center text-zinc-500">
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
          {editandoId ? `Editar setor #${editandoId}` : "Cadastrar setor"}
        </h2>
        <form onSubmit={salvar} className="grid w-full gap-3 sm:max-w-sm">
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