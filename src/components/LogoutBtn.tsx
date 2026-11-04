// src/components/BotaoSair.tsx
"use client";
import { useRouter } from "next/navigation";
import { authService } from "../app/services/auth";

export default function BotaoSair() {
  const router = useRouter();

  async function sair() {
    await authService.logout();
    router.push("/login");
    router.refresh();
  }

  return <button onClick={sair}>Sair</button>;
}