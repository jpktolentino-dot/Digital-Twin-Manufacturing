
import { BuildingComplex } from "lucide-react";
export default function App() {


    return (
        <div className="h-screen flex flex-col bg-slate-950">

            <div className="flex flex-1 overflow-hidden">
                <div className="w-[380px] p-8 bg-slate-950 border-r-2 border-slate-800 flex-shrink-0 overflow-hidden ">
                    <div className="mb-4">
                        <h1 className="text-[1.3rem] font-bold">
                            Nome da máquina
                        </h1>
                        <p className="text-zinc-400">
                            Id da máquina
                        </p>
                    </div>
                    <div className="p-4 border-2 bg-slate-900 border-slate-600 rounded-2xl">
                        <div className="flex justify-between">
                            <div className="space-y-3 ">
                                <p>Informação 1</p>
                                <p>Informação 2</p>
                                <p>Informação 3</p>
                                <p>Informação 4</p>
                            </div>
                            <div className="text-end space-y-3">
                                <p>55</p>
                                <p>234</p>
                                <p>10</p>
                                <p>30</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="flex-1 flex items-center justify-center bg-slate-900 relative overflow-hidden">
                    <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] opacity-20" />
                    <div className="relative text-center z-10">
                        <div className="bg-slate-800 p-12 flex flex-col items-center space-y-5 rounded-2xl border-2 border-slate-700 shadow-2xl mb-6 ">
                            <span>
                                <BuildingComplex size={110} />
                            </span>

                            <div>
                                <h2 className="text-2xl text-white mb-2">Área de integração 3D</h2>
                                <p className="text-base text-slate-400">Acompanhe a máquina em tempo real</p>
                            </div>

                        </div>
                        <div className="flex items-center justify-center gap-3">
                            <div className="w-3 h-3 bg-blue-500 rounded-full animate-pulse" />
                            <p className="text-sm text-slate-500">Pronto para mostrar a imagem</p>
                        </div>
                    </div>
                </div>

                <div className="w-[380px] bg-slate-950 border-l-2 border-slate-800 flex-shrink-0 overflow-hidden p-6">
                    <div className="space-y-10">

                        <h1 className="text-[1.3rem] font-bold mb-4">
                            Ordem de produção
                        </h1>
                        <div className="p-4 border-2 bg-slate-900 border-slate-600 rounded-2xl">
                            <div className="flex justify-between">
                                <div className="space-y-3 ">
                                    <p>Número</p>
                                    <p>Estágio atual</p>
                                    <p>Produto_id</p>
                                    <p>Operador_id</p>
                                </div>
                                <div className="text-end space-y-3">
                                    <p>4</p>
                                    <p>Em andamento</p>
                                    <p>12</p>
                                    <p>42</p>
                                </div>
                            </div>
                        </div>
                        <h1 className="text-[1.3rem] font-bold mb-4">
                            Produto
                        </h1>
                        <div className="p-4 border-2 bg-slate-900 border-slate-600 rounded-2xl">
                            <div className="flex justify-between">
                                <div className="space-y-3 font-bold">
                                    <p>Id</p>
                                    <p>Nome</p>
                                    <p>Código</p>
                                    <p>Específicações</p>
                                </div>
                                <div className="text-end space-y-3">
                                    <p>4</p>
                                    <p>Garrafa</p>
                                    <p>12</p>
                                    <p>*</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
