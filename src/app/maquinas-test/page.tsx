"use client";

import { useCallback, useEffect, useState } from "react";
import { maquinasService } from "../services/maquina-service";
import { setoresService } from "../services/sectors-service";
import type { EstadoMaquina, Maquina, Setor } from "../types";

// Confirme os valores reais do enum EstadoMaquina no Swagger (Schemas)
const ESTADOS: EstadoMaquina[] = ["OPERANDO", "PARADA", "FALHA", "MANUTENCAO"];

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

export default function MaquinasPage() {
  const [maquinas, setMaquinas] = useState<Maquina[]>([]);
  const [setores, setSetores] = useState<Setor[]>([]);
  const [filtroSetor, setFiltroSetor] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const [idBusca, setIdBusca] = useState("");
  const [encontrada, setEncontrada] = useState<Maquina | null>(null);

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
    setoresService.listar().then(setSetores).catch(() => {});
  }, []);

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

  function buscarPorId(e: React.FormEvent) {
    e.preventDefault();
    setEncontrada(null);
    executar(async () => {
      setEncontrada(await maquinasService.buscarPorId(Number(idBusca)));
    });
  }

  const celula = { padding: "6px 10px", borderBottom: "1px solid #ddd", textAlign: "left" as const };

  return (
    <main style={{ maxWidth: 1000, margin: "40px auto", display: "grid", gap: 20, padding: "0 16px" }}>
      <h1>Máquinas</h1>

      {erro && <p style={{ color: "crimson" }}>Erro: {erro}</p>}
      {aviso && <p style={{ color: "green" }}>{aviso}</p>}

      {/* LISTAR / FILTRAR POR SETOR */}
      <section style={{ display: "grid", gap: 8 }}>
        <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
          <h2 style={{ margin: 0 }}>Lista</h2>
          <select value={filtroSetor} onChange={(e) => setFiltroSetor(e.target.value)}>
            <option value="">Todos os setores</option>
            {setores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nome}
              </option>
            ))}
          </select>
          <button onClick={carregar} disabled={loading}>
            {loading ? "Carregando..." : "Recarregar"}
          </button>
        </div>

        <div style={{ overflowX: "auto" }}>
          <table style={{ borderCollapse: "collapse", width: "100%" }}>
            <thead>
              <tr>
                <th style={celula}>ID</th>
                <th style={celula}>Nome</th>
                <th style={celula}>Código</th>
                <th style={celula}>Setor</th>
                <th style={celula}>Estado</th>
                <th style={celula}>Operador</th>
                <th style={celula}>Ativa</th>
                <th style={celula}>Ações</th>
              </tr>
            </thead>
            <tbody>
              {maquinas.map((m) => (
                <tr key={m.id}>
                  <td style={celula}>{m.id}</td>
                  <td style={celula}>{m.nome}</td>
                  <td style={celula}>{m.codigo}</td>
                  <td style={celula}>{m.setor?.nome}</td>
                  <td style={celula}>
                    <select
                      value={m.estadoAtual}
                      onChange={(e) => alterarEstado(m, e.target.value as EstadoMaquina)}
                      disabled={loading}
                    >
                      {ESTADOS.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td style={celula}>{m.operadorId ?? "-"}</td>
                  <td style={celula}>{m.ativo ? "Sim" : "Não"}</td>
                  <td style={{ ...celula, display: "flex", gap: 6 }}>
                    <button onClick={() => iniciarEdicao(m)} disabled={loading}>
                      Editar
                    </button>
                    {m.ativo ? (
                      <button onClick={() => desativar(m)} disabled={loading}>
                        Desativar
                      </button>
                    ) : (
                      <button onClick={() => ativar(m)} disabled={loading}>
                        Reativar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {maquinas.length === 0 && (
                <tr>
                  <td style={celula} colSpan={8}>
                    Nenhuma máquina carregada.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* CRIAR / EDITAR */}
      <section style={{ display: "grid", gap: 8 }}>
        <h2 style={{ margin: 0 }}>
          {editandoId ? `Editar máquina #${editandoId}` : "Cadastrar máquina"}
        </h2>
        <form onSubmit={salvar} style={{ display: "grid", gap: 8, maxWidth: 420 }}>
          <input placeholder="Nome" value={form.nome} onChange={(e) => atualizarCampo("nome", e.target.value)} required />
          <input placeholder="Código" value={form.codigo} onChange={(e) => atualizarCampo("codigo", e.target.value)} required />
          <textarea
            placeholder="Especificações"
            value={form.especificacoes}
            onChange={(e) => atualizarCampo("especificacoes", e.target.value)}
            rows={3}
          />
          <select
            value={form.estadoAtual}
            onChange={(e) => atualizarCampo("estadoAtual", e.target.value as EstadoMaquina)}
          >
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <select value={form.setorId} onChange={(e) => atualizarCampo("setorId", e.target.value)} required>
            <option value="">Selecione o setor</option>
            {setores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nome} ({s.codigo})
              </option>
            ))}
          </select>
          <input
            type="number"
            min={1}
            placeholder="ID do operador (opcional)"
            value={form.operadorId}
            onChange={(e) => atualizarCampo("operadorId", e.target.value)}
          />
          <div style={{ display: "flex", gap: 8 }}>
            <button disabled={loading}>{editandoId ? "Salvar" : "Cadastrar"}</button>
            {editandoId && (
              <button type="button" onClick={cancelarEdicao}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </section>

      {/* BUSCAR POR ID */}
      <section style={{ display: "grid", gap: 8 }}>
        <h2 style={{ margin: 0 }}>Buscar por ID</h2>
        <form onSubmit={buscarPorId} style={{ display: "flex", gap: 8 }}>
          <input
            type="number"
            min={1}
            placeholder="ID"
            value={idBusca}
            onChange={(e) => setIdBusca(e.target.value)}
            required
          />
          <button disabled={loading}>Buscar</button>
        </form>
        {encontrada && <pre>{JSON.stringify(encontrada, null, 2)}</pre>}
      </section>
    </main>
  );
}