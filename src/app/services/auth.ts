import type { Usuario } from "../types";

async function chamar<T>(url: string, body?: unknown): Promise<T> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const erro = await res.json().catch(() => null);
    throw new Error(erro?.message ?? `Erro ${res.status}`);
  }
  return res.json();
}

export const authService = {
  login: (email: string, senha: string) => chamar<Usuario>("/auth/login", { email, senha }),
  logout: () => chamar<{ ok: boolean }>("/auth/logout"),
};