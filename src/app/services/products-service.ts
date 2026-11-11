
import { api } from "../lib/api";
import type { Produto, ProdutoRequest } from "../types";

export const produtosService = {
  listar: () => api<Produto[]>("/produtos"),
  buscarPorId: (id: number) => api<Produto>(`/produtos/${id}`),
  criar: (dados: ProdutoRequest) =>
    api<Produto>("/produtos", { method: "POST", body: JSON.stringify(dados) }),
  atualizar: (id: number, dados: ProdutoRequest) =>
    api<Produto>(`/produtos/${id}`, { method: "PUT", body: JSON.stringify(dados) }),
  desativar: (id: number) => api<void>(`/produtos/${id}`, { method: "DELETE" }),
  ativar: (id: number) => api<void>(`/produtos/${id}/ativar`, { method: "PATCH" }),
};