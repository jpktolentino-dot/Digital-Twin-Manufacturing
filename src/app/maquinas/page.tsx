"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { maquinasService } from "../services/maquina-service";
import { setoresService } from "../services/sectors-service";
import type { EstadoMaquina, Maquina, Setor } from "../types";

const ESTADOS: EstadoMaquina[] = ["OPERANDO", "PARADA", "FALHA", "MANUTENCAO"];

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700 cursor-pointer`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 bg-white dark:bg-background hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800 cursor-pointer`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950 cursor-pointer`;
const selectPequeno =
  "rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm dark:border-zinc-700 dark:bg-zinc-900";

const corEstado: Record<string, string> = {
  OPERANDO: "bg-green-100 text-green-700 font-semibold dark:bg-green-700 dark:text-green-200",
  PARADA: "bg-amber-100 text-amber-800 font-semibold dark:bg-amber-700 dark:text-amber-200",
  FALHA: "bg-red-100 text-red-700 font-semibold dark:bg-red-700 dark:text-red-200",
  MANUTENCAO: "bg-blue-100 text-blue-700 font-semibold dark:bg-blue-700 dark:text-blue-200",
};
const corPadrao = "bg-zinc-200 text-zinc-700 dark:bg-zinc-800 dark:text-zinc-300";

type Form = {
  nome: string;
  codigo: string;
  especificacoes: string;
  estadoAtual: EstadoMaquina;
  setorId: string;
  operadorId: string;
};

const FORM_VAZIO: Form = {
  nome: "",
  codigo: "",
  especificacoes: "",
  estadoAtual: "OPERANDO",
  setorId: "",
  operadorId: "",
};

function EstadoBadge({ valor }: { valor: string }) {
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${corEstado[valor] ?? corPadrao}`}>
      {valor}
    </span>
  );
}

function StatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-medium ${ativo
          ? "bg-green-100 text-green-700 font-semibold dark:bg-green-700 dark:text-green-200"
          : "bg-zinc-200 text-zinc-600 font-semibold dark:bg-zinc-800 dark:text-zinc-400"
        }`}
    >
      {ativo ? "Ativa" : "Inativa"}
    </span>
  );
}

function EstadoSelect({
  m,
  disabled,
  onChange,
}: {
  m: Maquina;
  disabled: boolean;
  onChange: (m: Maquina, estado: EstadoMaquina) => void;
}) {
  return (
    <select
      className={selectPequeno}
      value={m.estadoAtual}
      onChange={(e) => onChange(m, e.target.value as EstadoMaquina)}
      disabled={disabled}
    >
      {ESTADOS.map((s) => (
        <option key={s} value={s}>
          {s}
        </option>
      ))}
    </select>
  );
}

function Acoes({
  m,
  disabled,
  onEditar,
  onDesativar,
  onAtivar,
}: {
  m: Maquina;
  disabled: boolean;
  onEditar: (m: Maquina) => void;
  onDesativar: (m: Maquina) => void;
  onAtivar: (m: Maquina) => void;
}) {
  return (
    <div className="flex flex-wrap gap-2">
      <button onClick={() => onEditar(m)} disabled={disabled} className={btnSecundario}>
        Editar
      </button>
      {m.ativo ? (
        <button onClick={() => onDesativar(m)} disabled={disabled} className={btnPerigo}>
          Desativar
        </button>
      ) : (
        <button onClick={() => onAtivar(m)} disabled={disabled} className={btnSecundario}>
          Reativar
        </button>
      )}
    </div>
  );
}

export default function MaquinasPage() {
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [setores, setSetores] = useState<Setor[]>([]);
  const [filtroSetor, setFiltroSetor] = useState("");
  const [filtroId, setFiltroId] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  // formulário compartilhado entre criar e editar
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [form, setForm] = useState<Form>(FORM_VAZIO);

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

  const buscarLista = useCallback(
    () =>
      filtroSetor
        ? maquinasService.listarPorSetor(Number(filtroSetor))
        : maquinasService.listar(),
    [filtroSetor]
  );

  const carregar = useCallback(
    () =>
      executar(async () => {
        setMaquinas(await buscarLista());
      }),
    [executar, buscarLista]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    setoresService.listar().then(setSetores).catch(() => { });
  }, []);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? maquinas.filter((m) => String(m.id) === termo) : maquinas;
  }, [maquinas, filtroId]);

  function atualizarCampo<K extends keyof Form>(campo: K, valor: Form[K]) {
    setForm((f) => ({ ...f, [campo]: valor }));
  }

  function iniciarEdicao(m: Maquina) {
    setEditandoId(m.id);
    setForm({
      nome: m.nome,
      codigo: m.codigo,
      especificacoes: m.especificacoes ?? "",
      estadoAtual: m.estadoAtual,
      setorId: String(m.setor.id),
      operadorId: m.operadorId != null ? String(m.operadorId) : "",
    });
  }

  function cancelarEdicao() {
    setEditandoId(null);
    setForm(FORM_VAZIO);
  }

  function salvar(e: React.FormEvent) {
    e.preventDefault();
    const dados = {
      nome: form.nome,
      codigo: form.codigo,
      especificacoes: form.especificacoes,
      estadoAtual: form.estadoAtual,
      setorId: Number(form.setorId),
      operadorId: form.operadorId ? Number(form.operadorId) : null,
    };

    executar(async () => {
      if (editandoId) {
        await maquinasService.atualizar(editandoId, dados);
      } else {
        await maquinasService.criar(dados);
      }
      cancelarEdicao();
      setMaquinas(await buscarLista());
    }, editandoId ? "Máquina atualizada." : "Máquina cadastrada.");
  }

  function alterarEstado(m: Maquina, estado: EstadoMaquina) {
    if (estado === m.estadoAtual) return;
    executar(async () => {
      await maquinasService.alterarEstado(m.id, { estadoAtual: estado });
      setMaquinas(await buscarLista());
    }, `Estado de ${m.nome} alterado para ${estado}.`);
  }

  function desativar(m: Maquina) {
    executar(async () => {
      await maquinasService.desativar(m.id);
      setMaquinas(await buscarLista());
    }, `Máquina ${m.nome} desativada.`);
  }

  function ativar(m: Maquina) {
    executar(async () => {
      await maquinasService.ativar(m.id);
      setMaquinas(await buscarLista());
    }, `Máquina ${m.nome} reativada.`);
  }

  const vazio = filtroId ? "Nenhuma máquina com esse ID." : "Nenhuma máquina carregada.";

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Máquinas</h1>

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
              value={filtroSetor}
              onChange={(e) => setFiltroSetor(e.target.value)}
            >
              <option value="">Todos os setores</option>
              {setores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
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
          {visiveis.map((m) => (
            <li
              key={m.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate font-medium">{m.nome}</p>
                  <p className="truncate font-mono text-xs text-zinc-600 dark:text-zinc-400">
                    {m.codigo}
                  </p>
                </div>
                <span className="text-xs text-zinc-500">#{m.id}</span>
              </div>
              <p className="text-sm text-zinc-600 dark:text-zinc-400">
                Setor: {m.setor?.nome ?? "-"} · Operador: {m.operadorId ?? "-"}
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge ativo={m.ativo} />
                <EstadoSelect m={m} disabled={loading} onChange={alterarEstado} />
              </div>
              <Acoes
                m={m}
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
          <table className="w-full text-left bg-white dark:bg-zinc-950 text-sm">
            <thead className="bg-zinc-50 text-zinc-600 dark:bg-zinc-900 dark:text-zinc-400">
              <tr>
                <th className="px-3 py-2 font-medium">ID</th>
                <th className="px-3 py-2 font-medium">Nome</th>
                <th className="px-3 py-2 font-medium">Código</th>
                <th className="px-3 py-2 font-medium">Setor</th>
                <th className="px-3 py-2 font-medium">Estado</th>
                <th className="px-3 py-2 font-medium">Operador</th>
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((m) => (
                <tr key={m.id}>
                  <td className="px-3 py-2">{m.id}</td>
                  <td className="px-3 py-2">{m.nome}</td>
                  <td className="px-3 py-2 font-mono text-xs">{m.codigo}</td>
                  <td className="px-3 py-2">{m.setor?.nome ?? "-"}</td>
                  <td className="px-3 py-2">
                    <div className="flex items-center gap-2">
                      <EstadoBadge valor={m.estadoAtual} />
                      <EstadoSelect m={m} disabled={loading} onChange={alterarEstado} />
                    </div>
                  </td>
                  <td className="px-3 py-2">{m.operadorId ?? "-"}</td>
                  <td className="px-3 py-2">
                    <StatusBadge ativo={m.ativo} />
                  </td>
                  <td className="px-3 py-2">
                    <div className="flex justify-end">
                      <Acoes
                        m={m}
                        disabled={loading}
                        onEditar={iniciarEdicao}
                        onDesativar={desativar}
                        onAtivar={ativar}
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
          {editandoId ? `Editar máquina #${editandoId}` : "Cadastrar máquina"}
        </h2>
        <form onSubmit={salvar} className="grid w-full gap-3 ">
          <input
            className={input}
            placeholder="Nome"
            value={form.nome}
            onChange={(e) => atualizarCampo("nome", e.target.value)}
            required
          />
          <input
            className={input}
            placeholder="Código"
            value={form.codigo}
            onChange={(e) => atualizarCampo("codigo", e.target.value)}
            required
          />
          <textarea
            className={input}
            placeholder="Especificações"
            value={form.especificacoes}
            onChange={(e) => atualizarCampo("especificacoes", e.target.value)}
            rows={3}
          />
          <select
            className={input}
            value={form.estadoAtual}
            onChange={(e) => atualizarCampo("estadoAtual", e.target.value as EstadoMaquina)}
          >
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <select
            className={input}
            value={form.setorId}
            onChange={(e) => atualizarCampo("setorId", e.target.value)}
            required
          >
            <option value="">Selecione o setor</option>
            {setores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nome} ({s.codigo})
              </option>
            ))}
          </select>
          <input
            className={input}
            type="number"
            min={1}
            placeholder="ID do operador (opcional)"
            value={form.operadorId}
            onChange={(e) => atualizarCampo("operadorId", e.target.value)}
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