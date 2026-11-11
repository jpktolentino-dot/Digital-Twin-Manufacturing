import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

export async function GET() {
  const token = (await cookies()).get("auth")?.value;
  if (!token) {
    return NextResponse.json({ message: "Não autenticado." }, { status: 401 });
  }

  let email: string | undefined;
  try {
    const payload = JSON.parse(
      Buffer.from(token.split(".")[1], "base64url").toString("utf-8")
    );
    email = payload.email ?? payload.sub;
  } catch {
    return NextResponse.json({ message: "Token inválido." }, { status: 401 });
  }

  // O Spring decide: só administrador acessa /api/usuarios
  let admin = false;
  try {
    const res = await fetch(`${API_URL}/api/usuarios`, {
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    admin = res.ok;
  } catch {
    admin = false;
  }

  return NextResponse.json({
    email,
    tipo: admin ? "ADMINISTRADOR" : "OPERADOR",
  });
}