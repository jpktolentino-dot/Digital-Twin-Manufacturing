"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { ordensProducaoService } from "../services/production-order";
import { produtosService } from "../services/products-service";
import { maquinasService } from "../services/maquina-service";
import { usuariosService } from "../services/user-service";
import type { EstagioProducao, Maquina, OrdemProducao, Produto, Usuario } from "../types";

const TRANSICOES: Record<EstagioProducao, EstagioProducao[]> = {
  PLANEJADA: ["EM_PRODUCAO", "CANCELADA"],
  EM_PRODUCAO: ["PAUSADA", "CONCLUIDA", "CANCELADA"],
  PAUSADA: ["EM_PRODUCAO", "CANCELADA"],
  CONCLUIDA: [],
  CANCELADA: [],
};

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700 cursor-pointer`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 bg-white dark:bg-background hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800 cursor-pointer`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950 cursor-pointer`;
const selectPequeno =
  "rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-xs dark:border-zinc-700 dark:bg-zinc-900";

const corEstagio: Record<string, string> = {
  PLANEJADA: "bg-zinc-200 text-zinc-700 font-semibold dark:bg-zinc-800 dark:text-zinc-200",
  EM_PRODUCAO: "bg-blue-100 text-blue-700 font-semibold dark:bg-blue-700 dark:text-blue-200",
  PAUSADA: "bg-amber-100 text-amber-800 font-semibold dark:bg-amber-700 dark:text-amber-200",
  CONCLUIDA: "bg-green-100 text-green-700 font-semibold dark:bg-green-700 dark:text-green-200",
  CANCELADA: "bg-red-100 text-red-700 font-semibold dark:bg-red-700 dark:text-red-300",
};

function EstagioBadge({ valor }: { valor: string }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${corEstagio[valor] ?? corEstagio.PLANEJADA
        }`}
    >
      {valor}
    </span>
  );
}

function EstagioSelect({
  o,
  disabled,
  onChange,
}: {
  o: OrdemProducao;
  disabled: boolean;
  onChange: (o: OrdemProducao, estagio: EstagioProducao) => void;
}) {
  const opcoes = TRANSICOES[o.estagioAtual] ?? [];
  return (
    <select
      className={selectPequeno}
      value={o.estagioAtual}
      onChange={(e) => onChange(o, e.target.value as EstagioProducao)}
      disabled={disabled || opcoes.length === 0}
    >
      {[o.estagioAtual, ...opcoes].map((s) => (
        <option key={s} value={s}>
          {s}
        </option>
      ))}
    </select>
  );
}

function Acoes({
  o,
  disabled,
  onEditar,
  onCancelar,
}: {
  o: OrdemProducao;
  disabled: boolean;
  onEditar: (o: OrdemProducao) => void;
  onCancelar: (o: OrdemProducao) => void;
}) {
  const planejada = o.estagioAtual === "PLANEJADA";
  const encerrada = o.estagioAtual === "CANCELADA" || o.estagioAtual === "CONCLUIDA";
  return (
    <div className="flex flex-wrap gap-2">
      <button
        onClick={() => onEditar(o)}
        disabled={disabled || !planejada}
        title={planejada ? "" : "Só é possível editar ordens planejadas"}
        className={btnSecundario}
      >
        Editar
      </button>
      <button onClick={() => onCancelar(o)} disabled={disabled || encerrada} className={btnPerigo}>
        Cancelar
      </button>
    </div>
  );
}

export default function OrdensProducaoPage() {
  const [ordens, setOrdens] = useState<OrdemProducao[]>([]);
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);

  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // filtro por ID
  const [filtroId, setFiltroId] = useState("");

  // formulário compartilhado entre criar e editar
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [numero, setNumero] = useState("");
  const [quantidade, setQuantidade] = useState("");
  const [produtoId, setProdutoId] = useState("");
  const [operadorId, setOperadorId] = useState("");
  const [maquinasIds, setMaquinasIds] = useState<number[]>([]);

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
        setOrdens(await ordensProducaoService.listar());
      }),
    [executar]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  // dados dos selects
  useEffect(() => {
    produtosService.listar().then(setProdutos).catch(() => { });
    maquinasService.listar().then(setMaquinas).catch(() => { });
    usuariosService.listar().then(setUsuarios).catch(() => { });
  }, []);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? ordens.filter((o) => String(o.id) === termo) : ordens;
  }, [ordens, filtroId]);

  const nomeMaquina = (id: number) => maquinas.find((m) => m.id === id)?.nome ?? `#${id}`;
  const maquinasTexto = (o: OrdemProducao) =>
    o.maquinasIds?.length ? o.maquinasIds.map(nomeMaquina).join(", ") : "-";

  function alternarMaquina(id: number) {
    setMaquinasIds((atual) =>
      atual.includes(id) ? atual.filter((x) => x !== id) : [...atual, id]
    );
  }

  function limparForm() {
    setEditandoId(null);
    setNumero("");
    setQuantidade("");
    setProdutoId("");
    setOperadorId("");
    setMaquinasIds([]);
  }

  function iniciarEdicao(o: OrdemProducao) {
    setEditandoId(o.id);
    setNumero(o.numero);
    setQuantidade(String(o.quantidadePlanejada));
    setProdutoId(String(o.produtoId));
    setOperadorId(String(o.operadorId));
    setMaquinasIds(o.maquinasIds ?? []);
  }

  function salvar(e: React.FormEvent) {
    e.preventDefault();
    const dados = {
      numero,
      quantidadePlanejada: Number(quantidade),
      produtoId: Number(produtoId),
      operadorId: Number(operadorId),
      maquinasIds,
    };

    executar(async () => {
      if (editandoId) {
        await ordensProducaoService.atualizar(editandoId, dados);
      } else {
        await ordensProducaoService.criar(dados);
      }
      limparForm();
      setOrdens(await ordensProducaoService.listar());
    }, editandoId ? "Ordem atualizada." : "Ordem criada.");
  }

  function alterarEstagio(o: OrdemProducao, estagio: EstagioProducao) {
    if (estagio === o.estagioAtual) return;
    executar(async () => {
      await ordensProducaoService.alterarEstagio(o.id, { estagioAtual: estagio });
      setOrdens(await ordensProducaoService.listar());
    }, `Ordem ${o.numero} alterada para ${estagio}.`);
  }

  function cancelar(o: OrdemProducao) {
    executar(async () => {
      await ordensProducaoService.cancelar(o.id);
      setOrdens(await ordensProducaoService.listar());
    }, `Ordem ${o.numero} cancelada.`);
  }

  const vazio = filtroId ? "Nenhuma ordem com esse ID." : "Nenhuma ordem carregada.";
  const operadores = usuarios.filter((u) => u.ativo && u.tipo === "OPERADOR");
  const maquinasAtivas = maquinas.filter((m) => m.ativo);

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100 overflow-y-auto">
      <h1 className="text-xl font-semibold sm:text-2xl">Ordens de produção</h1>

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
          {visiveis.map((o) => (
            <li
              key={o.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate font-mono text-sm font-medium">{o.numero}</p>
                  <p className="truncate text-sm text-zinc-600 dark:text-zinc-400">
                    {o.produtoNome} · {o.quantidadePlanejada} un.
                  </p>
                </div>
                <span className="text-xs text-zinc-500">#{o.id}</span>
              </div>
              <p className="text-sm text-zinc-600 dark:text-zinc-400">
                Operador: {o.operadorNome}
              </p>
              <p className="text-sm text-zinc-600 dark:text-zinc-400">
                Máquinas: {maquinasTexto(o)}
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <EstagioBadge valor={o.estagioAtual} />
                <EstagioSelect o={o} disabled={loading} onChange={alterarEstagio} />
              </div>
              <Acoes o={o} disabled={loading} onEditar={iniciarEdicao} onCancelar={cancelar} />
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
          <table className="w-full text-left bg-white dark:bg-zinc-950 text-sm">
            <thead className="bg-zinc-100 text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
              <tr>
                <th className="px-3 py-2 font-medium">ID</th>
                <th className="px-3 py-2 font-medium">Número</th>
                <th className="px-3 py-2 font-medium">Produto</th>
                <th className="px-3 py-2 font-medium">Qtd.</th>
                <th className="px-3 py-2 font-medium">Operador</th>
                <th className="px-3 py-2 font-medium">Máquinas</th>
                <th className="px-3 py-2 font-medium">Estágio</th>
                <th className="px-3 py-2 font-medium text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((o) => (
                <tr key={o.id}>
                  <td className="px-3 py-2">{o.id}</td>
                  <td className="px-3 py-2 font-mono text-xs">{o.numero}</td>
                  <td className="px-3 py-2">{o.produtoNome}</td>
                  <td className="px-3 py-2">{o.quantidadePlanejada}</td>
                  <td className="px-3 py-2">{o.operadorNome}</td>
                  <td className="px-3 py-2 text-xs text-zinc-600 dark:text-zinc-400">
                    {maquinasTexto(o)}
                  </td>
                  <td className="px-3 py-2">
                    <div className="flex items-center gap-2">
                      <EstagioBadge valor={o.estagioAtual} />
                      <EstagioSelect o={o} disabled={loading} onChange={alterarEstagio} />
                    </div>
                  </td>
                  <td className="px-3 py-2">
                    <div className="flex justify-end">
                      <Acoes
                        o={o}
                        disabled={loading}
                        onEditar={iniciarEdicao}
                        onCancelar={cancelar}
                      />
                    </div>
                  </td>
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

      {/* CRIAR / EDITAR */}
      <section className="grid gap-3">
        <h2 className="text-lg font-medium">
          {editandoId ? `Editar ordem #${editandoId}` : "Criar ordem de produção"}
        </h2>
        <form onSubmit={salvar} className="grid w-full gap-3 ">
          <input
            className={input}
            placeholder="Número da ordem"
            value={numero}
            onChange={(e) => setNumero(e.target.value)}
            required
          />
          <input
            className={input}
            type="number"
            min={1}
            placeholder="Quantidade planejada"
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
            required
          />
          <select
            className={input}
            value={produtoId}
            onChange={(e) => setProdutoId(e.target.value)}
            required
          >
            <option value="">Selecione o produto</option>
            {produtos
              .filter((p) => p.ativo)
              .map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome} ({p.codigo})
                </option>
              ))}
          </select>
          <select
            className={input}
            value={operadorId}
            onChange={(e) => setOperadorId(e.target.value)}
            required
          >
            <option value="">Selecione o operador</option>
            {operadores.map((u) => (
              <option key={u.id} value={u.id}>
                {u.nome}
              </option>
            ))}
          </select>

          <fieldset className="grid gap-2 rounded-md border bg-white dark:bg-background border-zinc-300 p-3 dark:border-zinc-700">
            <legend className="px-1 text-sm text-zinc-600 dark:text-zinc-400">
              Máquinas (selecione ao menos uma)
            </legend>
            {maquinasAtivas.length === 0 && (
              <p className="text-sm text-zinc-500">Nenhuma máquina ativa disponível.</p>
            )}
            {maquinasAtivas.map((m) => (
              <label key={m.id} className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={maquinasIds.includes(m.id)}
                  onChange={() => alternarMaquina(m.id)}
                />
                {m.nome} <span className="text-xs text-zinc-500">({m.codigo})</span>
              </label>
            ))}
          </fieldset>

          <div className="flex gap-2">
            <button disabled={loading || maquinasIds.length === 0} className={btnPrimario}>
              {editandoId ? "Salvar" : "Criar"}
            </button>
            {editandoId && (
              <button type="button" onClick={limparForm} className={btnSecundario}>
                Cancelar edição
              </button>
            )}
          </div>
        </form>
      </section>
    </main >
  );
}