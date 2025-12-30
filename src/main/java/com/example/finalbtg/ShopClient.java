package com.example.finalbtg;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ShopClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6666;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ButtonHandler buttonHandler;
    private String username;
    private boolean isConnected = false;

    public ShopClient(ButtonHandler buttonHandler, String username) {
        this.buttonHandler = buttonHandler;
        this.username = username;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;

                String serverMessage = in.readLine();
                if ("USERNAME".equals(serverMessage)) {
                    out.println(username);
                }

                new Thread(this::listenForMessages).start();

                // Request initial item list
                requestItemListUpdate();

            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
            }
        }).start();
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                // Handle incoming messages
                String finalMessage = message;
                Platform.runLater(() -> {
                    buttonHandler.updateShopItemList(finalMessage);
                });
            }
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }


    public void sellItem(String itemId, String price) {
        if (out != null) {
            out.println("SELL:" + itemId + ":" + price);
        }
    }

    public void buyItem(String itemId) {
        if (out != null) {
            out.println("BUY:" + itemId);
        }
    }

    public void requestItemListUpdate() {
        if (out != null) {
            out.println("REQUEST_UPDATE");
        }
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}