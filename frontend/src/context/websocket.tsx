"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { useAuth } from "./auth";

interface Notification {
  type: string;
  message: string;
  data: Record<string, unknown>;
}

interface WebSocketContextType {
  lastNotification: Notification | null;
}

const WebSocketContext = createContext<WebSocketContextType>({ lastNotification: null });

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080/ws";

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const { auth } = useAuth();
  const [lastNotification, setLastNotification] = useState<Notification | null>(null);

  useEffect(() => {
    if (!auth) return;
    const companyId = auth.company.id;

    let reconnectTimeout: ReturnType<typeof setTimeout>;
    let stompClient: WebSocket | null = null;

    function connect() {
      const ws = new WebSocket(WS_URL);
      stompClient = ws;

      ws.onopen = () => {
        const subscribeMsg = JSON.stringify({ destination: `/topic/company/${companyId}` });
        ws.send(JSON.stringify({ command: "SUBSCRIBE", ...JSON.parse(subscribeMsg) }));
      };

      ws.onmessage = (event) => {
        try {
          const notification: Notification = JSON.parse(event.data);
          setLastNotification(notification);
        } catch { /* ignore parse errors */ }
      };

      ws.onclose = () => {
        reconnectTimeout = setTimeout(connect, 3000);
      };
    }

    connect();

    return () => {
      clearTimeout(reconnectTimeout);
      stompClient?.close();
    };
  }, [auth]);

  return (
    <WebSocketContext.Provider value={{ lastNotification }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket() {
  return useContext(WebSocketContext);
}
