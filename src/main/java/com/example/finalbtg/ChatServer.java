package com.example.finalbtg;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 9999; // Choose your desired port
    private Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public static boolean isServerRunning = false;



    public void startServer() {
        if (!isServerRunning) { // Check if the server is already running
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                isServerRunning = true; // Set the flag to true
                System.out.println("Server started on port " + PORT);
                // ... rest of your server code ...
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientHandler implements Runnable {
        private String username;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(Socket socket) {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                this.username = input.readLine(); // Get username from client
                System.out.println("Client connected: " + username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = input.readLine()) != null) {
                    if (message.equals("logout")) { // Check for logout message
                        break; // Exit the loop to disconnect the client
                    }
                    System.out.println("Received from " + username + " --> " + message);
                    broadcastMessage(username, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                System.out.println("Client disconnected: " + username);
            }
        }

        public void sendMessage(String message) {
            output.println(message);
        }
    }

    public void broadcastMessage(String sender, String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(sender + ": " + message);
        }
    }
}
