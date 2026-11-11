
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function POST() {
  (await cookies()).delete("auth");
  return NextResponse.json({ ok: true });
}