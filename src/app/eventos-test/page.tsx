"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { eventosService } from "../services/events-service";
import { maquinasService } from "../services/maquina-service";
import { ordensProducaoService } from "../services/production-order";
import { usuariosService } from "../services/user-service";
import type { Evento, Maquina, OrdemProducao, Usuario } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;
const selectPequeno =
  "rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-zinc-700 dark:bg-zinc-900";

type Filtro = "todos" | "maquina" | "ordem";

// Valor inicial do <input type="datetime-local"> no horário local
function agoraLocal(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

const formatarData = (iso: string) => new Date(iso).toLocaleString("pt-BR");

function CategoriaBadge({ valor }: { valor: string }) {
  return (
    <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800 dark:bg-amber-950 dark:text-amber-300">
      {valor}
    </span>
  );
}

export default function EventosPage() {
  const [eventos, setEventos] = useState<Evento[]>([]);
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [ordens, setOrdens] = useState<OrdemProducao[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);

  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // filtros
  const [filtro, setFiltro] = useState<Filtro>("todos");
  const [filtroRef, setFiltroRef] = useState("");
  const [filtroId, setFiltroId] = useState("");

  // formulário de registro
  const [categoria, setCategoria] = useState("");
  const [descricao, setDescricao] = useState("");
  const [impacto, setImpacto] = useState("");
  const [dataHora, setDataHora] = useState(agoraLocal);
  const [maquinaId, setMaquinaId] = useState("");
  const [ordemId, setOrdemId] = useState("");
  const [usuarioId, setUsuarioId] = useState("");

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

  const buscarLista = useCallback(() => {
    if (filtro === "maquina" && filtroRef) {
      return eventosService.listarPorMaquina(Number(filtroRef));
    }
    if (filtro === "ordem" && filtroRef) {
      return eventosService.listarPorOrdemProducao(Number(filtroRef));
    }
    return eventosService.listar();
  }, [filtro, filtroRef]);

  const carregar = useCallback(
    () =>
      executar(async () => {
        setEventos(await buscarLista());
      }),
    [executar, buscarLista]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  // dados dos selects
  useEffect(() => {
    maquinasService.listar().then(setMaquinas).catch(() => {});
    ordensProducaoService.listar().then(setOrdens).catch(() => {});
    usuariosService.listar().then(setUsuarios).catch(() => {});
  }, []);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? eventos.filter((ev) => String(ev.id) === termo) : eventos;
  }, [eventos, filtroId]);

  function trocarFiltro(novo: Filtro) {
    setFiltro(novo);
    setFiltroRef("");
  }

  function limparForm() {
    setCategoria("");
    setDescricao("");
    setImpacto("");
    setDataHora(agoraLocal());
    setMaquinaId("");
    setOrdemId("");
    setUsuarioId("");
  }

  function registrar(e: React.FormEvent) {
    e.preventDefault();
    executar(async () => {
      await eventosService.criar({
        categoria,
        descricao,
        impactoEstimado: Number(impacto),
        dataHora: new Date(dataHora).toISOString(),
        maquinaId: Number(maquinaId),
        ordemProducaoId: Number(ordemId),
        usuarioId: Number(usuarioId),
      });
      limparForm();
      setEventos(await buscarLista());
    }, "Evento registrado.");
  }

  const vazio = filtroId ? "Nenhum evento com esse ID." : "Nenhum evento carregado.";

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Eventos industriais</h1>

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

      {/* FILTROS + LISTA */}
      <section className="grid gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-lg font-medium">Lista</h2>
          <button onClick={carregar} disabled={loading} className={btnSecundario}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>

          <div className="flex w-full flex-wrap gap-2 sm:ml-auto sm:w-auto">
            <select
              className={`${selectPequeno} w-full sm:w-auto`}
              value={filtro}
              onChange={(e) => trocarFiltro(e.target.value as Filtro)}
            >
              <option value="todos">Todos os eventos</option>
              <option value="maquina">Por máquina</option>
              <option value="ordem">Por ordem de produção</option>
            </select>

            {filtro === "maquina" && (
              <select
                className={`${selectPequeno} w-full sm:w-auto`}
                value={filtroRef}
                onChange={(e) => setFiltroRef(e.target.value)}
              >
                <option value="">Selecione a máquina</option>
                {maquinas.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.nome}
                  </option>
                ))}
              </select>
            )}

            {filtro === "ordem" && (
              <select
                className={`${selectPequeno} w-full sm:w-auto`}
                value={filtroRef}
                onChange={(e) => setFiltroRef(e.target.value)}
              >
                <option value="">Selecione a ordem</option>
                {ordens.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.numero}
                  </option>
                ))}
              </select>
            )}

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
          {visiveis.map((ev) => (
            <li
              key={ev.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <CategoriaBadge valor={ev.categoria} />
                <span className="text-xs text-zinc-500">#{ev.id}</span>
              </div>
              <p className="text-sm">{ev.descricao}</p>
              <p className="text-xs text-zinc-600 dark:text-zinc-400">
                {formatarData(ev.dataHora)} · impacto {ev.impactoEstimado}
              </p>
              <p className="text-xs text-zinc-600 dark:text-zinc-400">
                {ev.maquinaNome} · ordem {ev.numeroOrdem} · {ev.usuarioNome}
              </p>
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
                <th className="px-3 py-2 font-medium">Data/hora</th>
                <th className="px-3 py-2 font-medium">Categoria</th>
                <th className="px-3 py-2 font-medium">Descrição</th>
                <th className="px-3 py-2 font-medium">Impacto</th>
                <th className="px-3 py-2 font-medium">Máquina</th>
                <th className="px-3 py-2 font-medium">Ordem</th>
                <th className="px-3 py-2 font-medium">Usuário</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((ev) => (
                <tr key={ev.id}>
                  <td className="px-3 py-2">{ev.id}</td>
                  <td className="whitespace-nowrap px-3 py-2 text-xs">{formatarData(ev.dataHora)}</td>
                  <td className="px-3 py-2">
                    <CategoriaBadge valor={ev.categoria} />
                  </td>
                  <td
                    className="max-w-xs truncate px-3 py-2 text-zinc-600 dark:text-zinc-400"
                    title={ev.descricao}
                  >
                    {ev.descricao}
                  </td>
                  <td className="px-3 py-2">{ev.impactoEstimado}</td>
                  <td className="px-3 py-2">{ev.maquinaNome}</td>
                  <td className="px-3 py-2 font-mono text-xs">{ev.numeroOrdem}</td>
                  <td className="px-3 py-2">{ev.usuarioNome}</td>
                </tr>
              ))}
              {visiveis.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-3 py-4 text-center text-zinc-500">
                    {vazio}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* REGISTRAR */}
      <section className="grid gap-3">
        <h2 className="text-lg font-medium">Registrar evento</h2>
        <form onSubmit={registrar} className="grid w-full gap-3 sm:max-w-md">
          <input
            className={input}
            placeholder="Categoria (ex.: FALHA, PARADA, QUALIDADE)"
            value={categoria}
            onChange={(e) => setCategoria(e.target.value)}
            required
          />
          <textarea
            className={input}
            placeholder="Descrição"
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            rows={3}
            required
          />
          <input
            className={input}
            type="number"
            step="any"
            placeholder="Impacto estimado"
            value={impacto}
            onChange={(e) => setImpacto(e.target.value)}
            required
          />
          <label className="grid gap-1 text-sm text-zinc-600 dark:text-zinc-400">
            Data e hora
            <input
              className={input}
              type="datetime-local"
              value={dataHora}
              onChange={(e) => setDataHora(e.target.value)}
              required
            />
          </label>
          <select
            className={input}
            value={maquinaId}
            onChange={(e) => setMaquinaId(e.target.value)}
            required
          >
            <option value="">Selecione a máquina</option>
            {maquinas
              .filter((m) => m.ativo)
              .map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nome} ({m.codigo})
                </option>
              ))}
          </select>
          <select
            className={input}
            value={ordemId}
            onChange={(e) => setOrdemId(e.target.value)}
            required
          >
            <option value="">Selecione a ordem de produção</option>
            {ordens.map((o) => (
              <option key={o.id} value={o.id}>
                {o.numero} - {o.produtoNome}
              </option>
            ))}
          </select>
          <select
            className={input}
            value={usuarioId}
            onChange={(e) => setUsuarioId(e.target.value)}
            required
          >
            <option value="">Selecione o usuário</option>
            {usuarios
              .filter((u) => u.ativo)
              .map((u) => (
                <option key={u.id} value={u.id}>
                  {u.nome} ({u.tipo})
                </option>
              ))}
          </select>
          <div className="flex gap-2">
            <button disabled={loading} className={btnPrimario}>
              Registrar
            </button>
            <button type="button" onClick={limparForm} className={btnSecundario}>
              Limpar
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}