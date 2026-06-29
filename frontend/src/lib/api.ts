const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

interface FetchOptions extends RequestInit {
  token?: string;
}

async function request<T>(path: string, options: FetchOptions = {}): Promise<T> {
  const { token, ...fetchOptions } = options;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(fetchOptions.headers as Record<string, string>),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...fetchOptions,
    headers,
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Error desconocido" }));
    throw new Error(error.message || `Error ${res.status}`);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  auth: {
    register: (data: { name: string; taxId: string; email: string; password: string }) =>
      request<{ token: string; company: unknown }>("/auth/register", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    login: (data: { email: string; password: string }) =>
      request<{ token: string; company: unknown }>("/auth/login", {
        method: "POST",
        body: JSON.stringify(data),
      }),
  },
  wallets: {
    list: (token: string) =>
      request<Array<{ id: string; companyId: string; balance: number; currency: string }>>("/wallets", { token }),
    create: (token: string, currency?: string) =>
      request<{ id: string; companyId: string; balance: number; currency: string }>("/wallets", {
        method: "POST",
        body: JSON.stringify({ currency }),
        token,
      }),
    getById: (token: string, id: string) =>
      request<{ id: string; companyId: string; balance: number; currency: string }>(`/wallets/${id}`, { token }),
    transactions: (token: string, id: string) =>
      request<Array<{ id: string; fromCardId: string; toWalletId: string; amount: number; description: string; type: string; status: string; createdAt: string }>>(`/wallets/${id}/transactions`, { token }),
  },
  cards: {
    list: (token: string, walletId?: string) => {
      const params = walletId ? `?walletId=${walletId}` : "";
      return request<Array<{ id: string; walletId: string; cardNumber: string; holderName: string; status: string; limitAmount: number; spentAmount: number; createdAt: string }>>(`/cards${params}`, { token });
    },
    create: (token: string, walletId: string, data: { holderName: string; limitAmount?: number }) =>
      request<{ id: string; walletId: string; cardNumber: string; holderName: string; status: string; limitAmount: number; spentAmount: number }>(`/wallets/${walletId}/cards`, {
        method: "POST",
        body: JSON.stringify(data),
        token,
      }),
    update: (token: string, cardId: string, data: { status?: string; limitAmount?: number }) =>
      request<{ id: string; status: string; limitAmount: number }>(`/cards/${cardId}`, {
        method: "PATCH",
        body: JSON.stringify(data),
        token,
      }),
    cancel: (token: string, cardId: string) =>
      request<void>(`/cards/${cardId}`, { method: "DELETE", token }),
  },
  transactions: {
    create: (token: string, data: { fromCardId: string; toWalletId: string; amount: number; description?: string }) =>
      request<{ id: string; status: string }>("/transactions", {
        method: "POST",
        body: JSON.stringify(data),
        token,
      }),
    list: (token: string) =>
      request<Array<{ id: string; fromCardId: string; toWalletId: string; amount: number; description: string; type: string; status: string; createdAt: string }>>("/transactions", { token }),
  },
};
