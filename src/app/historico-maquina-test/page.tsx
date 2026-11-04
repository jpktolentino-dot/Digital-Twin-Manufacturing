"use client";

import { useCallback, useEffect, useState } from "react";
import { historicosMaquinasService } from "../services/machine-history";
import { maquinasService } from "../services/maquina-service";
import type { HistoricoMaquina, Maquina } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;

// Cores por estado (valores fora da lista caem no padrão)
const corEstado: Record<string, string> = {
  OPERANDO: "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300",
  PARADA: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  FALHA: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300",
  MANUTENCAO: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
  INDISPONIVEL: "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300",
};
const corPadrao = "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300";

function Estado({ valor }: { valor: string }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${corEstado[valor] ?? corPadrao}`}
    >
      {valor}
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
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const [idBusca, setIdBusca] = useState("");
  const [encontrado, setEncontrado] = useState<HistoricoMaquina | null>(null);

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

  function buscarPorId(e: React.FormEvent) {
    e.preventDefault();
    setEncontrado(null);
    executar(async () => {
      setEncontrado(await historicosMaquinasService.buscarPorId(Number(idBusca)));
    });
  }

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-10 text-zinc-900 dark:text-zinc-100">
      <h1 className="text-2xl font-semibold">Histórico de máquinas</h1>

      {erro && (
        <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
          Erro: {erro}
        </p>
      )}

      {/* LISTAR / FILTRAR POR MÁQUINA */}
      <section className="grid gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-lg font-medium">Registros</h2>
          <select
            className="rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-zinc-700 dark:bg-zinc-900"
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
          <button onClick={carregar} disabled={loading} className={btnSecundario}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>
        </div>

        <div className="overflow-x-auto rounded-lg border border-zinc-200 dark:border-zinc-800">
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
              {registros.map((r) => (
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
                    {formatarData(r.dataHoraFim) ?? (
                      <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-700 dark:bg-green-950 dark:text-green-300">
                        em aberto
                      </span>
                    )}
                  </td>
                  <td className="whitespace-nowrap px-3 py-2 text-xs">
                    {duracao(r.dataHoraInicio, r.dataHoraFim)}
                  </td>
                </tr>
              ))}
              {registros.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-4 text-center text-zinc-500">
                    Nenhum registro encontrado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
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