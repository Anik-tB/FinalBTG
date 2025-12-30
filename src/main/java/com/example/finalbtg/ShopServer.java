package com.example.finalbtg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ShopServer {
    private static final int PORT = 6666;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private static final String ONSALE_FILE = "onSale.txt";
    private static ConcurrentHashMap<String, String> onSaleItems = new ConcurrentHashMap<>();

    public void start() {
        loadOnSaleItems();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Shop Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void broadcastUpdatedItemList(ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendUpdatedItemList();
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        ShopServer server = new ShopServer();
        server.start();
    }

    public synchronized static void addItemForSale(String itemId, String itemDetails) {
        onSaleItems.put(itemId, itemDetails);
        saveOnSaleItems();
    }

    public static String getItemDetails(String itemId) {
        return onSaleItems.get(itemId);
    }

    public static ConcurrentHashMap<String, String> getOnSaleItems() {
        return onSaleItems;
    }

    public synchronized static void removeItemFromSale(String itemId) {
        onSaleItems.remove(itemId);
        saveOnSaleItems();
    }

    private static void loadOnSaleItems() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ONSALE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    onSaleItems.put(parts[0], parts[1] + ":" + parts[2]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading on-sale items: " + e.getMessage());
        }
    }

    private synchronized static void saveOnSaleItems() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ONSALE_FILE))) {
            onSaleItems.forEach((itemId, details) -> writer.println(itemId + ":" + details));
        } catch (IOException e) {
            System.err.println("Error saving on-sale items: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ShopServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket, ShopServer server) {
            this.clientSocket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("USERNAME");
                username = in.readLine();

                System.out.println(username + " has joined the shop.");

                sendUpdatedItemList();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("SELL:")) {
                        String[] parts = inputLine.split(":");
                        if (parts.length == 3) {
                            String itemId = parts[1];
                            String price = parts[2];
                            ShopServer.addItemForSale(itemId, username + ":" + price);
                            server.broadcast("ITEM_ADDED:" + itemId + ":" + username + ":" + price, this); // Broadcast to other clients
                        }
                    } else if (inputLine.startsWith("BUY:")) {
                        String[] parts = inputLine.split(":");
                        if (parts.length == 2) {
                            String itemId = parts[1];
                            String itemDetails = ShopServer.getItemDetails(itemId);
                            if (itemDetails != null) {
                                String sellerName = itemDetails.split(":")[0];
                                String priceString = itemDetails.split(":")[1];

                                if (!sellerName.equals(username)) { // Prevent buying own items
                                    int price = Integer.parseInt(priceString);

                                    // Check if buyer has enough gems
                                    if (hasEnoughGems(username, price)) {
                                        // Deduct gems from buyer
                                        if (updateGems(username, -price)) {
                                            // Add gems to seller
                                            if (updateGems(sellerName, price)) {
                                                ShopServer.removeItemFromSale(itemId);
                                                server.broadcast("ITEM_SOLD:" + itemId + ":" + username, this);
                                                sendMessage("BOUGHT:" + itemId + ":" + itemDetails);
                                                sendMessageToUser(sellerName, "SOLD:" + itemId + ":" + username);

                                                // Add item to buyer's file
                                                if (!addItemToUserFile(username, itemId)) {
                                                    System.err.println("Failed to add item to buyer's file.");
                                                }
                                            } else {
                                                // Revert gem deduction if adding to seller fails
                                                updateGems(username, price);
                                                sendMessage("ERROR: Could not update seller's gems.");
                                            }
                                        } else {
                                            sendMessage("ERROR: Could not update buyer's gems.");
                                        }
                                    } else {
                                        sendMessage("ERROR: Not enough gems to buy this item.");
                                    }
                                }
                            }
                        }
                    }
                         else if (inputLine.startsWith("REQUEST_UPDATE:")) {
                        sendUpdatedItemList(); // Send the updated item list to the client
                    } else {
                        server.broadcast(username + ": " + inputLine, this);
                    }
                }


            } catch (IOException e) {
                System.out.println("Error in client handler for " + username + ": " + e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.removeClient(this);
                System.out.println(username + " has left the shop.");
            }
        }

        private synchronized boolean updateGems(String username, int gemChange) {
            File userFile = new File("user/" + username + ".txt");
            List<String> lines = new ArrayList<>();
            int currentGems = 0;
            boolean updated = false;

            // Read all lines from the file
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        currentGems = Integer.parseInt(line.split(",")[1]);
                        int newGems = currentGems + gemChange;
                        if (newGems >= 0) {
                            line = "gems," + newGems;
                            updated = true;
                        } else {
                            return false; // Not enough gems
                        }
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (!updated) {
                return false; // User file did not contain a gems line
            }

            // Write the modified lines back to the file
            try (PrintWriter writer = new PrintWriter(userFile)) {
                for (String line : lines) {
                    writer.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return updated;
        }

        private synchronized boolean addItemToUserFile(String username, String itemId) {
            File userFile = new File("user/" + username + ".txt");
            List<String> lines = new ArrayList<>();
            boolean updated = false;
            boolean specialCardLineFound = false;

            // Read all lines from the file
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("specialcard,")) {
                        specialCardLineFound = true;
                        // Correctly append the new card ID without a trailing comma
                        if (line.endsWith(",")) {
                            line += itemId;
                        } else {
                            line += "," + itemId;
                        }
                        updated = true;
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // If specialcard line doesn't exist, add it
            if (!specialCardLineFound) {
                lines.add("specialcard," + itemId);
                updated = true;
            }

            // Write the modified lines back to the file
            try (PrintWriter writer = new PrintWriter(userFile)) {
                for (String line : lines) {
                    writer.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return updated;
        }

        private boolean hasEnoughGems(String username, int price) {
            File userFile = new File("user/" + username + ".txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        int gems = Integer.parseInt(line.split(",")[1]);
                        return gems >= price;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false; // Assume not enough gems if file or line not found
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void sendMessageToUser(String targetUsername, String message) {
            for (ClientHandler client : server.clients) {
                if (client.username.equals(targetUsername)) {
                    client.sendMessage(message);
                    break;
                }
            }
        }

        private void sendUpdatedItemList() {
            if (!ShopServer.getOnSaleItems().isEmpty()) {
                StringBuilder itemList = new StringBuilder("ITEM_LIST");
                ShopServer.getOnSaleItems().forEach((itemId, details) -> itemList.append(":").append(itemId).append(":").append(details));
                sendMessage(itemList.toString());
            } else {
                sendMessage("ITEM_LIST_EMPTY");
            }
        }

    }
}