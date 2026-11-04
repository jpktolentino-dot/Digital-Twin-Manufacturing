"use client";

import { useCallback, useEffect, useState } from "react";
import { ordensProducaoService } from "../services/production-order";
import { produtosService } from "../services/products-service";
import { maquinasService } from "../services/maquina-service";
import { usuariosService } from "../services/user-service";
import type { EstagioProducao, Maquina, OrdemProducao, Produto, Usuario } from "../types";

// Confirme os valores reais do enum EstagioProducao no Swagger (Schemas)
const ESTAGIOS: EstagioProducao[] = ["PLANEJADA", "CONCLUIDA", "CANCELADA"];

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950`;

const corEstagio: Record<string, string> = {
  PLANEJADA: "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300",
  EM_ANDAMENTO: "bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300",
  CONCLUIDA: "bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300",
  CANCELADA: "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300",
};

export default function OrdensProducaoPage() {
  const [ordens, setOrdens] = useState<OrdemProducao[]>([]);
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);

  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const [idBusca, setIdBusca] = useState("");
  const [encontrada, setEncontrada] = useState<OrdemProducao | null>(null);

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
    produtosService.listar().then(setProdutos).catch(() => {});
    maquinasService.listar().then(setMaquinas).catch(() => {});
    usuariosService.listar().then(setUsuarios).catch(() => {});
  }, []);

  const nomeMaquina = (id: number) => maquinas.find((m) => m.id === id)?.nome ?? `#${id}`;

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
      // O nome do campo segue o padrão das máquinas (estadoAtual);
      // se o TypeScript reclamar, ajuste conforme AlterarEstagioRequest.
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

  function buscarPorId(e: React.FormEvent) {
    e.preventDefault();
    setEncontrada(null);
    executar(async () => {
      setEncontrada(await ordensProducaoService.buscarPorId(Number(idBusca)));
    });
  }

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-10 text-zinc-900 dark:text-zinc-100">
      <h1 className="text-2xl font-semibold">Ordens de produção</h1>

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
                <th className="px-3 py-2 font-medium">Número</th>
                <th className="px-3 py-2 font-medium">Produto</th>
                <th className="px-3 py-2 font-medium">Qtd.</th>
                <th className="px-3 py-2 font-medium">Operador</th>
                <th className="px-3 py-2 font-medium">Máquinas</th>
                <th className="px-3 py-2 font-medium">Estágio</th>
                <th className="px-3 py-2 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {ordens.map((o) => {
                const planejada = o.estagioAtual === "PLANEJADA";
                const encerrada = o.estagioAtual === "CANCELADA" || o.estagioAtual === "CONCLUIDA";
                return (
                  <tr key={o.id}>
                    <td className="px-3 py-2">{o.id}</td>
                    <td className="px-3 py-2 font-mono text-xs">{o.numero}</td>
                    <td className="px-3 py-2">{o.produtoNome}</td>
                    <td className="px-3 py-2">{o.quantidadePlanejada}</td>
                    <td className="px-3 py-2">{o.operadorNome}</td>
                    <td className="px-3 py-2 text-xs text-zinc-600 dark:text-zinc-400">
                      {o.maquinasIds?.length ? o.maquinasIds.map(nomeMaquina).join(", ") : "-"}
                    </td>
                    <td className="px-3 py-2">
                      <div className="flex items-center gap-2">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            corEstagio[o.estagioAtual] ?? corEstagio.PLANEJADA
                          }`}
                        >
                          {o.estagioAtual}
                        </span>
                        <select
                          className="rounded-md border border-zinc-300 bg-white px-1 py-1 text-xs dark:border-zinc-700 dark:bg-zinc-900"
                          value={o.estagioAtual}
                          onChange={(e) => alterarEstagio(o, e.target.value as EstagioProducao)}
                          disabled={loading || encerrada}
                        >
                          {ESTAGIOS.map((s) => (
                            <option key={s} value={s}>
                              {s}
                            </option>
                          ))}
                        </select>
                      </div>
                    </td>
                    <td className="flex gap-2 px-3 py-2">
                      <button
                        onClick={() => iniciarEdicao(o)}
                        disabled={loading || !planejada}
                        title={planejada ? "" : "Só é possível editar ordens planejadas"}
                        className={btnSecundario}
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => cancelar(o)}
                        disabled={loading || encerrada}
                        className={btnPerigo}
                      >
                        Cancelar
                      </button>
                    </td>
                  </tr>
                );
              })}
              {ordens.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-3 py-4 text-center text-zinc-500">
                    Nenhuma ordem carregada.
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
        <form onSubmit={salvar} className="grid max-w-md gap-3">
          <input className={input} placeholder="Número da ordem" value={numero} onChange={(e) => setNumero(e.target.value)} required />
          <input
            className={input}
            type="number"
            min={1}
            placeholder="Quantidade planejada"
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
            required
          />
          <select className={input} value={produtoId} onChange={(e) => setProdutoId(e.target.value)} required>
            <option value="">Selecione o produto</option>
            {produtos
              .filter((p) => p.ativo)
              .map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome} ({p.codigo})
                </option>
              ))}
          </select>
          <select className={input} value={operadorId} onChange={(e) => setOperadorId(e.target.value)} required>
            <option value="">Selecione o operador</option>
            {usuarios
              .filter((u) => u.ativo)
              .map((u) => (
                <option key={u.id} value={u.id}>
                  {u.nome} ({u.tipo})
                </option>
              ))}
          </select>

          <fieldset className="grid gap-2 rounded-md border border-zinc-300 p-3 dark:border-zinc-700">
            <legend className="px-1 text-sm text-zinc-600 dark:text-zinc-400">Máquinas</legend>
            {maquinas.filter((m) => m.ativo).length === 0 && (
              <p className="text-sm text-zinc-500">Nenhuma máquina ativa disponível.</p>
            )}
            {maquinas
              .filter((m) => m.ativo)
              .map((m) => (
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
            <button disabled={loading} className={btnPrimario}>
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
        {encontrada && (
          <pre className="overflow-x-auto rounded-md bg-zinc-100 p-3 text-xs dark:bg-zinc-900">
            {JSON.stringify(encontrada, null, 2)}
          </pre>
        )}
      </section>
    </main>
  );
}