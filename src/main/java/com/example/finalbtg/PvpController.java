package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javafx.scene.control.Button;

public class PvpController {
    @FXML
    private AnchorPane pvpWorld, exitConfirmAnchorPane, playerConditionAnchorPane, opponentConditionAnchorPane1,youWinAnchorPane, youLoseAnchorPane;
    @FXML
    private Button exitButton;
    @FXML
    private ImageView mainCharacterImage, opponentCharacterImage;

    @FXML
    private Label healthLabel, attackPowerLabel, opponentHealthLabel, opponentAttackPowerLabel;
    private Stage window;
    private PvpLoop pvpLoop;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private List<ImageView> obstacles = new ArrayList<>();
    private static final double tileSize = 48.0;
    private MainCharacter player, opponent;

    private List<PvpProjectile> projectiles = new ArrayList<>();

    private int playerId; // 1 for main, 2 for opponent
    private PvpClient pvpClient;

    private AnimationTimer opponentAnimationTimer;
    private long lastOpponentUpdate = 0;
    private static final long OPPONENT_UPDATE_INTERVAL = 160_000_000; // ~60 FPS in nanoseconds
    private static final double PROJECTILE_SPEED = 20;

    public int getPlayerId() {
        return playerId;
    }


    public void handleServerMessage(String message) {
        if (message.startsWith("POS")) {
            // Update opponent position
            String[] parts = message.split(",");
            int opponentId = Integer.parseInt(parts[1]);
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);

            // Assuming you have a method to update opponent position
            updateOpponentPosition(opponentId, x, y);
        } else if (message.startsWith("MOVE")) {
            // Move opponent
            String[] parts = message.split(",");
            String direction = parts[2];
            moveOpponent(direction);
        } else if (message.startsWith("STOP")) {
            // Stop opponent
            stopOpponent();
        } else if (message.startsWith("SHOOT")) {
            // Create a new projectile
            String[] parts = message.split(",");
            int shooterId = Integer.parseInt(parts[1]);
            double startX = Double.parseDouble(parts[2]);
            double startY = Double.parseDouble(parts[3]);
            double directionX = Double.parseDouble(parts[4]);
            double directionY = Double.parseDouble(parts[5]);

            createProjectile(shooterId, startX, startY, directionX, directionY);
        } else if (message.equals("OPPONENT_DISCONNECTED")) {
            // Handle opponent disconnection
            Platform.runLater(() -> {
                showError("Opponent has disconnected.");
                // Optionally, reset the game or go back to the main screen
            });
        }
    }


    private void createProjectile(int shooterId, double startX, double startY, double directionX, double directionY) {
        Image projectileImage = new Image(getClass().getResource("/image/monsterProjectile.png").toExternalForm());
        ImageView projectileView = new ImageView(projectileImage);
        projectileView.setFitWidth(20);
        projectileView.setFitHeight(20);

        // Adjust starting position based on direction
        double offsetX = 0;  // Horizontal offset
        double offsetY = 0;  // Vertical offset

        // Constants for adjustments (you might need to fine-tune these values)
        final double DOWN_SHOOT_OFFSET_X = 5;  // Adjust to the right for downward shots
        final double SIDE_SHOOT_OFFSET_Y = 3;   // Adjust downwards for left/right shots

        if (directionY > 0) {  // Shooting downwards
            offsetX = DOWN_SHOOT_OFFSET_X;
        }
        if (directionX != 0) { // Shooting left or right
            offsetY = SIDE_SHOOT_OFFSET_Y;
        }

        // Determine damage based on shooter
        int damage;
        if (shooterId == playerId) {
            damage = player.getAttackPower();
        } else {
            damage = opponent.getAttackPower();
        }

        // Get the correct opponent player based on shooterId
        MainCharacter targetPlayer = (shooterId == playerId) ? opponent : player;

        PvpProjectile projectile = new PvpProjectile(projectileView, startX, startY, directionX, directionY, PROJECTILE_SPEED, damage, this, shooterId);
        // Apply the offsets to the starting position
        startX += offsetX;
        startY += offsetY;

        projectiles.add(projectile);
        Platform.runLater(() -> pvpWorld.getChildren().add(projectileView));
        projectileView.toFront();
    }

    public void updateHealthLabel(MainCharacter player) {
        if (player == this.player) {
            healthLabel.setText(String.valueOf(player.getHealth()));
        } else if (player == this.opponent) {
            opponentHealthLabel.setText(String.valueOf(player.getHealth()));
        }
    }

    public void damagePlayer(int playerId, int damage) {
        Platform.runLater(() -> {
            // Determine which player to damage based on playerId
            MainCharacter playerToDamage = (playerId == this.playerId) ? player : opponent;

            playerToDamage.damagePlayer(damage);

            // Update the correct health label
            if (playerToDamage == player) {
                healthLabel.setText(String.valueOf(player.getHealth()));
            } else {
                opponentHealthLabel.setText(String.valueOf(opponent.getHealth()));
            }

            // Check for game over condition
            if (playerToDamage.getHealth() <= 0) {
                if (playerToDamage == player) {
                    youLoseAnchorPane.setVisible(true);
                } else {
                    youWinAnchorPane.setVisible(true);
                }

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
                    Scene scene = new Scene(loader.load(), 912, 624);

                    Object controller = loader.getController();
                    if (controller != null && controller instanceof ButtonHandler buttonHandler) {
                        buttonHandler.setWindow(window);
                    }

                    window.setScene(scene);
                    window.setTitle("Beyond The Galaxy");
                    // window.centerOnScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void showError(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void updateOpponentPosition(int opponentId, double x, double y) {
        if (opponentId == 1 || opponentId == 2) {
            long now = System.nanoTime();
            if (now - lastOpponentUpdate >= OPPONENT_UPDATE_INTERVAL) {
                Platform.runLater(() -> {
                    if (opponentId == 1) {
                        mainCharacterImage.setLayoutX(x - mainCharacterImage.getFitWidth() / 2);
                        mainCharacterImage.setLayoutY(y - mainCharacterImage.getFitHeight() / 2);
                    } else if (opponentId == 2) {
                        opponentCharacterImage.setLayoutX(x - opponentCharacterImage.getFitWidth() / 2);
                        opponentCharacterImage.setLayoutY(y - opponentCharacterImage.getFitHeight() / 2);
                    }
                });
                lastOpponentUpdate = now;
            }
        }
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;

        // Adjust positions based on playerId
        if (playerId == 1) {
            // Player 1 (mainCharacterImage) starts on the left
            player = new MainCharacter(mainCharacterImage, 25, 144, 300, 100, 200, 1200);
            opponent = new MainCharacter(opponentCharacterImage, 25, 760, 300, 100, 200, 1200);
        } else {
            // Player 2 (opponentCharacterImage) starts on the right
            player = new MainCharacter(opponentCharacterImage, 25, 760, 300, 100, 200, 1200);
            opponent = new MainCharacter(mainCharacterImage, 25, 144, 300, 100, 200, 1200);
        }

        player.setGameWorld(pvpWorld);
        opponent.setGameWorld(pvpWorld);

        pvpLoop = new PvpLoop(player, activeKeys, obstacles, pvpWorld, playerId, projectiles, opponent, this);
        pvpLoop.start();

    }

    public void setClient(PvpClient pvpClient) {
        this.pvpClient = pvpClient;

        // Check if pvpLoop is null before starting
        if (pvpLoop != null) {
            pvpLoop.start();
        }
    }

    @FXML
    public void initialize() {
        try {
            fillMoonSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Make the condition anchor panes visible
        playerConditionAnchorPane.setVisible(true);
        opponentConditionAnchorPane1.setVisible(true);
        youWinAnchorPane.setVisible(false);
        youLoseAnchorPane.setVisible(false);
        // Initialize labels with default values
        healthLabel.setText("1200");
        attackPowerLabel.setText("100");
        opponentHealthLabel.setText("1200");
        opponentAttackPowerLabel.setText("100");

        // Set up key event handlers on the scene
        Platform.runLater(() -> {
            Scene scene = pvpWorld.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(this::handleKeyPressed);
                scene.setOnKeyReleased(this::handleKeyReleased);
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (!activeKeys.contains(event.getCode())) {
            activeKeys.add(event.getCode());
            //got the keycode as string
            String key = event.getCode().toString();
            pvpClient.sendMoveCommand(key);
        }
        // Handle shooting separately
        else if (event.getCode() == KeyCode.F) {
            shootProjectile();
        }
    }

    private void shootProjectile() {
        // 1. Get player's current position and facing direction
        double startX = player.getPosX() + player.getCharacterImageView().getFitWidth() / 2;
        double startY = player.getPosY() + player.getCharacterImageView().getFitHeight() / 2;
        double directionX = 0, directionY = 0;

        // Determine direction based on player's current image
        // (You'll need to adjust this based on how you determine player facing direction)
        if (player.getCharacterImageView().getImage().getUrl().contains("up")) {
            directionY = -1;
        } else if (player.getCharacterImageView().getImage().getUrl().contains("down")) {
            directionY = 1;
        } else if (player.getCharacterImageView().getImage().getUrl().contains("left")) {
            directionX = -1;
        } else if (player.getCharacterImageView().getImage().getUrl().contains("right")) {
            directionX = 1;
        }

        // 2. Send shoot command to server
        pvpClient.sendShootCommand(startX, startY, directionX, directionY);

        // 3. (Optional) Create projectile locally for immediate feedback (client-side prediction)
        // You might want to add this for a more responsive feel, but make sure to reconcile with server updates
        createProjectile(playerId, startX, startY, directionX, directionY);
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.F) {
            activeKeys.remove(event.getCode());
            pvpClient.sendStopCommand();
        }
    }

    public void moveOpponent(String direction) {
        Platform.runLater(() -> {
            if (opponentAnimationTimer != null) {
                opponentAnimationTimer.stop();
            }

            opponentAnimationTimer = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    // Check if enough time has passed since the last update
                    if (now - lastOpponentUpdate >= OPPONENT_UPDATE_INTERVAL) {
                        switch (direction) {
                            case "UP":
                                opponent.updateCharacterImage("UP");
                                moveOpponentHelper(0, -opponent.getSpeed());
                                break;
                            case "DOWN":
                                opponent.updateCharacterImage("DOWN");
                                moveOpponentHelper(0, opponent.getSpeed());
                                break;
                            case "LEFT":
                                opponent.updateCharacterImage("LEFT");
                                moveOpponentHelper(-opponent.getSpeed(), 0);
                                break;
                            case "RIGHT":
                                opponent.updateCharacterImage("RIGHT");
                                moveOpponentHelper(opponent.getSpeed(), 0);
                                break;
                            default:
                                break;
                        }
                        lastOpponentUpdate = now; // Update the last update time
                    }
                }
            };
            opponentAnimationTimer.start();
        });
    }



    public void stopOpponent() {
        if (opponentAnimationTimer != null) {
            opponentAnimationTimer.stop();
        }
    }

    // Helper method to update opponent position, checking for boundaries and obstacles
    private void moveOpponentHelper(int deltaX, int deltaY) {
        int newX = opponent.getPosX() + deltaX;
        int newY = opponent.getPosY() + deltaY;

        // Boundary and collision checks (similar to your PvpLoop)
        if (newX >= 48 && newX <= pvpWorld.getPrefWidth() - 48) {
            if (newY >= 48 && newY <= pvpWorld.getPrefHeight() - 48) {
                if (!isCollidingWithObstacles(newX, newY)) {
                    opponent.setPosX(newX);
                    opponent.setPosY(newY);
                    opponent.updatePosition();
                }
            }
        }
    }

    private boolean isCollidingWithObstacles(int x, int y) {
        double newPlayerMinX = x;
        double newPlayerMaxX = x + opponent.getCharacterImageView().getFitWidth();
        double newPlayerMinY = y;
        double newPlayerMaxY = y + opponent.getCharacterImageView().getFitHeight();

        for (ImageView obstacle : obstacles) {
            if (obstacle != null) {
                double obstacleMinX = obstacle.getLayoutX();
                double obstacleMaxX = obstacleMinX + obstacle.getFitWidth();
                double obstacleMinY = obstacle.getLayoutY();
                double obstacleMaxY = obstacleMinY + obstacle.getFitHeight();

                boolean collisionX = newPlayerMaxX > obstacleMinX && newPlayerMinX < obstacleMaxX;
                boolean collisionY = newPlayerMaxY > obstacleMinY && newPlayerMinY < obstacleMaxY;

                if (collisionX && collisionY) {
                    return true;
                }
            }
        }
        return false;
    }


    public void setWindow(Stage stage) {
        this.window = stage;
    }

    private void fillMoonSurface() throws IOException {
        String mapFilePath = "pvpmap.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            Image road = new Image(getClass().getResource("/image/tile/17.png").toExternalForm());
            Image road2 = new Image(getClass().getResource("/image/tile/18.png").toExternalForm());
            Image wall = new Image(getClass().getResource("/image/tile/20.png").toExternalForm());
            Image wall2 = new Image(getClass().getResource("/image/tile/21.png").toExternalForm());
            Image stone = new Image(getClass().getResource("/image/tile/19.png").toExternalForm());
            Image stone2 = new Image(getClass().getResource("/image/tile/22.png").toExternalForm());
            Image stars = new Image(getClass().getResource("/image/tile/s.jpg").toExternalForm());

            Canvas canvas = new Canvas(912, 624);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double tileSize = 48.0;
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char tileType = line.charAt(col);
                    Image tileImage = null;

                    if (tileType == '1') {
                        tileImage = road;

                    }
                    if (tileType == '2') {
                        tileImage = road2;

                    }
                    if (tileType == '3') {
                        tileImage = wall;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '4') {
                        tileImage = wall2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }

                    if (tileType == '5') {
                        tileImage = stone;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '6') {
                        tileImage = stone2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == 's') {
                        tileImage = stars;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }

                    if (tileImage != null) {
                        gc.drawImage(tileImage, col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
                row++;
            }
            pvpWorld.getChildren().add(canvas);
            mainCharacterImage.toFront();
            opponentCharacterImage.toFront();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRockObstacle(double x, double y) {
        ImageView rockObstacle = new ImageView(new Image(getClass().getResource("/image/tile/4.png").toExternalForm()));
        rockObstacle.setFitWidth(tileSize);
        rockObstacle.setFitHeight(tileSize);
        rockObstacle.setLayoutX(x);
        rockObstacle.setLayoutY(y);

        obstacles.add(rockObstacle);
        pvpWorld.getChildren().add(rockObstacle);
    }


    @FXML
    private void exitButtonAction() {
        exitConfirmAnchorPane.setVisible(true);
        exitButton.setVisible(false);
    }

    @FXML
    private void exitYesButtonAction() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
            Scene scene = new Scene(loader.load(), 912, 624);

            Object controller = loader.getController();
            if (controller != null && controller instanceof ButtonHandler buttonHandler) {
                buttonHandler.setWindow(window);
            }

            window.setScene(scene);
            window.setTitle("Beyond The Galaxy");
            window.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exitNoButtonAction() {
        exitConfirmAnchorPane.setVisible(false);
        exitButton.setVisible(true);
    }
}