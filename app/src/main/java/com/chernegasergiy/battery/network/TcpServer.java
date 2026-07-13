package com.chernegasergiy.battery.network;

import android.util.Log;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static final String TAG = "TcpServer";
    private Thread listenerThread;
    private ServerSocket activeServer;
    private boolean running = false;
    
    private int port;
    private boolean allInterfaces;
    private java.util.List<String> allowedIps;
    private final Listener listener;

    public interface Listener {
        void onServerStarted();
        void onServerError(Exception e);
        void onClientConnected(String clientIp);
        void onClientBlocked(String clientIp);
        String onRequestData();
    }

    public TcpServer(int port, boolean allInterfaces, java.util.List<String> allowedIps, Listener listener) {
        this.port = port;
        this.allInterfaces = allInterfaces;
        this.allowedIps = allowedIps;
        this.listener = listener;
    }

    public void updateConfig(int port, boolean allInterfaces, java.util.List<String> allowedIps) {
        this.port = port;
        this.allInterfaces = allInterfaces;
        this.allowedIps = allowedIps;
    }

    public void start() {
        Thread oldThread = listenerThread;
        stop();
        running = true;
        
        listenerThread = new Thread(() -> {
            if (oldThread != null) {
                try {
                    oldThread.join(2000); // Properly wait for old thread to release resources
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                InetAddress bindAddress = InetAddress.getByName(allInterfaces ? "0.0.0.0" : "127.0.0.1");
                Log.d(TAG, "Opening ServerSocket on " + bindAddress.getHostAddress() + ":" + port);
                try (ServerSocket server = new ServerSocket()) {
                    server.setReuseAddress(true);
                    server.bind(new java.net.InetSocketAddress(bindAddress, port), 50);
                    activeServer = server;
                    
                    if (listener != null) {
                        listener.onServerStarted();
                    }
                    
                    while (running && !Thread.currentThread().isInterrupted()) {
                        try (Socket client = server.accept()) {
                            String clientIp = client.getInetAddress().getHostAddress();
                            
                            if (allowedIps != null && !allowedIps.isEmpty()) {
                                if (!allowedIps.contains(clientIp)) {
                                    Log.w(TAG, "Blocked connection from unauthorized IP: " + clientIp);
                                    if (listener != null) {
                                        listener.onClientBlocked(clientIp);
                                    }
                                    continue;
                                }
                            }
                            
                            Log.d(TAG, "Client connected: " + clientIp);
                            
                            if (listener != null) {
                                listener.onClientConnected(clientIp);
                            }

                            String data = listener != null ? listener.onRequestData() : "{}";
                            PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                            pw.print(data);
                            pw.flush();
                            Log.d(TAG, "Sent data to client");
                        }
                    }
                }
            } catch (Exception e) {
                if (running && Thread.currentThread() == listenerThread) {
                    Log.e(TAG, "Error in TCP server", e);
                    if (listener != null) {
                        listener.onServerError(e);
                    }
                }
            }
        });
        listenerThread.start();
    }

    public void stop() {
        running = false;
        if (activeServer != null) {
            try {
                activeServer.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing server socket", e);
            }
            activeServer = null;
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    public boolean isRunning() {
        return running && (
            (activeServer != null && !activeServer.isClosed()) || 
            (listenerThread != null && listenerThread.isAlive())
        );
    }
}
