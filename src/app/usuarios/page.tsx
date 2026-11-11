"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { usuariosService } from "../services/user-service";
import type { TipoUsuario, Usuario } from "../types";

const input =
  "w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100";
const btn =
  "rounded-md px-3 py-1.5 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50";
const btnPrimario = `${btn} bg-blue-600 text-white hover:bg-blue-700 cursor-pointer`;
const btnSecundario = `${btn} border border-zinc-300 text-zinc-700 bg-white dark:bg-background hover:bg-zinc-50 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800 cursor-pointer`;
const btnPerigo = `${btn} border border-red-300 text-red-700 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950 cursor-pointer`;

function StatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${ativo
        ? "bg-green-100 text-green-700 dark:bg-green-700 dark:text-green-200"
        : "bg-zinc-200 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400"
        }`}
    >
      {ativo ? "Ativo" : "Inativo"}
    </span>
  );
}

function TipoBadge({ tipo }: { tipo: TipoUsuario }) {
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${tipo === "ADMINISTRADOR"
        ? "bg-purple-100 text-purple-700 dark:bg-purple-800 dark:text-purple-300"
        : "bg-blue-100 text-blue-700 dark:bg-blue-700 dark:text-blue-300"
        }`}
    >
      {tipo}
    </span>
  );
}

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);


  const [filtroId, setFiltroId] = useState("");

  // edição
  const [editando, setEditando] = useState<Usuario | null>(null);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [tipo, setTipo] = useState<TipoUsuario>("OPERADOR");
  const [ativo, setAtivo] = useState(true);

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
        setUsuarios(await usuariosService.listar());
      }),
    [executar]
  );

  useEffect(() => {
    carregar();
  }, [carregar]);

  const visiveis = useMemo(() => {
    const termo = filtroId.trim();
    return termo ? usuarios.filter((u) => String(u.id) === termo) : usuarios;
  }, [usuarios, filtroId]);

  function iniciarEdicao(u: Usuario) {
    setEditando(u);
    setNome(u.nome);
    setEmail(u.email);
    setSenha("");
    setTipo(u.tipo);
    setAtivo(u.ativo);
  }

  function salvarEdicao(e: React.FormEvent) {
    e.preventDefault();
    if (!editando) return;
    executar(async () => {
      await usuariosService.atualizar(editando.id, {
        nome,
        email,
        senha, // vazio mantém a senha atual
        tipoUsuario: tipo,
        ativo,
      });
      setEditando(null);
      setUsuarios(await usuariosService.listar());
    }, "Usuário atualizado.");
  }

  function desativar(u: Usuario) {
    executar(async () => {
      await usuariosService.desativar(u.id);
      setUsuarios(await usuariosService.listar());
    }, `Usuário ${u.nome} desativado.`);
  }

  function ativar(u: Usuario) {
    executar(async () => {
      await usuariosService.ativar(u.id);
      setUsuarios(await usuariosService.listar());
    }, `Usuário ${u.nome} reativado.`);
  }

  function Acoes({ u }: { u: Usuario }) {
    return (
      <div className="flex flex-wrap gap-2">
        <button onClick={() => iniciarEdicao(u)} disabled={loading} className={btnSecundario}>
          Editar
        </button>
        {u.ativo ? (
          <button onClick={() => desativar(u)} disabled={loading} className={btnPerigo}>
            Desativar
          </button>
        ) : (
          <button onClick={() => ativar(u)} disabled={loading} className={btnSecundario}>
            Reativar
          </button>
        )}
      </div>
    );
  }

  return (
    <main className="mx-auto grid max-w-4xl gap-6 px-4 py-6 text-zinc-900 sm:py-10 dark:text-zinc-100">
      <h1 className="text-xl font-semibold sm:text-2xl">Usuários</h1>

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
          {visiveis.map((u) => (
            <li
              key={u.id}
              className="grid gap-2 rounded-lg border border-zinc-200 bg-white p-3 dark:border-zinc-800 dark:bg-zinc-900"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate font-medium">{u.nome}</p>
                  <p className="truncate text-sm text-zinc-600 dark:text-zinc-400">{u.email}</p>
                </div>
                <span className="text-xs text-zinc-500">#{u.id}</span>
              </div>
              <div className="flex flex-wrap gap-2">
                <TipoBadge tipo={u.tipo} />
                <StatusBadge ativo={u.ativo} />
              </div>
              <Acoes u={u} />
            </li>
          ))}
          {visiveis.length === 0 && (
            <li className="rounded-lg border border-dashed border-zinc-300 p-4 text-center text-sm text-zinc-500 dark:border-zinc-700">
              {filtroId ? "Nenhum usuário com esse ID." : "Nenhum usuário carregado."}
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
                <th className="px-3 py-2 font-medium">E-mail</th>
                <th className="px-3 py-2 font-medium">Tipo</th>
                <th className="px-3 py-2 font-medium">Status</th>
                <th className="px-3 py-2 font-medium text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
              {visiveis.map((u) => (
                <tr key={u.id}>
                  <td className="px-3 py-2">{u.id}</td>
                  <td className="px-3 py-2">{u.nome}</td>
                  <td className="px-3 py-2">{u.email}</td>
                  <td className="px-3 py-2">
                    <TipoBadge tipo={u.tipo} />
                  </td>
                  <td className="px-3 py-2">
                    <StatusBadge ativo={u.ativo} />
                  </td>
                  <td className="px-3 py-2">
                    <div className="flex justify-end">
                      <Acoes u={u} />
                    </div>
                  </td>
                </tr>
              ))}
              {visiveis.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-4 text-center text-zinc-500">
                    {filtroId ? "Nenhum usuário com esse ID." : "Nenhum usuário carregado."}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>



      {/* EDITAR */}
      {editando && (
        <section className="grid gap-3">
          <h2 className="text-lg font-medium">Editar usuário #{editando.id}</h2>
          <form onSubmit={salvarEdicao} className="grid w-full gap-3 ">
            <input className={input} placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <input
              className={input}
              type="email"
              placeholder="E-mail"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <input
              className={input}
              type="password"
              placeholder="Nova senha (deixe vazio para manter)"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              maxLength={72}
            />
            <select className={input} value={tipo} onChange={(e) => setTipo(e.target.value as TipoUsuario)}>
              <option value="OPERADOR">OPERADOR</option>
              <option value="ADMINISTRADOR">ADMINISTRADOR</option>
            </select>
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={ativo} onChange={(e) => setAtivo(e.target.checked)} />
              Ativo
            </label>
            <div className="flex gap-2">
              <button disabled={loading} className={btnPrimario}>
                Salvar
              </button>
              <button type="button" onClick={() => setEditando(null)} className={btnSecundario}>
                Cancelar
              </button>
            </div>
          </form>
        </section>
      )}
    </main>
  );
}