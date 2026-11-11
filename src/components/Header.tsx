'use client';
import { RobotArm, LogOut } from 'lucide-react'
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useTheme } from 'next-themes';
import { Sun, Moon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '../app/services/auth';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import Link from "next/link";

function Header() {

    const { resolvedTheme, setTheme } = useTheme();

    const isDark = resolvedTheme === "dark";

    const router = useRouter();
    const [email, setEmail] = useState<string | null>(null);

    useEffect(() => {
        fetch("/auth/me")
            .then((r) => (r.ok ? r.json() : null))
            .then((d) => setEmail(d?.email ?? null))
            .catch(() => setEmail(null));
    }, []);

    async function sair() {
        try {
            await authService.logout();
        } finally {
            router.push("/login");
            router.refresh();
        }
    }

    const inicial = email ? email.charAt(0).toUpperCase() : "?";
    return (
        <>
            <header className="top-0 sticky w-full bg-white dark:bg-background pointer-events-auto">
                <div className="max-w-full mx-auto border-b dark:border-zinc-800 border-zinc-400 px-4 sm:px-6 lg:px-4 ">
                    <div className="flex items-center justify-between md:justify-around h-16 md:grid md:grid-cols-3">

                        <Link href="/home" className="flex justify-self-start items-center gap-3">

                            <div className='bg-blue-500 p-2 rounded-lg'>
                                <RobotArm className='text-white' />
                            </div>
                            <div>
                                <h1 className='text-xl'>
                                    Digital Twin manufacture
                                </h1>
                                <p className='text-xs text-zinc-500'> Jermam v1.0</p>
                            </div>
                        </Link>

                        <nav className="hidden md:flex justify-center items-center gap-1 lg:gap-8 font-medium text-[1rem] w-full">

                        </nav>
                        <div className="col-start-3 flex justify-end items-center gap-2 relative">
                            
                            <DropdownMenu>
                                <DropdownMenuTrigger
                                    render={
                                        <div
                                            aria-label="Abrir menu do usuário"
                                            className="flex size-8 cursor-pointer items-center justify-center rounded-full bg-gray-700 text-white outline-none focus-visible:ring-2 focus-visible:ring-ring"
                                        />
                                    }
                                >
                                    {inicial}
                                </DropdownMenuTrigger>

                                <DropdownMenuContent align="end" className="w-48 z-100 ">
                                    <DropdownMenuItem onClick={() => setTheme(isDark ? "light" : "dark")} className="cursor-pointer">
                                        {isDark ? <Sun className="size-4" /> : <Moon className="size-4" />}
                                        {isDark ? "Tema claro" : "Tema escuro"}
                                    </DropdownMenuItem>
                                    <DropdownMenuItem onClick={sair} className="text-red-600 cursor-pointer">
                                        <LogOut />
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