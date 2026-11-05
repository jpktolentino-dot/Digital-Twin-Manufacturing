"use client";

import { useEffect, useMemo, useState } from "react";
import { Factory } from "lucide-react";
import { maquinasService } from "../services/maquina-service";
import { ordensProducaoService } from "../services/production-order";
import { produtosService } from "../services/products-service";
import { kpisService } from "../services/kpis-service";
import { historicosMaquinasService } from "../services/machine-history";
import { eventosService } from "../services/events-service";
import type {
  Evento,
  HistoricoMaquina,
  KpiMaquina,
  Maquina,
  OrdemProducao,
  Produto,
} from "../types";

const PERIODOS = [
  { label: "Últimas 24 horas", horas: 24 },
  { label: "Últimos 7 dias", horas: 24 * 7 },
  { label: "Últimos 30 dias", horas: 24 * 30 },
];

const corEstado: Record<string, string> = {
  OPERANDO: "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300",
  PARADA: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  FALHA: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300",
  MANUTENCAO: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
  INDISPONIVEL: "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300",
};
const corPadrao = "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300";

const corEstagio: Record<string, string> = {
  PLANEJADA: corPadrao,
  EM_PRODUCAO: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
  PAUSADA: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  CONCLUIDA: "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300",
  CANCELADA: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300",
};

const card =
  "rounded-2xl border border-zinc-300 bg-gray-100 p-4 dark:border-slate-600 dark:bg-slate-900";
const titulo = "mb-3 text-[1.1rem] font-bold";

function paraLocalDateTime(d: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return (
    `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}` +
    `T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  );
}

function Badge({ valor, cores }: { valor: string; cores: Record<string, string> }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${cores[valor] ?? corPadrao}`}
    >
      {valor}
    </span>
  );
}

function Linha({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-4 py-1.5 text-sm">
      <span className="text-zinc-500 dark:text-slate-400">{label}</span>
      <span className="text-right">{children}</span>
    </div>
  );
}

function formatarDuracao(segundos: number): string {
  const total = Math.max(0, Math.round(segundos));
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  return `${h}h ${m}min`;
}

const formatarData = (iso: string | null) =>
  iso ? new Date(iso).toLocaleString("pt-BR") : null;

export default function PainelMaquinaPage() {
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [ordens, setOrdens] = useState<OrdemProducao[]>([]);
  const [maquinaId, setMaquinaId] = useState("");
  const [horas, setHoras] = useState(24);

  const [kpi, setKpi] = useState<KpiMaquina | null>(null);
  const [historico, setHistorico] = useState<HistoricoMaquina[]>([]);
  const [eventos, setEventos] = useState<Evento[]>([]);
  const [produto, setProduto] = useState<Produto | null>(null);

  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [erroKpi, setErroKpi] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Dados base: máquinas e ordens
  useEffect(() => {
    Promise.all([maquinasService.listar(), ordensProducaoService.listar()])
      .then(([m, o]) => {
        setMaquinas(m);
        setOrdens(o);
        if (m.length > 0) setMaquinaId((atual) => atual || String(m[0].id));
      })
      .catch((e) => setErroGeral(e instanceof Error ? e.message : "Erro ao carregar dados"));
  }, []);

  const maquina = useMemo(
    () => maquinas.find((m) => m.id === Number(maquinaId)) ?? null,
    [maquinas, maquinaId]
  );

  // Ordens que usam esta máquina; a "atual" prioriza em produção > pausada > planejada
  const ordensDaMaquina = useMemo(
    () => ordens.filter((o) => maquina && o.maquinasIds?.includes(maquina.id)),
    [ordens, maquina]
  );
  const ordemAtual = useMemo(
    () =>
      ordensDaMaquina.find((o) => o.estagioAtual === "EM_PRODUCAO") ??
      ordensDaMaquina.find((o) => o.estagioAtual === "PAUSADA") ??
      ordensDaMaquina.find((o) => o.estagioAtual === "PLANEJADA") ??
      null,
    [ordensDaMaquina]
  );
  const produtoId = ordemAtual?.produtoId ?? null;

  // Dados que dependem da máquina e do período
  useEffect(() => {
    if (!maquina) return;
    let cancelado = false;
    const id = maquina.id;

    setLoading(true);
    setErroKpi(null);

    const fim = new Date(Date.now() - 60_000);
    const inicio = new Date(fim.getTime() - horas * 3600 * 1000);

    Promise.allSettled([
      kpisService.calcularPorMaquina(id, {
        inicio: paraLocalDateTime(inicio),
        fim: paraLocalDateTime(fim),
      }),
      historicosMaquinasService.listarPorMaquina(id),
      eventosService.listarPorMaquina(id),
    ]).then(([k, h, e]) => {
      if (cancelado) return;
      if (k.status === "fulfilled") setKpi(k.value);
      else {
        setKpi(null);
        setErroKpi(k.reason instanceof Error ? k.reason.message : "Erro ao calcular KPIs");
      }
      setHistorico(h.status === "fulfilled" ? h.value : []);
      setEventos(e.status === "fulfilled" ? e.value : []);
      setLoading(false);
    });

    return () => {
      cancelado = true;
    };
  }, [maquina, horas]);

  useEffect(() => {
    if (produtoId == null) {
      setProduto(null);
      return;
    }
    let cancelado = false;
    produtosService
      .buscarPorId(produtoId)
      .then((p) => !cancelado && setProduto(p))
      .catch(() => !cancelado && setProduto(null));
    return () => {
      cancelado = true;
    };
  }, [produtoId]);

  const ultimosHistoricos = useMemo(
    () =>
      [...historico]
        .sort((a, b) => b.dataHoraInicio.localeCompare(a.dataHoraInicio))
        .slice(0, 6),
    [historico]
  );
  const ultimosEventos = useMemo(
    () => [...eventos].sort((a, b) => b.dataHora.localeCompare(a.dataHora)).slice(0, 5),
    [eventos]
  );

  // Se o backend devolver 0..1 em vez de 0..100, normaliza só para exibir
  const disponibilidade = kpi
    ? kpi.disponibilidadePercentual <= 1 && kpi.disponibilidadePercentual > 0
      ? kpi.disponibilidadePercentual * 100
      : kpi.disponibilidadePercentual
    : null;

  const barras = kpi
    ? [
      { nome: "Operação", seg: kpi.tempoOperacaoSegundos, cor: "bg-green-500" },
      { nome: "Parada", seg: kpi.tempoParadaSegundos, cor: "bg-amber-500" },
      { nome: "Falha", seg: kpi.tempoFalhaSegundos, cor: "bg-red-500" },
      { nome: "Manutenção", seg: kpi.tempoManutencaoSegundos, cor: "bg-blue-500" },
      { nome: "Sem histórico", seg: kpi.tempoSemHistoricoSegundos, cor: "bg-zinc-300 dark:bg-zinc-700" },
    ]
    : [];
  const totalBarras = barras.reduce((s, b) => s + b.seg, 0);

  return (
    <div className="flex h-screen flex-col bg-white text-zinc-900 dark:bg-slate-950 dark:text-zinc-100">
      {/* Barra superior */}
      <header className="flex flex-wrap items-center gap-3 border-b-2 border-zinc-400 px-6 py-3 dark:border-slate-800">
        <h1 className="text-lg font-bold">Painel da máquina</h1>
        <select
          className="rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-slate-700 dark:bg-slate-900"
          value={maquinaId}
          onChange={(e) => setMaquinaId(e.target.value)}
        >
          {maquinas.length === 0 && <option value="">Nenhuma máquina</option>}
          {maquinas.map((m) => (
            <option key={m.id} value={m.id}>
              {m.nome} ({m.codigo})
            </option>
          ))}
        </select>
        <select
          className="rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-slate-700 dark:bg-slate-900"
          value={horas}
          onChange={(e) => setHoras(Number(e.target.value))}
        >
          {PERIODOS.map((p) => (
            <option key={p.horas} value={p.horas}>
              {p.label}
            </option>
          ))}
        </select>
        {loading && <span className="text-xs text-zinc-500">Atualizando...</span>}
        {erroGeral && <span className="text-sm text-red-600">Erro: {erroGeral}</span>}
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* COLUNA ESQUERDA: máquina + KPIs */}
        <aside className="w-[380px] shrink-0 space-y-6 overflow-y-auto border-r-2 border-zinc-400 p-6 dark:border-slate-800">
          <div>
            <h2 className="text-[1.3rem] font-bold">{maquina?.nome ?? "Nome da máquina"}</h2>
            <p className="text-zinc-400">{maquina ? `ID ${maquina.id}` : "Id da máquina"}</p>
          </div>

          {maquina && (
            <div className={card}>
              <Linha label="Código">{maquina.codigo}</Linha>
              <Linha label="Estado">
                <Badge valor={maquina.estadoAtual} cores={corEstado} />
              </Linha>
              <Linha label="Setor">{maquina.setor?.nome ?? "-"}</Linha>
              <Linha label="Operador (ID)">{maquina.operadorId ?? "-"}</Linha>
              <Linha label="Ativa">{maquina.ativo ? "Sim" : "Não"}</Linha>
              <Linha label="Especificações">{maquina.especificacoes || "-"}</Linha>
            </div>
          )}

          <div>
            <h3 className={titulo}>KPIs</h3>
            {erroKpi && (
              <p className="mb-2 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
                {erroKpi}
              </p>
            )}
            {kpi ? (
              <div className={`${card} space-y-4`}>
                <div>
                  <p className="text-sm text-zinc-500 dark:text-slate-400">Disponibilidade</p>
                  <p className="text-3xl font-bold">
                    {disponibilidade != null ? `${disponibilidade.toFixed(1)}%` : "-"}
                  </p>
                </div>

                {totalBarras > 0 && (
                  <div className="flex h-3 w-full overflow-hidden rounded-full bg-zinc-200 dark:bg-slate-800">
                    {barras.map((b) => (
                      <div
                        key={b.nome}
                        className={b.cor}
                        style={{ width: `${(b.seg / totalBarras) * 100}%` }}
                        title={`${b.nome}: ${formatarDuracao(b.seg)}`}
                      />
                    ))}
                  </div>
                )}

                <div>
                  {barras.map((b) => (
                    <div key={b.nome} className="flex items-center justify-between py-1 text-sm">
                      <span className="flex items-center gap-2">
                        <span className={`h-2.5 w-2.5 rounded-full ${b.cor}`} />
                        {b.nome}
                      </span>
                      <span className="text-zinc-600 dark:text-slate-400">
                        {formatarDuracao(b.seg)}
                      </span>
                    </div>
                  ))}
                  <div className="mt-1 flex items-center justify-between border-t border-zinc-300 pt-2 text-sm font-medium dark:border-slate-700">
                    <span>Tempo monitorado</span>
                    <span>{formatarDuracao(kpi.tempoMonitoradoSegundos)}</span>
                  </div>
                </div>
              </div>
            ) : (
              !erroKpi && <p className="text-sm text-zinc-500">Sem dados de KPI.</p>
            )}
          </div>
        </aside>

        {/* CENTRO: área 3D */}
        <section className="relative flex flex-1 items-center justify-center overflow-hidden bg-zinc-300 dark:bg-slate-900">
          <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] opacity-20" />
          <div className="relative z-10 text-center">
            <div className="mb-6 flex flex-col items-center space-y-5 rounded-2xl border-2 border-zinc-400 bg-zinc-100 p-12 shadow-2xl dark:border-slate-700 dark:bg-slate-800">
              <Factory size={110} />
              <div>
                <h2 className="mb-2 text-2xl text-zinc-900 dark:text-white">Área de integração 3D</h2>
                <p className="text-base text-zinc-400 dark:text-slate-400">
                  Acompanhe a máquina em tempo real
                </p>
              </div>
              {maquina && <Badge valor={maquina.estadoAtual} cores={corEstado} />}
            </div>
            <div className="flex items-center justify-center gap-3">
              <div className="h-3 w-3 animate-pulse rounded-full bg-blue-500" />
              <p className="text-sm text-slate-600 dark:text-slate-500">Pronto para mostrar a imagem</p>
            </div>
          </div>
        </section>

        {/* COLUNA DIREITA: ordem, produto, histórico, eventos */}
        <aside className="w-[380px] shrink-0 space-y-6 overflow-y-auto border-l-2 border-zinc-400 p-6 dark:border-slate-800">
          <div>
            <h3 className={titulo}>Ordem de produção</h3>
            {ordemAtual ? (
              <div className={card}>
                <Linha label="Número">{ordemAtual.numero}</Linha>
                <Linha label="Estágio atual">
                  <Badge valor={ordemAtual.estagioAtual} cores={corEstagio} />
                </Linha>
                <Linha label="Produto">{ordemAtual.produtoNome}</Linha>
                <Linha label="Quantidade">{ordemAtual.quantidadePlanejada}</Linha>
                <Linha label="Operador">{ordemAtual.operadorNome}</Linha>
                {ordensDaMaquina.length > 1 && (
                  <p className="mt-2 text-xs text-zinc-500">
                    Esta máquina participa de {ordensDaMaquina.length} ordens no total.
                  </p>
                )}
              </div>
            ) : (
              <p className="text-sm text-zinc-500">Nenhuma ordem ativa para esta máquina.</p>
            )}
          </div>

          <div>
            <h3 className={titulo}>Produto</h3>
            {produto ? (
              <div className={card}>
                <Linha label="Id">{produto.id}</Linha>
                <Linha label="Nome">{produto.nome}</Linha>
                <Linha label="Código">{produto.codigo}</Linha>
                <Linha label="Especificações">{produto.especificacoes || "-"}</Linha>
              </div>
            ) : (
              <p className="text-sm text-zinc-500">Sem produto associado.</p>
            )}
          </div>

          <div>
            <h3 className={titulo}>Histórico de estados</h3>
            {ultimosHistoricos.length > 0 ? (
              <ul className="space-y-2">
                {ultimosHistoricos.map((h) => (
                  <li key={h.id} className={`${card} p-3 text-sm`}>
                    <div className="flex items-center gap-2">
                      <Badge valor={h.estadoAnterior} cores={corEstado} />
                      <span className="text-zinc-400">→</span>
                      <Badge valor={h.estadoNovo} cores={corEstado} />
                    </div>
                    <p className="mt-1 text-xs text-zinc-500 dark:text-slate-400">
                      {formatarData(h.dataHoraInicio)} até{" "}
                      {formatarData(h.dataHoraFim) ?? "agora"}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-zinc-500">Sem registros de histórico.</p>
            )}
          </div>

          <div>
            <h3 className={titulo}>Eventos recentes</h3>
            {ultimosEventos.length > 0 ? (
              <ul className="space-y-2">
                {ultimosEventos.map((ev) => (
                  <li key={ev.id} className={`${card} p-3 text-sm`}>
                    <div className="flex items-center justify-between gap-2">
                      <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800 dark:bg-amber-950 dark:text-amber-300">
                        {ev.categoria}
                      </span>
                      <span className="text-xs text-zinc-500">{formatarData(ev.dataHora)}</span>
                    </div>
                    <p className="mt-1">{ev.descricao}</p>
                    <p className="mt-1 text-xs text-zinc-500 dark:text-slate-400">
                      Ordem {ev.numeroOrdem} · {ev.usuarioNome} · impacto {ev.impactoEstimado}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-zinc-500">Nenhum evento registrado.</p>
            )}
          </div>
        </aside>
      </div>
    </div>
  );
}