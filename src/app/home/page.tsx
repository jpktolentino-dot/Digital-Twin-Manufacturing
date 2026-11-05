import Link from "next/link";
import BotaoSair from "../../components/LogoutBtn";

type Modulo = {
  titulo: string;
  descricao: string;
  href?: string; // sem href = ainda não implementado
};

const GRUPOS: { nome: string; modulos: Modulo[] }[] = [
  {
    nome: "Cadastros",
    modulos: [
      { titulo: "Usuários", descricao: "Listar, editar, ativar e desativar usuários.", href: "/usuarios-test" },
      { titulo: "Novo usuário", descricao: "Cadastrar usuário ou primeiro administrador.", href: "/test" },
      { titulo: "Setores", descricao: "Setores da fábrica.", href: "/setores-test" },
      { titulo: "Máquinas", descricao: "Máquinas, estado operacional e setor.", href: "/maquinas-test" },
      { titulo: "Produtos", descricao: "Produtos fabricados.", href: "/produtos-test" },
    ],
  },
  {
    nome: "Produção",
    modulos: [
      { titulo: "Ordens de produção", descricao: "Planejamento, estágios e cancelamento.", href: "/ordens-producao-test" },
      { titulo: "Eventos", descricao: "Registro de eventos industriais.", href: "/eventos-test" },
    ],
  },
  {
    nome: "Análise",
    modulos: [
      { titulo: "Histórico de máquinas", descricao: "Transições de estado de cada máquina.", href: "/historicos-maquinas" },
      { titulo: "KPIs", descricao: "Disponibilidade e tempos por estado. Em breve." },
    ],
  },
];

const base =
  "rounded-lg border p-4 transition";
const ativo =
  `${base} border-zinc-200 bg-white hover:border-blue-500 hover:shadow-sm dark:border-zinc-800 dark:bg-zinc-900 dark:hover:border-blue-500`;
const inativo =
  `${base} cursor-not-allowed border-dashed border-zinc-300 opacity-60 dark:border-zinc-700`;

export default function HomePage() {
  return (
    <main className="mx-auto grid max-w-5xl gap-8 px-4 py-10 text-zinc-900 dark:text-zinc-100">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Digital Twin Manufacturing</h1>
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Selecione um módulo para começar.
          </p>
        </div>
        <BotaoSair />
      </header>

      {GRUPOS.map((grupo) => (
        <section key={grupo.nome} className="grid gap-3">
          <h2 className="text-sm font-medium uppercase tracking-wide text-zinc-500">
            {grupo.nome}
          </h2>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {grupo.modulos.map((m) =>
              m.href ? (
                <Link key={m.titulo} href={m.href} className={ativo}>
                  <h3 className="font-medium">{m.titulo}</h3>
                  <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">{m.descricao}</p>
                </Link>
              ) : (
                <div key={m.titulo} className={inativo} aria-disabled="true">
                  <h3 className="font-medium">{m.titulo}</h3>
                  <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">{m.descricao}</p>
                </div>
              )
            )}
          </div>
        </section>
      ))}
    </main>
  );
}