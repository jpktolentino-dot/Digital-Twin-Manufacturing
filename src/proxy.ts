import { NextResponse, type NextRequest } from "next/server";

export function proxy(req: NextRequest) {
  const token = req.cookies.get("auth")?.value;
  const { pathname } = req.nextUrl;
  const ehLogin = pathname === "/login" || pathname.startsWith("/login/");

  // Sem login: qualquer página protegida vai para /login
  if (!token && !ehLogin) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  // Já logado: /login vai para a página principal
  if (token && ehLogin) {
    return NextResponse.redirect(new URL("/", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!api|auth|_next/static|_next/image|favicon.ico).*)"],
};