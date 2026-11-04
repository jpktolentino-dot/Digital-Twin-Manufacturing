// src/services/maquinas.ts
import { api } from "../lib/api";

import type { AlterarEstadoMaquinaRequest, Maquina, MaquinaRequest } from "../types";

export const maquinasService = {
  listar: () => api<Maquina[]>("/maquinas"),
  buscarPorId: (id: number) => api<Maquina>(`/maquinas/${id}`),
  listarPorSetor: (setorId: number) => api<Maquina[]>(`/maquinas/setor/${setorId}`),
  criar: (dados: MaquinaRequest) =>
    api<Maquina>("/maquinas", { method: "POST", body: JSON.stringify(dados) }),
  atualizar: (id: number, dados: MaquinaRequest) =>
    api<Maquina>(`/maquinas/${id}`, { method: "PUT", body: JSON.stringify(dados) }),
  alterarEstado: (id: number, dados: AlterarEstadoMaquinaRequest) =>
    api<Maquina>(`/maquinas/${id}/estado`, { method: "PATCH", body: JSON.stringify(dados) }),
  desativar: (id: number) => api<void>(`/maquinas/${id}`, { method: "DELETE" }),
  ativar: (id: number) => api<void>(`/maquinas/${id}/ativar`, { method: "PATCH" }),
};