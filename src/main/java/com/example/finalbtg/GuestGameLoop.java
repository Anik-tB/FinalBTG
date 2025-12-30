package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Set;

public class GuestGameLoop {

    @FXML
    private AnchorPane convoAnchorpane;
    private ImageView instructor;
    private AnimationTimer gameLoop;
    private MainCharacter player;
    private final Set<KeyCode> activeKeys;
    private List<ImageView> obstacles;
    private GuestGameController controller;

    public GuestGameLoop(MainCharacter player, Set<KeyCode> activeKeys, List<ImageView> obstacles, AnchorPane convoAnchorpane, ImageView instructor,GuestGameController controller) {
        this.player = player;
        this.activeKeys = activeKeys;
        this.obstacles = obstacles;
        this.convoAnchorpane = convoAnchorpane;
        this.instructor = instructor;
        this.controller = controller;
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

    private void handleKeyInputs() {
        if (activeKeys.contains(KeyCode.UP)) {
            player.moveUp(obstacles);
        }
        if (activeKeys.contains(KeyCode.DOWN)) {
            player.moveDown(obstacles);
        }
        if (activeKeys.contains(KeyCode.LEFT)) {
            player.moveLeft(obstacles);
        }
        if (activeKeys.contains(KeyCode.RIGHT)) {
            player.moveRight(obstacles);
        }
    }

    private void checkPosition() {
        controller.checkLevel1Entry();
        double x = player.getCharacterImageView().getLayoutX();
        double y = player.getCharacterImageView().getLayoutY();

        if(convoAnchorpane!=null && instructor.isVisible()) {
            convoAnchorpane.setVisible(x >= 29*48 && x <= 33*48 && y >= 48*18 && y <= 48*20);
        }
        if((x >= 29*48 && x <= 33*48 && y >= 48*18 && y <= 48*20) && instructor.isVisible()) {
            stop();
        }
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }

}