import { RobotArm } from 'lucide-react'
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";


function Header() {

    return (
        <>
            <header className="top-0 sticky z-60 w-full bg-background pointer-events-auto ">
                <div className="max-w-full mx-auto px-4 sm:px-6 lg:px-4">
                    <div className="flex items-center justify-between md:justify-around h-16 md:grid md:grid-cols-3">

                        <div className="flex justify-self-start items-center gap-3">

                            <div className='bg-blue-500 p-2 rounded-lg'>
                                <RobotArm />
                            </div>
                            <div>
                                <h1 className='text-xl'>
                                    Digital Twin manufacture
                                </h1>
                                <p className='text-xs text-zinc-500'> Jermam v1.0</p>
                            </div>
                        </div>

                        <nav className="hidden md:flex justify-center items-center gap-1 lg:gap-8 font-medium text-[1rem] w-full">

                        </nav>
                        <div className="col-start-3 flex justify-end items-center gap-2 relative">
                            <div className="relative w-full max-w-xs">
                                <Input
                                    type="search"
                                    placeholder="Buscar..."
                                    className="pr-10"
                                />
                                <button
                                    type="button"
                                    className="absolute right-1 top-1/2 -translate-y-1/2 rounded-md p-1.5 text-zinc-500 hover:bg-zinc-100 hover:text-zinc-900"
                                    aria-label="Buscar"
                                >
                                    <Search className="size-4" />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

        </>
    )
}

export default Header