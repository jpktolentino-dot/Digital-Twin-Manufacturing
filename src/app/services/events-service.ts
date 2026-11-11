
import { api } from "../lib/api";
import type { Evento, EventoRequest } from "../types";

export const eventosService = {
  listar: () => api<Evento[]>("/eventos"),
  buscarPorId: (id: number) => api<Evento>(`/eventos/${id}`),
  listarPorOrdemProducao: (ordemProducaoId: number) =>
    api<Evento[]>(`/eventos/ordem-producao/${ordemProducaoId}`),
  listarPorMaquina: (maquinaId: number) =>
    api<Evento[]>(`/eventos/maquina/${maquinaId}`),
  criar: (dados: EventoRequest) =>
    api<Evento>("/eventos", { method: "POST", body: JSON.stringify(dados) }),
};