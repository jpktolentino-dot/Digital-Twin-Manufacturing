import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

export async function POST(req: Request) {
  try {
    const { email, senha } = await req.json();

    const res = await fetch(`${API_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, senha }),
      cache: "no-store",
    });

    if (!res.ok) {
      return NextResponse.json(
        { message: "E-mail ou senha inválidos." },
        { status: 401 }
      );
    }

    const data = await res.json();
    const token: string | undefined = data.token; 
    if (!token) {
      return NextResponse.json(
        { message: "Resposta de login sem token." },
        { status: 502 }
      );
    }

    (await cookies()).set("auth", token, {
      httpOnly: true,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      path: "/",
      maxAge: 60 * 60 * 8, 
    });

    return NextResponse.json(data);
  } catch (err) {
    console.error("Erro no login:", err);
    return NextResponse.json(
      { message: "Não foi possível conectar ao servidor." },
      { status: 502 }
    );
  }
}