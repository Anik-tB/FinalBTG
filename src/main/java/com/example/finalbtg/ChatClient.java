package com.example.finalbtg;
import java.io.*;
import java.net.*;


public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost"; // Or your server IP
    private static final int SERVER_PORT = 9999;
    private String username;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ChatClient(String username) {
        this.username = username;
    }

    public void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println(username); // Send username to server
            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
// Handle the error, maybe display a message to the user
            System.err.println("Failed to connect to the server.");
        }
    }


    public void sendMessage(String message) {
        output.println(message);
    }

    public String receiveMessage() {
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void disconnectFromServer() {
        try {
            if (output != null) {
                output.println("logout"); // Send logout message to server
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
