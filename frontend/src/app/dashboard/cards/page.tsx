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

interface Card {
  id: string;
  walletId: string;
  cardNumber: string;
  holderName: string;
  status: string;
  limitAmount: number;
  spentAmount: number;
}

interface Wallet {
  id: string;
  balance: number;
  currency: string;
}

export default function CardsPage() {
  const { auth } = useAuth();
  const [cards, setCards] = useState<Card[]>([]);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [holderName, setHolderName] = useState("");
  const [limit, setLimit] = useState("");
  const [selectedWallet, setSelectedWallet] = useState("");
  const [open, setOpen] = useState(false);
  const [walletOpen, setWalletOpen] = useState(false);
  const [newWalletCurrency, setNewWalletCurrency] = useState("USD");

  const handleCreateWallet = async () => {
    if (!auth) return;
    await api.wallets.create(auth.token, newWalletCurrency);
    setWalletOpen(false);
    setNewWalletCurrency("USD");
    load();
  };

  const load = () => {
    if (!auth) return;
    api.cards.list(auth.token).then(setCards);
    api.wallets.list(auth.token).then(setWallets);
  };

  useEffect(() => { load(); }, [auth]);

  const handleCreate = async () => {
    if (!auth) return;
    await api.cards.create(auth.token, selectedWallet, {
      holderName,
      limitAmount: limit ? Number(limit) : undefined,
    });
    setOpen(false);
    setHolderName("");
    setLimit("");
    load();
  };

  const handleCancel = async (cardId: string) => {
    if (!auth) return;
    await api.cards.cancel(auth.token, cardId);
    load();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Tarjetas</h1>
        <div className="flex gap-2">
          <Dialog open={walletOpen} onOpenChange={setWalletOpen}>
            <DialogTrigger>
              <Button variant="outline">Nueva wallet</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Crear wallet</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Moneda</label>
                  <select className="w-full border rounded-md px-3 py-2 text-sm" value={newWalletCurrency} onChange={(e) => setNewWalletCurrency(e.target.value)}>
                    <option value="USD">USD - Dólar</option>
                    <option value="EUR">EUR - Euro</option>
                    <option value="ARS">ARS - Peso argentino</option>
                    <option value="BRL">BRL - Real brasileño</option>
                    <option value="MXN">MXN - Peso mexicano</option>
                    <option value="COP">COP - Peso colombiano</option>
                    <option value="CLP">CLP - Peso chileno</option>
                    <option value="PEN">PEN - Sol peruano</option>
                  </select>
                </div>
                <Button onClick={handleCreateWallet} className="w-full">Crear</Button>
              </div>
            </DialogContent>
          </Dialog>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger>
              <Button>Emitir tarjeta</Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Nueva tarjeta</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Wallet destino</label>
                  <select
                    className="w-full border rounded-md px-3 py-2 text-sm"
                    value={selectedWallet}
                    onChange={(e) => setSelectedWallet(e.target.value)}
                  >
                    <option value="">Seleccionar wallet</option>
                    {wallets.map((w) => (
                      <option key={w.id} value={w.id}>
                        {w.id.slice(0, 8)}... ({w.currency} ${w.balance.toFixed(2)})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Nombre del titular</label>
                  <Input value={holderName} onChange={(e) => setHolderName(e.target.value)} />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Límite (opcional)</label>
                  <Input type="number" value={limit} onChange={(e) => setLimit(e.target.value)} />
                </div>
                <Button onClick={handleCreate} className="w-full">Crear</Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Todas las tarjetas</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Titular</TableHead>
                <TableHead>Número</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead>Límite</TableHead>
                <TableHead>Gastado</TableHead>
                <TableHead>Acción</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {cards.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>{c.holderName}</TableCell>
                  <TableCell className="font-mono">****{c.cardNumber.slice(-4)}</TableCell>
                  <TableCell>
                    <Badge variant={c.status === "ACTIVE" ? "default" : "secondary"}>
                      {c.status}
                    </Badge>
                  </TableCell>
                  <TableCell>${c.limitAmount.toFixed(2)}</TableCell>
                  <TableCell>${c.spentAmount.toFixed(2)}</TableCell>
                  <TableCell>
                    {c.status === "ACTIVE" && (
                      <Button variant="destructive" size="sm" onClick={() => handleCancel(c.id)}>
                        Cancelar
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {cards.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-gray-500">
                    No hay tarjetas todavia
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
