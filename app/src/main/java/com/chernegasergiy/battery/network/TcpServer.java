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
    
    private final int port;
    private final boolean allInterfaces;
    private final Listener listener;

    public interface Listener {
        void onServerStarted();
        void onServerError(Exception e);
        void onClientConnected(String clientIp);
        String onRequestData();
    }

    public TcpServer(int port, boolean allInterfaces, Listener listener) {
        this.port = port;
        this.allInterfaces = allInterfaces;
        this.listener = listener;
    }

    public void start() {
        stop();
        running = true;
        
        listenerThread = new Thread(() -> {
            try {
                InetAddress bindAddress = InetAddress.getByName(allInterfaces ? "0.0.0.0" : "127.0.0.1");
                Log.d(TAG, "Opening ServerSocket on " + bindAddress.getHostAddress() + ":" + port);
                try (ServerSocket server = new ServerSocket(port, 50, bindAddress)) {
                    activeServer = server;
                    
                    if (listener != null) {
                        listener.onServerStarted();
                    }
                    
                    while (running && !Thread.currentThread().isInterrupted()) {
                        try (Socket client = server.accept()) {
                            String clientIp = client.getInetAddress().getHostAddress();
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
                if (running) {
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
        return running && activeServer != null && !activeServer.isClosed();
    }
}
