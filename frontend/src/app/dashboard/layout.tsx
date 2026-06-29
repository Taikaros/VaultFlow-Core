"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import Link from "next/link";
import { useAuth } from "@/context/auth";
import { Button } from "@/components/ui/button";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { auth, logout } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!auth) router.push("/login");
  }, [auth, router]);

  if (!auth) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link href="/dashboard" className="font-bold text-xl">VaultFlow</Link>
            <nav className="flex gap-4 text-sm">
              <Link href="/dashboard" className="text-gray-600 hover:text-gray-900">Inicio</Link>
              <Link href="/dashboard/cards" className="text-gray-600 hover:text-gray-900">Tarjetas</Link>
              <Link href="/dashboard/transactions" className="text-gray-600 hover:text-gray-900">Transacciones</Link>
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-500">{auth.company.name}</span>
            <Button variant="outline" size="sm" onClick={() => { logout(); router.push("/login"); }}>
              Cerrar sesión
            </Button>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        {children}
      </main>
    </div>
  );
}
