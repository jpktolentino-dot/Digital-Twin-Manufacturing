import Link from "next/link";
import { ArrowLeft, Home, SearchX } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="flex min-h-full bg-[hsl(0,0%,97%)] dark:bg-[hsl(217,33%,5%)] items-center justify-center px-6 py-12">
      <section
        role="alert"
        className="w-full max-w-lg rounded-2xl border border-border bg-card p-10 text-center shadow-sm"
      >
        <div className="mx-auto mb-6 flex size-20 items-center justify-center rounded-full bg-red-500/10 text-red-500 dark:text-red-400">
          <SearchX className="size-10" aria-hidden="true" />
        </div>

        <p className="mb-2 text-6xl font-black tracking-tight text-foreground">
          404
        </p>

        <h1 className="mb-3 text-2xl font-bold tracking-tight text-foreground">
          Página não encontrada
        </h1>

        <p className="mx-auto mb-8 max-w-sm text-sm leading-relaxed text-muted-foreground">
          O item que você está tentando acessar não existe no sistema, foi
          removido ou o endereço está incorreto.
        </p>

        <div className="flex flex-col justify-center gap-3 sm:flex-row">
          <Button render={<Link href="/dashboard" />} nativeButton={false}>
            <Home className="size-4" />
            Ir para o dashboard
          </Button>
        </div>
      </section>
    </main>
  );
}