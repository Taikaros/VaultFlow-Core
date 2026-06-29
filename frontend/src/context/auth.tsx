"use client";

import { createContext, useContext, useState, useCallback, type ReactNode } from "react";
import { api } from "@/lib/api";

interface AuthState {
  token: string;
  company: { id: string; name: string; email: string };
}

interface AuthContextType {
  auth: AuthState | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, taxId: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<AuthState | null>(null);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.auth.login({ email, password });
    setAuth({ token: res.token, company: res.company as AuthState["company"] });
  }, []);

  const register = useCallback(async (name: string, taxId: string, email: string, password: string) => {
    const res = await api.auth.register({ name, taxId, email, password });
    setAuth({ token: res.token, company: res.company as AuthState["company"] });
  }, []);

  const logout = useCallback(() => setAuth(null), []);

  return (
    <AuthContext.Provider value={{ auth, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de AuthProvider");
  return ctx;
}
