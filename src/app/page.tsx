
import { RobotArm,Sun } from "lucide-react"
import { ThemeToggle } from "@/components/theme-toggle"


export default function Home() {
    return (
    <>
      <section className="w-screen h-screen flex overflow-hidden dark:bg-zinc-950 bg-[#f0f0f0] transition-colors duration-300 relative">

        <div className='absolute right-4 top-4'>
          <ThemeToggle />
        </div>
        <section className="h-screen w-full p-4 flex justify-center items-center">
          <div className="w-full max-w-md p-8 space-y-8 bg-white rounded-2xl border border-zinc-400 dark:bg-zinc-900 dark:border-zinc-800">
            <div className="text-center flex items-center justify-center flex-col space-y-2">
              {/* <img  alt="logo" className='w-32 dark:brightness-110' /> */}
              <div className="bg-blue-700 w-18 aspect-square relative flex items-center justify-center rounded-xl">
                  <RobotArm size={36} className="text-white" />
              </div>
              <h2 className="text-3xl font-bold text-slate-900 dark:text-zinc-100">Acesse o Sistema</h2>
              <p className="text-slate-500 text-sm dark:text-zinc-400">Entre com suas credenciais do sistema</p>
            </div>

            <form className="space-y-6" >
              <div className="space-y-1 flex flex-col gap-1">
                <label className="text-sm font-medium text-slate-700 ml-1 dark:text-zinc-300">Usuário</label>
                <input data-slot="input" required className="flex h-9 w-full min-w-0 border px-3 py-1 text-base shadow-xs outline-none transition-[color,box-shadow,border] selection:bg-primary selection:text-primary-foreground placeholder:text-muted-foreground md:text-sm bg-input/30 border-zinc-300 rounded-md focus-visible:border-(--lightCyan) focus-visible:ring-(--lightCyan)/30 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive" id="login" max="254" placeholder="usuário@exemplo.com" type="text" autoComplete="off" name="login"></input>
              </div>

              <div className="space-y-1 flex flex-col gap-1" >
                <label className="text-sm font-medium text-slate-700 ml-1 dark:text-zinc-300">Senha</label>
                <input data-slot="input" required className="flex h-9 w-full min-w-0 border px-3 py-1 text-base shadow-xs outline-none transition-[color,box-shadow,border] selection:bg-primary selection:text-primary-foreground placeholder:text-muted-foreground md:text-sm bg-input/30 border-zinc-300 rounded-md focus-visible:border-(--lightCyan) focus-visible:ring-(--lightCyan)/30 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive" id="login" max="254" placeholder="••••••••" type="password" autoComplete="off" name="login"></input>
              </div>

              <button type="submit" className="w-full py-3 px-4 flex justify-center bg-blue-700 hover:bg-violet-900 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transform active:scale-[0.98] transition-all cursor-pointer dark:bg-blue-700 dark:hover:bg-blue-500 dark:shadow-none"> Entrar </button>
            </form>

            <p className="text-center text-xs text-slate-400 dark:text-zinc-500">
              Problemas com acesso? entre em contato com o <span className="text-blue-500 dark:text-blue-400">Suporte TI</span>
            </p>
          </div>
        </section>
      </section>
    </>
  )

}
