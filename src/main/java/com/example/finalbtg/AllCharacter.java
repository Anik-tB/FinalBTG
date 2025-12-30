package com.example.finalbtg;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import java.util.ArrayList;
import java.util.List;

public class AllCharacter {

    @FXML
    private AnchorPane gameWorld;

    public void setGameWorld(AnchorPane gameWorld) {
        this.gameWorld = gameWorld;
    }


    private List<ImageView> obstacles = new ArrayList<>();
    ImageView characterImageView;
    private int posX;
    private int posY;
    private int speed;
    private int attackPower;
    private int attackRange;
    int health;


    public AllCharacter(ImageView characterImageView, int speed, int posX, int posY, int attackPower, int attackRange, int health) {
        this.characterImageView = characterImageView;
        this.speed = speed;
        this.posX = posX;
        this.posY = posY;
        this.attackPower = attackPower;
        this.attackRange = attackRange;
        this.health = health;
    }

    public ImageView getCharacterImageView() {
        return characterImageView;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
        updatePosition();
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
        updatePosition();
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        if (attackPower < 0) {
            this.attackPower = 0;
        }
        this.attackPower = attackPower;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health < 0) {
            this.health = 0;
        } else {
            this.health = health;
        }
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        if (attackRange < 0) {
            this.attackRange = 0;
        }
        this.attackRange = attackRange;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (speed < 0) {
            this.speed = -speed;
        }
        this.speed = speed;
    }


    private static final double WORLD_WIDTH = 2112.0;
    private static final double WORLD_HEIGHT = 1680.0;
    private static final double SCREEN_WIDTH = 912.0;
    private static final double SCREEN_HEIGHT = 624.0;

    private double worldOffsetX = 0;
    private double worldOffsetY = 0;


    protected void move(int deltaX, int deltaY, List<ImageView> obstacles) {
        int playerNewX = getPosX() + (deltaX * getSpeed());
        int playerNewY = getPosY() + (deltaY * getSpeed());

        double newPlayerMinX = playerNewX;
        double newPlayerMaxX = playerNewX + characterImageView.getFitWidth();
        double newPlayerMinY = playerNewY;
        double newPlayerMaxY = playerNewY + characterImageView.getFitHeight();

        for (ImageView obstacle : obstacles) {
            if (obstacle != null) {
                double obstacleMinX = obstacle.getLayoutX();
                double obstacleMaxX = obstacleMinX + obstacle.getFitWidth();
                double obstacleMinY = obstacle.getLayoutY();
                double obstacleMaxY = obstacleMinY + obstacle.getFitHeight();

                boolean collisionX = newPlayerMaxX > obstacleMinX && newPlayerMinX < obstacleMaxX - 5;
                boolean collisionY = newPlayerMaxY > obstacleMinY && newPlayerMinY < obstacleMaxY;

                if (collisionX && collisionY) {
                    return;
                }
            }
        }

        if (playerNewX >= 144 + 24 && playerNewX <= WORLD_WIDTH - 144 - 48 - 24) {
            setPosX(playerNewX);
        }
        if (playerNewY >= (144 + 24) && playerNewY <= WORLD_HEIGHT - 144 - 48 - 24) {
            setPosY(playerNewY);
        }

        double newWorldOffsetX = worldOffsetX - (deltaX * getSpeed());
        double newWorldOffsetY = worldOffsetY - (deltaY * getSpeed());

        if (newWorldOffsetX <= (WORLD_WIDTH - SCREEN_WIDTH) - 144 && newWorldOffsetX >= SCREEN_WIDTH - WORLD_WIDTH + (WORLD_WIDTH - SCREEN_WIDTH) - 144) {
            if (deltaX < 0 && getPosX() <= WORLD_WIDTH - ((SCREEN_WIDTH / 2) + (48 / 2.0))) {
                worldOffsetX = newWorldOffsetX;
                gameWorld.setTranslateX(worldOffsetX);
            }
            if (deltaX > 0 && getPosX() >= (SCREEN_WIDTH / 2) - (48 / 2.0)) {
                worldOffsetX = newWorldOffsetX;
                gameWorld.setTranslateX(worldOffsetX);
            }
        }

        if (newWorldOffsetY <= (WORLD_HEIGHT - SCREEN_HEIGHT) - 144 && newWorldOffsetY >= SCREEN_HEIGHT - WORLD_HEIGHT + (WORLD_HEIGHT - SCREEN_HEIGHT) - 144) {
            if (deltaY < 0 && getPosY() <= WORLD_HEIGHT - (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                gameWorld.setTranslateY(worldOffsetY);
            }
            if (deltaY > 0 && getPosY() >= (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                gameWorld.setTranslateY(worldOffsetY);
            }
        }
        updatePosition();
    }


    public void updatePosition() {
        if (characterImageView != null) {
            characterImageView.setLayoutX(posX);
            characterImageView.setLayoutY(posY);
        }
    }

}