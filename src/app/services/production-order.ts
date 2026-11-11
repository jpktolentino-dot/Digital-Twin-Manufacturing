// src/services/ordens-producao.ts
import { api } from "../lib/api";
import type { AlterarEstagioRequest, OrdemProducao, OrdemProducaoRequest } from "../types";

export const ordensProducaoService = {
  listar: () => api<OrdemProducao[]>("/ordens-producao"),
  buscarPorId: (id: number) => api<OrdemProducao>(`/ordens-producao/${id}`),
  criar: (dados: OrdemProducaoRequest) =>
    api<OrdemProducao>("/ordens-producao", { method: "POST", body: JSON.stringify(dados) }),
  atualizar: (id: number, dados: OrdemProducaoRequest) =>
    api<OrdemProducao>(`/ordens-producao/${id}`, { method: "PUT", body: JSON.stringify(dados) }),
  alterarEstagio: (id: number, dados: AlterarEstagioRequest) =>
    api<OrdemProducao>(`/ordens-producao/${id}/estagio`, { method: "PATCH", body: JSON.stringify(dados) }),
  cancelar: (id: number) => api<void>(`/ordens-producao/${id}`, { method: "DELETE" }),
};