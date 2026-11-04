// src/services/setores.ts
import { api } from "../lib/api";
import type { Setor, SetorRequest } from "../types";

export const setoresService = {
  listar: () => api<Setor[]>("/setores"),
  buscarPorId: (id: number) => api<Setor>(`/setores/${id}`),
  criar: (dados: SetorRequest) =>
    api<Setor>("/setores", { method: "POST", body: JSON.stringify(dados) }),
  atualizar: (id: number, dados: SetorRequest) =>
    api<Setor>(`/setores/${id}`, { method: "PUT", body: JSON.stringify(dados) }),
  desativar: (id: number) => api<void>(`/setores/${id}`, { method: "DELETE" }),
  ativar: (id: number) => api<void>(`/setores/${id}/ativar`, { method: "PATCH" }),
};