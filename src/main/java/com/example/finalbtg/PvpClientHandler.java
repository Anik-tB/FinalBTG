package com.example.finalbtg;

import java.io.*;
import java.net.Socket;

public class PvpClientHandler implements Runnable {
    private Socket clientSocket;
    private PvpServer server;
    private BufferedReader in;
    private PrintWriter out;
    private int playerId;

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public PvpClientHandler(Socket clientSocket, PvpServer server) {
        this.clientSocket = clientSocket;
        this.server = server;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from client " + playerId + ": " + message);

                // Forward messages to the other client if the game is ready
                if (server.getConnectedPlayersCount() == 2) {
                    String finalMessage = message;
                    server.clients.stream()
                            .filter(c -> c != this && c.playerId != this.playerId)
                            .forEach(c -> c.send(finalMessage));
                }
            }
        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                server.removeClient(this);
                System.out.println("Client " + playerId + " disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) {
        out.println(message);
    }
}