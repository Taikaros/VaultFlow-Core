"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/auth";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

export default function DashboardPage() {
  const { auth } = useAuth();
  const [metrics, setMetrics] = useState<{
    totalBalance: number;
    walletCount: number;
    activeCards: number;
    totalCards: number;
    completedTransactions: number;
    failedTransactions: number;
    totalTransactions: number;
  } | null>(null);
  const [transactions, setTransactions] = useState<Array<{
    id: string; amount: number; description: string; type: string; status: string; createdAt: string;
  }>>([]);

  useEffect(() => {
    if (!auth) return;
    api.dashboard.metrics(auth.token)
      .then(setMetrics)
      .catch((err) => console.error("Error al cargar métricas:", err));
    api.transactions.list(auth.token, 0, 10)
      .then((res) => setTransactions(res.content))
      .catch((err) => console.error("Error al cargar transacciones:", err));
  }, [auth]);

  const txPieData = metrics ? [
    { name: "Completadas", value: metrics.completedTransactions },
    { name: "Fallidas", value: metrics.failedTransactions },
  ] : [];

  const COLORS = ["#22c55e", "#ef4444"];

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      {metrics && (
        <div className="grid gap-4 md:grid-cols-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm text-gray-500">Saldo total</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">${metrics.totalBalance.toFixed(2)}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm text-gray-500">Wallets</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{metrics.walletCount}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm text-gray-500">Tarjetas activas</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{metrics.activeCards}/{metrics.totalCards}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm text-gray-500">Transacciones</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold">{metrics.totalTransactions}</p>
            </CardContent>
          </Card>
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {metrics && metrics.totalTransactions > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>Transacciones</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={txPieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {txPieData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle>Resumen de wallets</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Balance total</span>
              <span className="font-semibold">${metrics?.totalBalance.toFixed(2) ?? "0.00"}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Wallets activas</span>
              <span className="font-semibold">{metrics?.walletCount ?? 0}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Tarjetas emitidas</span>
              <span className="font-semibold">{metrics?.totalCards ?? 0}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Tasa de éxito</span>
              <span className="font-semibold">
                {metrics && metrics.totalTransactions > 0
                  ? `${((metrics.completedTransactions / metrics.totalTransactions) * 100).toFixed(1)}%`
                  : "-"}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      {transactions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Transacciones recientes</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Monto</TableHead>
                  <TableHead>Descripción</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Fecha</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {transactions.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell>${t.amount.toFixed(2)}</TableCell>
                    <TableCell>{t.description || "-"}</TableCell>
                    <TableCell>
                      <Badge variant={t.status === "COMPLETED" ? "default" : t.status === "FAILED" ? "destructive" : "secondary"}>
                        {t.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm text-gray-500">
                      {new Date(t.createdAt).toLocaleDateString()}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
