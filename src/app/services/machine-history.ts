
import { api } from "../lib/api";
import type { HistoricoMaquina } from "../types";

export const historicosMaquinasService = {
  listar: () => api<HistoricoMaquina[]>("/historicos-maquinas"),
  buscarPorId: (id: number) => api<HistoricoMaquina>(`/historicos-maquinas/${id}`),
  listarPorMaquina: (maquinaId: number) =>
    api<HistoricoMaquina[]>(`/historicos-maquinas/maquina/${maquinaId}`),
};