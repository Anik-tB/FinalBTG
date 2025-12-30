package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PvpLoop {

    private AnimationTimer gameLoop;
    private MainCharacter player;
    private final Set<KeyCode> activeKeys;
    private List<ImageView> obstacles;
    private AnchorPane pvpWorld;
    private int playerId; // Player ID (1 or 2)

    public static double WORLD_WIDTH;
    public static double WORLD_HEIGHT;
    private static final double SCREEN_WIDTH = 912.0;
    private static final double SCREEN_HEIGHT = 624.0;
    private double worldOffsetX = 0;
    private double worldOffsetY = 0;

    private int updateCounter = 0;
    private final int UPDATE_RATE = 2; // Send update every 2 frames
    private double playerPrevX, playerPrevY;
    private List<PvpProjectile> projectiles;
    private MainCharacter opponent;
    private PvpController pvpController;

    public PvpLoop(MainCharacter player, Set<KeyCode> activeKeys, List<ImageView> obstacles, AnchorPane pvpWorld,int playerId, List<PvpProjectile> projectiles, MainCharacter opponent,PvpController pvpController) {
        this.player = player;
        this.pvpWorld = pvpWorld;
        WORLD_WIDTH = pvpWorld.getPrefWidth();
        WORLD_HEIGHT = pvpWorld.getPrefHeight();
        this.activeKeys = activeKeys;
        this.obstacles = obstacles;
        this.playerId = playerId;
        this.playerPrevX = player.getPosX();
        this.playerPrevY = player.getPosY();
        this.projectiles = projectiles;
        this.opponent = opponent;
        this.pvpController = pvpController;
        setupGameLoop();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                long elapsedTime = now - lastUpdate;
                if (elapsedTime >= 155_000_000) { // FPS

                    handleKeyInputs();
                    lastUpdate = now;
                }
            }
        };
    }

    public void handleKeyInputs() {
        // Only handle key inputs for the player controlled by this client
        if (playerId == 1 && player.getCharacterImageView() == pvpWorld.lookup("#mainCharacterImage")) {
            handleMovement();
        } else if (playerId == 2 && player.getCharacterImageView() == pvpWorld.lookup("#opponentCharacterImage")) {
            handleMovement();
        }
        updateProjectiles();
    }

    private void updateProjectiles() {
        for (Iterator<PvpProjectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
            PvpProjectile projectile = iterator.next();
            projectile.update();

            if (projectile.isOffScreen() || projectile.hasCollided(player, opponent, pvpController))  { // Pass player and opponent references
                Platform.runLater(() -> pvpWorld.getChildren().remove(projectile.getProjectileView()));
                iterator.remove();
            }
        }
    }

    private void handleMovement() {
        // Store previous position for update check
        playerPrevX = player.getPosX();
        playerPrevY = player.getPosY();

        // Use delta for time-based movement
        if (activeKeys.contains(KeyCode.UP)) {
            player.updateCharacterImage("UP");
            moveWorld(0, -1, playerPrevX, playerPrevY);
        }
        if (activeKeys.contains(KeyCode.DOWN)) {
            player.updateCharacterImage("DOWN");
            moveWorld(0, 1, playerPrevX, playerPrevY);
        }
        if (activeKeys.contains(KeyCode.LEFT)) {
            player.updateCharacterImage("LEFT");
            moveWorld(-1, 0, playerPrevX, playerPrevY);
        }
        if (activeKeys.contains(KeyCode.RIGHT)) {
            player.updateCharacterImage("RIGHT");
            moveWorld(1, 0, playerPrevX, playerPrevY);
        }
    }

    private void moveWorld(double deltaX, double deltaY, double playerPrevX, double playerPrevY) {
        // Calculate new position using delta and speed
        int playerNewX = player.getPosX() + (int)(deltaX * player.getSpeed()); // Adjust multiplier (60) as needed
        int playerNewY = player.getPosY() + (int)(deltaY * player.getSpeed()); // Adjust multiplier (60) as needed

        // Collision detection code
        if (!isCollidingWithObstacles(playerNewX, playerNewY)) {
            // Keep the player within the world boundaries
            if (playerNewX >= 48 && playerNewX <= WORLD_WIDTH - 48) {
                player.setPosX(playerNewX);
            }
            if (playerNewY >= 48 && playerNewY <= WORLD_HEIGHT - 48) {
                player.setPosY(playerNewY);
            }

//            // World movement and boundary checks (only move the world if the player is near the edge)
//            double newWorldOffsetX = worldOffsetX - (deltaX * player.getSpeed());
//            double newWorldOffsetY = worldOffsetY - (deltaY * player.getSpeed());
//
//            if (newWorldOffsetX <= (WORLD_WIDTH - SCREEN_WIDTH) - 144 && newWorldOffsetX >= SCREEN_WIDTH - WORLD_WIDTH + (WORLD_WIDTH - SCREEN_WIDTH) - 144) {
//                if (deltaX < 0 && player.getPosX() <= WORLD_WIDTH - ((SCREEN_WIDTH / 2) + (48 / 2))) {
//                    worldOffsetX = newWorldOffsetX;
//                    pvpWorld.setTranslateX(worldOffsetX);
//                }
//                if (deltaX > 0 && player.getPosX() >= (SCREEN_WIDTH / 2) - (48 / 2)) {
//                    worldOffsetX = newWorldOffsetX;
//                    pvpWorld.setTranslateX(worldOffsetX);
//                }
//            }
//
//            if (newWorldOffsetY <= (WORLD_HEIGHT - SCREEN_HEIGHT) - 144 && newWorldOffsetY >= SCREEN_HEIGHT - WORLD_HEIGHT + (WORLD_HEIGHT - SCREEN_HEIGHT) - 144) {
//                if (deltaY < 0 && player.getPosY() <= WORLD_HEIGHT - (SCREEN_HEIGHT / 2) - (48 / 2)) {
//                    worldOffsetY = newWorldOffsetY;
//                    pvpWorld.setTranslateY(worldOffsetY);
//                }
//                if (deltaY > 0 && player.getPosY() >= (SCREEN_HEIGHT / 2) - (48 / 2)) {
//                    worldOffsetY = newWorldOffsetY;
//                    pvpWorld.setTranslateY(worldOffsetY);
//                }
//            }

            player.updatePosition(); // Update the ImageView's position
        }
    }

    private boolean isCollidingWithObstacles(int x, int y) {
        double newPlayerMinX = x;
        double newPlayerMaxX = x + player.getCharacterImageView().getFitWidth();
        double newPlayerMinY = y;
        double newPlayerMaxY = y + player.getCharacterImageView().getFitHeight();

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

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }
}