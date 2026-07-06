'use client';

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';

interface WebSocketContextType {
  connected: boolean;
  subscribe: (destination: string, callback: (data: unknown) => void) => () => void;
  send: (destination: string, body: unknown) => void;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const [client] = useState(() => new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    connectHeaders: {},
    debug: process.env.NODE_ENV === 'development' ? console.log : undefined,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  }));
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    client.onConnect = () => setConnected(true);
    client.onDisconnect = () => setConnected(false);
    client.activate();
    return () => { client.deactivate(); };
  }, [client]);

  const subscribe = (destination: string, callback: (data: unknown) => void) => {
    const sub = client.subscribe(destination, message => {
      callback(JSON.parse(message.body));
    });
    return () => sub.unsubscribe();
  };

  const send = (destination: string, body: unknown) => {
    client.publish({ destination, body: JSON.stringify(body) });
  };

  return (
    <WebSocketContext.Provider value={{ connected, subscribe, send }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export const useWebSocket = () => {
  const ctx = useContext(WebSocketContext);
  if (!ctx) throw new Error('useWebSocket must be used within WebSocketProvider');
  return ctx;
};
