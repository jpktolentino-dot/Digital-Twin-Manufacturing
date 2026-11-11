// src/services/kpis.ts
import { api } from "../lib/api";
import type { KpiMaquina } from "../types";


export const kpisService = {
  calcularPorMaquina: (maquinaId: number, params: { inicio: string; fim: string }) =>
    api<KpiMaquina>(`/kpis/maquinas/${maquinaId}?${new URLSearchParams(params)}`),
};