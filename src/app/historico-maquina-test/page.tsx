"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { historicosMaquinasService } from "../services/machine-history";
import { maquinasService } from "../services/maquina-service";
import type { HistoricoMaquina, Maquina } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;
const selectPequeno =
  "rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-zinc-700 dark:bg-zinc-900";

const corEstado: Record<string, string> = {
  OPERANDO: "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300",
  PARADA: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  FALHA: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300",
  MANUTENCAO: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
};
const corPadrao = "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300";

function Estado({ valor }: { valor: string }) {
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${corEstado[valor] ?? corPadrao}`}>
      {valor}
    </span>
  );
}

function EmAberto() {
  return (
    <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-700 dark:bg-green-950 dark:text-green-300">
      em aberto
    </span>
  );
}

const formatarData = (iso: string | null) =>
  iso ? new Date(iso).toLocaleString("pt-BR") : null;

// Duração entre início e fim (ou até agora, se o registro ainda está aberto)
function duracao(inicio: string, fim: string | null): string {
  const ms = (fim ? new Date(fim).getTime() : Date.now()) - new Date(inicio).getTime();
  const total = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  if (h > 0) return `${h}h ${m}min`;
  if (m > 0) return `${m}min ${s}s`;
  return `${s}s`;
}

export default function HistoricosMaquinasPage() {
  const [registros, setRegistros] = useState<HistoricoMaquina[]>([]);
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [filtroMaquina, setFiltroMaquina] = useState("");
  const [filtroId, setFiltroId] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const executar = useCallback(async (fn: () => Promise<void>) => {
    setLoading(true);
    setErro(null);
    try {
      await fn();
    } catch (err) {
      setErro(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setLoading(false);
    }
  }, []);

  const carregar = useCallback(
    () =>
      executar(async () => {
        setRegistros(
          filtroMaquina
            ? await historicosMaquinasService.listarPorMaquina(Number(filtroMaquina))
            : await historicosMaquinasService.listar()
        );
      }),
    [executar, filtroMaquina]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    maquinasService.listar().then(setMaquinas).catch(() => {});
  }, []);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? registros.filter((r) => String(r.id) === termo) : registros;
  }, [registros, filtroId]);

  const vazio = filtroId ? "Nenhum registro com esse ID." : "Nenhum registro encontrado.";

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Histórico de máquinas</h1>

      {erro && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          Erro: {erro}
        </p>
      )}

      {/* FILTROS + LISTA */}
      <section className="grid gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-lg font-medium">Registros</h2>
          <button onClick={carregar} disabled={loading} className={btnSecundario}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>

          <div className="flex w-full flex-wrap gap-2 sm:ml-auto sm:w-auto">
            <select
              className={`${selectPequeno} w-full sm:w-auto`}
              value={filtroMaquina}
              onChange={(e) => setFiltroMaquina(e.target.value)}
            >
              <option value="">Todas as máquinas</option>
              {maquinas.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nome}
                </option>
              ))}
            </select>
            <input
              className={`${input} flex-1 sm:w-40 sm:flex-none`}
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
          {visiveis.map((r) => (
            <li
              key={r.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <p className="truncate font-medium">{r.maquinaNome}</p>
                <span className="text-xs text-zinc-500">#{r.id}</span>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <Estado valor={r.estadoAnterior} />
                <span className="text-zinc-400">→</span>
                <Estado valor={r.estadoNovo} />
              </div>
              <p className="text-xs text-zinc-600 dark:text-zinc-400">
                {formatarData(r.dataHoraInicio)} até{" "}
                {r.dataHoraFim ? formatarData(r.dataHoraFim) : "agora"}
              </p>
              <div className="flex flex-wrap items-center gap-2 text-xs text-zinc-600 dark:text-zinc-400">
                <span>Duração: {duracao(r.dataHoraInicio, r.dataHoraFim)}</span>
                {!r.dataHoraFim && <EmAberto />}
              </div>
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
                <th className="px-3 py-2 font-medium">Máquina</th>
                <th className="px-3 py-2 font-medium">Transição</th>
                <th className="px-3 py-2 font-medium">Início</th>
                <th className="px-3 py-2 font-medium">Fim</th>
                <th className="px-3 py-2 font-medium">Duração</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((r) => (
                <tr key={r.id}>
                  <td className="px-3 py-2">{r.id}</td>
                  <td className="px-3 py-2">{r.maquinaNome}</td>
                  <td className="px-3 py-2">
                    <div className="flex items-center gap-2">
                      <Estado valor={r.estadoAnterior} />
                      <span className="text-zinc-400">→</span>
                      <Estado valor={r.estadoNovo} />
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-3 py-2 text-xs">
                    {formatarData(r.dataHoraInicio)}
                  </td>
                  <td className="whitespace-nowrap px-3 py-2 text-xs">
                    {r.dataHoraFim ? formatarData(r.dataHoraFim) : <EmAberto />}
                  </td>
                  <td className="whitespace-nowrap px-3 py-2 text-xs">
                    {duracao(r.dataHoraInicio, r.dataHoraFim)}
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
    </main>
  );
}