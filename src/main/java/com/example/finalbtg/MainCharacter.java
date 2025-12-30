package com.example.finalbtg;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class MainCharacter extends AllCharacter {
    private PvpController pvpController;

    public MainCharacter(ImageView characterImageView, int speed, int posX, int posY, int attackPower, int attackRange, int health) {
        super(characterImageView, speed, posX, posY, attackPower, attackRange, health);
    }


    private String lastDirection = "DOWN";
    private int upStep = 1;
    private int downStep = 1;
    private int leftStep = 1;
    private int rightStep = 1;


    public void setPosition(int i, int i1) {
        setPosX(i);
        setPosY(i1);
    }

    public String getDirection() { // Add getDirection() method
        return this.lastDirection;
    }


    public void moveUp(List<ImageView> obstacles) {
        if (obstacles != null) {
            updateCharacterImage("UP");
            move(0, -1, obstacles);
        }
    }

    public void moveDown(List<ImageView> obstacles) {
        if (obstacles != null) {
            updateCharacterImage("DOWN");
            move(0, 1, obstacles);
        }

    }

    public void moveLeft(List<ImageView> obstacles) {
        if (obstacles != null) {
            updateCharacterImage("LEFT");
            move(-1, 0, obstacles);
        }
    }

    public void moveRight(List<ImageView> obstacles) {
        if (obstacles != null) {
            updateCharacterImage("RIGHT");
            move(1, 0, obstacles);
        }
    }


    public void updateCharacterImage(String direction) {
        String imageFile = null;

        if (direction.equals("UP")) {
            lastDirection = "UP";
            if (upStep == 1) {
                imageFile = "/image/sprite/up1.png";
                upStep = 2;
            } else {
                imageFile = "/image/sprite/up2.png";
                upStep = 1;
            }
        } else if (direction.equals("DOWN")) {
            lastDirection = "DOWN";
            if (downStep == 1) {
                imageFile = "/image/sprite/down1.png";
                downStep = 2;
            } else {
                imageFile = "/image/sprite/down2.png";
                downStep = 1;
            }
        } else if (direction.equals("LEFT")) {
            lastDirection = "LEFT";
            if (leftStep == 1) {
                imageFile = "/image/sprite/left1.png";
                leftStep = 2;
            } else {
                imageFile = "/image/sprite/left2.png";
                leftStep = 1;
            }
        } else if (direction.equals("RIGHT")) {
            lastDirection = "RIGHT";
            if (rightStep == 1) {
                imageFile = "/image/sprite/right1.png";
                rightStep = 2;
            } else {
                imageFile = "/image/sprite/right2.png";
                rightStep = 1;
            }
        }

        try {
            if (imageFile != null) {
                getCharacterImageView().setImage(new Image(getClass().getResource(imageFile).toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void damagePlayer(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            characterImageView.setVisible(false);
        }
    }
}