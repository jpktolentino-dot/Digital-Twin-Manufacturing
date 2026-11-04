// src/services/usuarios.ts
import { api } from "../lib/api";
import type { Usuario, UsuarioRequest, UsuarioUpdateRequest } from "../types";

export const usuariosService = {
  listar: () => api<Usuario[]>("/usuarios"),

  buscarPorId: (id: number) => api<Usuario>(`/usuarios/${id}`),

  criar: (dados: UsuarioRequest) =>
    api<Usuario>("/usuarios", { method: "POST", body: JSON.stringify(dados) }),

  atualizar: (id: number, dados: UsuarioUpdateRequest) =>
    api<Usuario>(`/usuarios/${id}`, { method: "PUT", body: JSON.stringify(dados) }),

  desativar: (id: number) => api<void>(`/usuarios/${id}`, { method: "DELETE" }),

  ativar: (id: number) => api<void>(`/usuarios/${id}/ativar`, { method: "PATCH" }),
};