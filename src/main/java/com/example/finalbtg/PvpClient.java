package com.example.finalbtg;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PvpClient {
    private static final String SERVER_IP = "127.0.0.1"; // Or your server's IP
    private static final int SERVER_PORT = 5555;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private PvpController pvpController;
    private ButtonHandler buttonHandler;

    private int playerId; // 1 or 2
    private boolean isConnected = false;

    private Stage window;

    public PvpClient(Stage window, ButtonHandler buttonHandler) {
        this.window = window;
        this.buttonHandler = buttonHandler;
    }

    public void sendShootCommand(double startX, double startY, double directionX, double directionY) {
        if (out != null) {
            out.println("SHOOT," + playerId + "," + startX + "," + startY + "," + directionX + "," + directionY);
        }
    }

    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;

            // Listen for server messages on a separate thread
            new Thread(this::listenForServerMessages).start();

        } catch (IOException e) {
            System.err.println("Error connecting to PvP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void listenForServerMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from server: " + message);

                if (message.equals("WAITING_FOR_OPPONENT")) {
                    // Update UI to show waiting message
                    Platform.runLater(() -> {
                        if (buttonHandler != null) {
                            buttonHandler.showWaitingForOpponent();
                        }
                    });
                } else if (message.equals("GAME_READY")) {
                    // Both players are connected, load the PvP scene
                    Platform.runLater(() -> {
                        if (buttonHandler != null) {
                            buttonHandler.hideWaitingForOpponent();
                        }
                        loadPvpScene();
                    });
                } else if (message.startsWith("PLAYER_ID")) {
                    playerId = Integer.parseInt(message.split(",")[1]);
                } else if (message.equals("OPPONENT_DISCONNECTED")) {
                    // Handle opponent disconnection
                    Platform.runLater(() -> {
                        if (pvpController != null) {
                            pvpController.showError("Opponent has disconnected.");
                        }
                        // Optionally, reset or go back to the main screen
                    });
                } else {
                    // Handle other messages (POS, MOVE, STOP)
                    if (pvpController != null) {
                        pvpController.handleServerMessage(message);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
            if (!socket.isClosed()) {
                disconnect(); // Attempt to close the socket
            }
            // Show an error message to the user using Platform.runLater()
            Platform.runLater(() -> {
                if (pvpController != null) {
                    pvpController.showError("Connection to server lost.");
                }
            });
        }
    }

    private void loadPvpScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("pvp.fxml"));
            Scene pvpScene = new Scene(loader.load(), 912, 624);
            pvpController = loader.getController();
            pvpController.setPlayerId(playerId); // Set the player ID
            pvpController.setClient(this);
            pvpController.setWindow(window);

            Platform.runLater(() -> {
                window.setScene(pvpScene);
                window.setTitle("Beyond The Galaxy - PvP");
                //  window.centerOnScreen();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Add this method to send position updates
    public void sendPosition(double x, double y) {
        if (out != null) {
            out.println("POS," + playerId + "," + x + "," + y);
        }
    }

    public void sendMoveCommand(String direction) {
        if (out != null) {
            out.println("MOVE," + playerId + "," + direction);
        }
    }

    public void sendStopCommand() {
        if (out != null) {
            out.println("STOP," + playerId);
        }
    }

    // Method to send a message to the server
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // Method to receive a message from the server
    public String receiveMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setPvpController(PvpController pvpController) {
        this.pvpController = pvpController;
    }

    public void setButtonHandler(ButtonHandler buttonHandler) {
        this.buttonHandler = buttonHandler;
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}