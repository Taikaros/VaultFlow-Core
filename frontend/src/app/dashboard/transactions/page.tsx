"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/auth";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";

interface Transaction {
  id: string;
  fromCardId: string;
  toWalletId: string;
  amount: number;
  description: string;
  type: string;
  status: string;
  createdAt: string;
}

interface Card {
  id: string;
  cardNumber: string;
  holderName: string;
  walletId: string;
  status: string;
}

interface Wallet {
  id: string;
  balance: number;
  currency: string;
}

export default function TransactionsPage() {
  const { auth } = useAuth();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [cards, setCards] = useState<Card[]>([]);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [fromCardId, setFromCardId] = useState("");
  const [toWalletId, setToWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const load = () => {
    if (!auth) return;
    api.transactions.list(auth.token, page).then((res) => {
      setTransactions(res.content);
      setTotalPages(res.totalPages);
    });
    api.cards.list(auth.token).then(setCards);
    api.wallets.list(auth.token).then(setWallets);
  };

  useEffect(() => { load(); }, [auth, page]);

  const handlePay = async () => {
    if (!auth) return;
    await api.transactions.create(auth.token, {
      fromCardId,
      toWalletId,
      amount: Number(amount),
      description: description || undefined,
    });
    setOpen(false);
    setAmount("");
    setDescription("");
    load();
  };

  const activeCards = cards.filter((c) => c.status === "ACTIVE");

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Transacciones</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger>
            <Button>Nuevo pago</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Realizar pago</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Desde tarjeta</label>
                <select
                  className="w-full border rounded-md px-3 py-2 text-sm"
                  value={fromCardId}
                  onChange={(e) => setFromCardId(e.target.value)}
                >
                  <option value="">Seleccionar tarjeta</option>
                  {activeCards.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.holderName} - ****{c.cardNumber.slice(-4)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Hacia wallet</label>
                <select
                  className="w-full border rounded-md px-3 py-2 text-sm"
                  value={toWalletId}
                  onChange={(e) => setToWalletId(e.target.value)}
                >
                  <option value="">Seleccionar wallet</option>
                  {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                      {w.id.slice(0, 8)}... (${w.balance})
                    </option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Monto</label>
                <Input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Descripción (opcional)</label>
                <Input value={description} onChange={(e) => setDescription(e.target.value)} />
              </div>
              <Button onClick={handlePay} className="w-full">Pagar</Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Historial</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Monto</TableHead>
                <TableHead>Descripción</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead>Fecha</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transactions.map((t) => (
                <TableRow key={t.id}>
                  <TableCell className="font-medium">${t.amount.toFixed(2)}</TableCell>
                  <TableCell>{t.description || "-"}</TableCell>
                  <TableCell>{t.type}</TableCell>
                  <TableCell>
                    <Badge variant={t.status === "COMPLETED" ? "default" : t.status === "FAILED" ? "destructive" : "secondary"}>
                      {t.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">
                    {new Date(t.createdAt).toLocaleString()}
                  </TableCell>
                </TableRow>
              ))}
              {transactions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-gray-500">
                    No hay transacciones
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-4">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</Button>
              <span className="text-sm self-center">Página {page + 1} de {totalPages}</span>
              <Button variant="outline" size="sm" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Siguiente</Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
