"use client";

import { useCallback, useEffect, useState } from "react";
import { setoresService } from "../services/sectors-service";
import type { Setor } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950`;

export default function SetoresPage() {
  const [setores, setSetores] = useState<Setor[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const [idBusca, setIdBusca] = useState("");
  const [encontrado, setEncontrado] = useState<Setor | null>(null);

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

  function buscarPorId(e: React.FormEvent) {
    e.preventDefault();
    setEncontrado(null);
    executar(async () => {
      setEncontrado(await setoresService.buscarPorId(Number(idBusca)));
    });
  }

  return (
    <main className="mx-auto grid max-w-4xl gap-6 px-4 py-10 text-zinc-900 dark:text-zinc-100">
      <h1 className="text-2xl font-semibold">Setores</h1>

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

      {/* LISTAR */}
      <section className="grid gap-3">
        <div className="flex items-center gap-3">
          <h2 className="text-lg font-medium">Lista</h2>
          <button onClick={carregar} disabled={loading} className={btnSecundario}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>
        </div>

        <div className="overflow-x-auto rounded-lg border border-zinc-200 dark:border-zinc-800">
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
              {setores.map((s) => (
                <tr key={s.id}>
                  <td className="px-3 py-2">{s.id}</td>
                  <td className="px-3 py-2">{s.nome}</td>
                  <td className="px-3 py-2 font-mono text-xs">{s.codigo}</td>
                  <td className="px-3 py-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        s.ativo
                          ? "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300"
                          : "bg-zinc-200 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400"
                      }`}
                    >
                      {s.ativo ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td className="flex gap-2 px-3 py-2">
                    <button onClick={() => iniciarEdicao(s)} disabled={loading} className={btnSecundario}>
                      Editar
                    </button>
                    {s.ativo ? (
                      <button onClick={() => desativar(s)} disabled={loading} className={btnPerigo}>
                        Desativar
                      </button>
                    ) : (
                      <button onClick={() => ativar(s)} disabled={loading} className={btnSecundario}>
                        Reativar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {setores.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-3 py-4 text-center text-zinc-500">
                    Nenhum setor carregado.
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
        <form onSubmit={salvar} className="grid max-w-sm gap-3">
          <input className={input} placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
          <input className={input} placeholder="Código" value={codigo} onChange={(e) => setCodigo(e.target.value)} required />
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

      {/* BUSCAR POR ID */}
      <section className="grid gap-3">
        <h2 className="text-lg font-medium">Buscar por ID</h2>
        <form onSubmit={buscarPorId} className="flex max-w-sm gap-2">
          <input
            className={input}
            type="number"
            min={1}
            placeholder="ID"
            value={idBusca}
            onChange={(e) => setIdBusca(e.target.value)}
            required
          />
          <button disabled={loading} className={btnPrimario}>
            Buscar
          </button>
        </form>
        {encontrado && (
          <pre className="overflow-x-auto rounded-md bg-zinc-100 p-3 text-xs dark:bg-zinc-900">
            {JSON.stringify(encontrado, null, 2)}
          </pre>
        )}
      </section>
    </main>
  );
}