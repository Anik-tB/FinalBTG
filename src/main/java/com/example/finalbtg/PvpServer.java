package com.example.finalbtg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PvpServer {
    private static final int PORT = 5555;
    private ServerSocket serverSocket;

    private ExecutorService pool = Executors.newFixedThreadPool(2); // Max 2 players
    public List<PvpClientHandler> clients = new ArrayList<>();

    private PvpClientHandler player1 = null;
    private PvpClientHandler player2 = null;
    private boolean isGameReady = false;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("PvP Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                PvpClientHandler clientHandler = new PvpClientHandler(clientSocket, this);

                if (player1 == null) {
                    player1 = clientHandler;
                    player1.setPlayerId(1);
                    player1.send("WAITING_FOR_OPPONENT");
                    player1.send("PLAYER_ID," + 1);
                    System.out.println("Player 1 connected");
                } else if (player2 == null) {
                    player2 = clientHandler;
                    player2.setPlayerId(2);
                    player2.send("PLAYER_ID," + 2);
                    System.out.println("Player 2 connected");

                    // Notify both players that the game is ready
                    player1.send("GAME_READY");
                    player2.send("GAME_READY");
                    isGameReady = true;
                    System.out.println("Game is ready");
                } else {
                    // Reject connection if two players are already connected
                    clientSocket.close();
                    System.out.println("Connection rejected. Game is already full.");
                    continue;
                }
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error in PvP Server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public synchronized void removeClient(PvpClientHandler client) {
        clients.remove(client);
        if (client == player1) {
            player1 = null;
            isGameReady = false;
            System.out.println("Player 1 disconnected");
            // Notify the other player if still connected
            if (player2 != null) {
                player2.send("OPPONENT_DISCONNECTED");
            }
        } else if (client == player2) {
            player2 = null;
            isGameReady = false;
            System.out.println("Player 2 disconnected");
            // Notify the other player if still connected
            if (player1 != null) {
                player1.send("OPPONENT_DISCONNECTED");
            }
        }
    }

    public synchronized int getConnectedPlayersCount() {
        return clients.size();
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            pool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PvpServer server = new PvpServer();
        server.start();
    }
}