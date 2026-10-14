'use client';
import { RobotArm, LogOut } from 'lucide-react'
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useTheme } from 'next-themes';
import { Sun, Moon } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";



function Header() {

    const { resolvedTheme, setTheme } = useTheme();

    const isDark = resolvedTheme === "dark";

    return (
        <>
            <header className="top-0 sticky w-full bg-background pointer-events-auto ">
                <div className="max-w-full mx-auto border-b dark:border-slate-800 border-zinc-400 px-4 sm:px-6 lg:px-4 ">
                    <div className="flex items-center justify-between md:justify-around h-16 md:grid md:grid-cols-3">

                        <div className="flex justify-self-start items-center gap-3">

                            <div className='bg-blue-500 p-2 rounded-lg'>
                                <RobotArm className='text-white' />
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
                                    placeholder="Buscar por id ou nome..."
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
                            <DropdownMenu>
                                <DropdownMenuTrigger
                                    render={
                                        <button
                                            type="button"
                                            aria-label="Abrir menu do usuário"
                                            className="flex size-8 cursor-pointer items-center justify-center rounded-full bg-gray-700 text-white outline-none focus-visible:ring-2 focus-visible:ring-ring"
                                        />
                                    }
                                >
                                    E
                                </DropdownMenuTrigger>

                                <DropdownMenuContent align="end" className="w-48 z-100 ">
                                    <DropdownMenuItem onClick={() => setTheme(isDark ? "light" : "dark")} className="cursor-pointer">
                                        {isDark ? <Sun className="size-4" /> : <Moon className="size-4" />}
                                        {isDark ? "Tema claro" : "Tema escuro"}
                                    </DropdownMenuItem>
                                    <DropdownMenuItem className="text-red-600 cursor-pointer">
                                        <LogOut/>
                                        Logout
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                    </div>
                </div>
            </header>

        </>
    )
}

export default Header