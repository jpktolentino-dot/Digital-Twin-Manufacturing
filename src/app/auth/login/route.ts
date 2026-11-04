// src/app/auth/login/route.ts
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const API_URL = process.env.API_URL ;

export async function POST(req: Request) {
  const { email, senha } = await req.json();
  const token = Buffer.from(`${email}:${senha}`).toString("base64");

  const res = await fetch(`${API_URL}/api/auth/me`, {
    headers: { Authorization: `Basic ${token}` },
    cache: "no-store",
  });

  if (!res.ok) {
    return NextResponse.json({ message: "E-mail ou senha inválidos." }, { status: 401 });
  }

  (await cookies()).set("auth", token, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 60 * 60 * 8, 
  });

  return NextResponse.json(await res.json());
}