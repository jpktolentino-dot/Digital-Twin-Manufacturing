import { BuildingComplex } from 'lucide-react'
import Header from '@/components/Header'

export default function dashboard() {
    return (
        <>
        <div className="flex h-screen flex-col overflow-hidden">
                <main className="grid min-h-0 flex-1  grid-cols-[1fr_2.5fr_1fr]">
                    <section className="bg-[#0F172B] p-6"></section>
                    <section className="relative p-6 flex items-center justify-center">
                        <section className="border-2 border-zinc-600 p-7 py-10 rounded-xl bg-[#172030]">
                            <div className="text-center ">
                                <div className='flex flex-col gap-4 '>
                                    <div className='flex justify-center'>
                                        <BuildingComplex size={90} />
                                    </div>
                                    <div className='space-y-2 '>
                                        <div className="flex items-center justify-center gap-2 ">
                                            <span className="size-2 shrink-0 rounded-full bg-green-400"></span>
                                            <h1 className="text-[1.75rem] font-bold">Área de visualização</h1>
                                        </div>
                                        <p className='text-sm text-zinc-500'>acompanhe a máquina em tempo real</p>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </section>
                    <section className="bg-[#0F172B] p-6"></section>
                </main>
        </div>
        </>
    )
}