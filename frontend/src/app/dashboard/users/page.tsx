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

interface User {
  id: string;
  email: string;
  name: string;
  role: string;
  walletId: string;
  companyId: string;
}

export default function UsersPage() {
  const { auth } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("EMPLOYEE");
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const load = () => {
    if (!auth) return;
    api.users.list(auth.token, page).then((res) => {
      setUsers(res.content);
      setTotalPages(res.totalPages);
    });
  };

  useEffect(() => { load(); }, [auth, page]);

  const handleCreate = async () => {
    if (!auth) return;
    await api.users.create(auth.token, { name, email, password, role });
    setOpen(false);
    setName("");
    setEmail("");
    setPassword("");
    setRole("EMPLOYEE");
    load();
  };

  const handleDelete = async (userId: string) => {
    if (!auth) return;
    await api.users.delete(auth.token, userId);
    load();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Usuarios</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger>
            <Button>Nuevo usuario</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Crear usuario</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Nombre</label>
                <Input value={name} onChange={(e) => setName(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Contraseña</label>
                <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Rol</label>
                <select className="w-full border rounded-md px-3 py-2 text-sm" value={role} onChange={(e) => setRole(e.target.value)}>
                  <option value="EMPLOYEE">Empleado</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
              <Button onClick={handleCreate} className="w-full">Crear</Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Todos los usuarios</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nombre</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Rol</TableHead>
                <TableHead>Acción</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((u) => (
                <TableRow key={u.id}>
                  <TableCell>{u.name}</TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell>
                    <Badge variant={u.role === "ADMIN" ? "default" : "secondary"}>{u.role}</Badge>
                  </TableCell>
                  <TableCell>
                    <Button variant="destructive" size="sm" onClick={() => handleDelete(u.id)}>Eliminar</Button>
                  </TableCell>
                </TableRow>
              ))}
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-gray-500">No hay usuarios</TableCell>
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
