

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

function getAuthHeaders(): Record<string, string> {
  return {};
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`/api${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
      ...options.headers,
    },
  });

  if (res.status === 401) {
    await fetch("/auth/logout", { method: "POST" });
    throw new ApiError(401, "Sessão expirada.");
  }

  if (!res.ok) {
    const erro = await res.json().catch(() => null);
    throw new ApiError(res.status, erro?.message ?? `Erro ${res.status}`);
  }

  return res.status === 204 ? (undefined as T) : res.json();
}