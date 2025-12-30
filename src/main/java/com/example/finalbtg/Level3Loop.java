package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Set;

public class Level3Loop {
    private AnimationTimer gameLoop;
    private MainCharacter player;
    private final Set<KeyCode> activeKeys;
    private List<ImageView> obstacles;
    private AnchorPane level3World;

    private static  double WORLD_WIDTH;
    private static  double WORLD_HEIGHT;
    private static final double SCREEN_WIDTH = 912.0;
    private static final double SCREEN_HEIGHT = 624.0;
    private double worldOffsetX = 0;
    private double worldOffsetY = 0;

    public Level3Loop(MainCharacter player, Set<KeyCode> activeKeys, List<ImageView> obstacles, AnchorPane level3World ) {
        this.player = player;
        this.level3World = level3World;
        WORLD_WIDTH = this.level3World.getPrefWidth();
        WORLD_HEIGHT = this.level3World.getPrefHeight();
        this.activeKeys = activeKeys;
        this.obstacles = obstacles;
        setupGameLoop();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 150_000_000) {
                    handleKeyInputs();
                    checkPosition();

                    lastUpdate = now;
                }
            }
        };
    }
    private void checkPosition() {

    }

    public void handleKeyInputs() {
        if (activeKeys.contains(KeyCode.UP)) {
            player.updateCharacterImage("UP");
            moveWorld(0, -1);
        }
        if (activeKeys.contains(KeyCode.DOWN)) {
            player.updateCharacterImage("DOWN");
            moveWorld(0, 1);
        }
        if (activeKeys.contains(KeyCode.LEFT)) {
            player.updateCharacterImage("LEFT");
            moveWorld(-1, 0);
        }
        if (activeKeys.contains(KeyCode.RIGHT)) {
            player.updateCharacterImage("RIGHT");
            moveWorld(1, 0);
        }
    }

    private void moveWorld(int deltaX, int deltaY) {
        int playerNewX = player.getPosX() + (deltaX * player.getSpeed());
        int playerNewY = player.getPosY() + (deltaY * player.getSpeed());
        double newPlayerMinX = playerNewX;
        double newPlayerMaxX = playerNewX + player.getCharacterImageView().getFitWidth();
        double newPlayerMinY = playerNewY;
        double newPlayerMaxY = playerNewY + player.getCharacterImageView().getFitHeight();

        for (ImageView obstacle : obstacles) {
            if (obstacle != null) {
                double obstacleMinX = obstacle.getLayoutX();
                double obstacleMaxX = obstacleMinX + obstacle.getFitWidth();
                double obstacleMinY = obstacle.getLayoutY();
                double obstacleMaxY = obstacleMinY + obstacle.getFitHeight();

                boolean collisionX = newPlayerMaxX > obstacleMinX && newPlayerMinX < obstacleMaxX;
                boolean collisionY = newPlayerMaxY > obstacleMinY && newPlayerMinY < obstacleMaxY;

                if (collisionX && collisionY) {
                    return;
                }
            }
        }



        if (playerNewX >= 144 + 24 && playerNewX <= WORLD_WIDTH - 144 - 48 - 24) {
            player.setPosX(playerNewX);
        }
        if (playerNewY >= (144 + 24) && playerNewY <= WORLD_HEIGHT - 144 - 48 - 24) {
            player.setPosY(playerNewY);
        }

        double newWorldOffsetX = worldOffsetX - (deltaX * player.getSpeed());
        double newWorldOffsetY = worldOffsetY - (deltaY * player.getSpeed());

        if (newWorldOffsetX <= (WORLD_WIDTH-SCREEN_WIDTH)-144 && newWorldOffsetX >= SCREEN_WIDTH - WORLD_WIDTH + (WORLD_WIDTH-SCREEN_WIDTH)-144) {
            if (deltaX < 0 && player.getPosX() <= WORLD_WIDTH - ((SCREEN_WIDTH / 2) + (48 / 2))) {
                worldOffsetX = newWorldOffsetX;
                level3World.setTranslateX(worldOffsetX);
            }
            if (deltaX > 0 && player.getPosX() >= (SCREEN_WIDTH / 2) - (48 / 2)) {
                worldOffsetX = newWorldOffsetX;
                level3World.setTranslateX(worldOffsetX);
            }
        }

        if (newWorldOffsetY <= (WORLD_HEIGHT-SCREEN_HEIGHT)-144 && newWorldOffsetY >= SCREEN_HEIGHT - WORLD_HEIGHT + (WORLD_HEIGHT-SCREEN_HEIGHT)-144) {
            if (deltaY < 0 && player.getPosY() <= WORLD_HEIGHT - (SCREEN_HEIGHT / 2) - (48 / 2)) {
                worldOffsetY = newWorldOffsetY;
                level3World.setTranslateY(worldOffsetY);
            }
            if (deltaY > 0 && player.getPosY() >= (SCREEN_HEIGHT / 2) - (48 / 2)) {
                worldOffsetY = newWorldOffsetY;
                level3World.setTranslateY(worldOffsetY);
            }
        }
        player.updatePosition();
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }
}

