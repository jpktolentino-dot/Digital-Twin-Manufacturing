import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function GET() {
  const token = (await cookies()).get("auth")?.value;
  if (!token) {
    return NextResponse.json({ message: "Não autenticado." }, { status: 401 });
  }

  try {
    const payload = JSON.parse(
      Buffer.from(token.split(".")[1], "base64url").toString("utf-8")
    );
    // Em geral o "sub" do JWT é o e-mail (o login é por e-mail)
    const email: string | undefined = payload.email ?? payload.sub;
    if (!email) throw new Error("sem e-mail");
    return NextResponse.json({ email });
  } catch {
    return NextResponse.json({ message: "Token inválido." }, { status: 401 });
  }
}