package com.example.finalbtg;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.Random;

public class Monster extends AllCharacter{

    // private final int attackRange;
    private  int initialX;
    private  int initialY;
    public String lastDirection = "DOWN";
    private int minX;  // Add minX boundary
    private int minY;  // Add minY boundary
    private int maxX;  // Add maxX boundary
    private int maxY;// Add maxY boundary
    public int hitCount = 0;
    private long lastAttackTime = 0; // Add this line
    public boolean isAlive = true;
    public  int monsterId;
    private List<Monster> monsters; // Add a reference to the list of monsters
    private int initialHealth;
    private int health;




    private ImageView healthBar;

    // Method 1: Using a Constructor with Boundary Parameters
    public Monster(ImageView characterImageView, int speed, int posX, int posY, int attackPower, int health, int attackRange, int minX, int minY, int maxX, int maxY,int monsterId,ImageView healthBar) {
        super(characterImageView, speed, posX, posY, attackPower,attackRange, health);
        // this.attackRange = attackRange;
        this.initialX = posX;
        this.initialY = posY;
        this.initialHealth = health;
        this.health=health;

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.monsterId = monsterId;
        this.healthBar = healthBar;

        updateMonsterImage(monsterId,lastDirection);
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }
    public void setLastAttackTime(long lastAttackTime) { // Add this setter
        this.lastAttackTime = lastAttackTime;
    }
    public void setInitialPosition(int x,int y){
        this.initialX=x;
        this.initialY=y;
    }

    // Method 2: Using a Separate Method to Set Boundaries
    public void setBoundary(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public boolean isPlayerInAttackZone(MainCharacter player) {
        if (!isAlive) {
            return false; // Dead monsters cannot attack
        }
        int distanceX = Math.abs(player.getPosX() - getPosX());
        int distanceY = Math.abs(player.getPosY() - getPosY());
        return distanceX <= getAttackRange() && distanceY <= getAttackRange();
    }
    public void setMonsters(List<Monster> monsters) {
        this.monsters = monsters;
    }

    public ImageView getHealthBar() { // Change return type
        return healthBar;
    }

    public void chasePlayer(MainCharacter player) {
        if (monsterId == 8||monsterId ==9) {
            // This monster doesn't chase, so just return
            return;
        }
        int newPosX = getPosX();
        int newPosY = getPosY();

        // Calculate the angle to the player
        double angle = Math.atan2(player.getPosY() - getPosY(), player.getPosX() - getPosX());

        // Calculate movement based on the angle
        newPosX += (int) (getSpeed() * Math.cos(angle));
        newPosY += (int) (getSpeed() * Math.sin(angle));

        // Collision avoidance with other monsters
        if (monsters != null) {
            for (Monster otherMonster : monsters) {
                if (otherMonster != this && willCollide(newPosX, newPosY, otherMonster)) {
                    Random random = new Random();
                    newPosX += random.nextInt(15) - 7;
                    newPosY += random.nextInt(15) - 7;
                    break;
                }
            }
        }

        // Keep the monster within the defined boundary
        newPosX = Math.max(minX, Math.min(newPosX, maxX));
        newPosY = Math.max(minY, Math.min(newPosY, maxY));

        setPosX(newPosX);
        setPosY(newPosY);


        // Update the monster's image based on the direction
        double degrees = Math.toDegrees(angle);
        degrees = (degrees + 180) % 360;

        if (degrees >= 45 && degrees < 135) {
            lastDirection = "UP";
            updateMonsterImage(monsterId, lastDirection);
        } else if (degrees >= 135 && degrees < 225) {
            lastDirection = "RIGHT"; // Changed from "RIGHT" to "LEFT"
            updateMonsterImage(monsterId, lastDirection);
        } else if (degrees >= 225 && degrees < 315) {
            lastDirection = "DOWN";
            updateMonsterImage(monsterId, lastDirection);
        } else {
            lastDirection = "LEFT"; // Changed from "LEFT" to "RIGHT"
            updateMonsterImage(monsterId, lastDirection);
        }


        updatePosition();
    }

    private boolean willCollide(int newPosX, int newPosY, Monster otherMonster) {
        int thisWidth = (int) getCharacterImageView().getBoundsInParent().getWidth();
        int thisHeight = (int) getCharacterImageView().getBoundsInParent().getHeight();
        int otherWidth = (int) otherMonster.getCharacterImageView().getBoundsInParent().getWidth();
        int otherHeight = (int) otherMonster.getCharacterImageView().getBoundsInParent().getHeight();

        int tolerance = 10; // Adjust this value for the desired margin

        return (newPosX < otherMonster.getPosX() + otherWidth - tolerance &&
                newPosX + thisWidth - tolerance > otherMonster.getPosX() &&
                newPosY < otherMonster.getPosY() + otherHeight - tolerance &&
                newPosY + thisHeight - tolerance > otherMonster.getPosY());
    }


    public boolean isPlayerInActivationZone(MainCharacter player) {
        int activationRadius = 220; // Adjust as needed
        double distance = Math.sqrt(Math.pow(player.getPosX() - initialX, 2) + Math.pow(player.getPosY() - initialY, 2));
        return distance <= activationRadius;
    }


    private void updateMonsterImage(int monsterId,String lastDirection) {
        switch (monsterId) {
            case 1:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m1left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m1right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m1up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m1down.png").toExternalForm()));

                }
                break;
            case 2:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2down.png").toExternalForm()));

                }
                break;
            case 3:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5down.png").toExternalForm()));

                }
                break;
            case 4:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m19left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m19right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m19up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m19down.png").toExternalForm()));

                }
                break;
            case 5:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m18left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m18right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m18up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m18down.png").toExternalForm()));

                }
                break;
            case 6:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29down.png").toExternalForm()));

                }
                break;
            case 7:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m16left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m16right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m16up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m16down.png").toExternalForm()));

                }
                break;
            case 8:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26down.png").toExternalForm()));
                }
                break;
            case 9:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24down.png").toExternalForm()));

                }
                break;

            case 10:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30down.png").toExternalForm()));

                }
                break;
            case 11:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m27left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m27right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m27up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m27down.png").toExternalForm()));

                }
                break;
            case 12:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28down.png").toExternalForm()));

                }
                break;
            case 13:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m4left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m4right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m4up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m4down.png").toExternalForm()));

                }
                break;
            case 14:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m5down.png").toExternalForm()));

                }
                break;
            case 15:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m25left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m25right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m25up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m25down.png").toExternalForm()));

                }
                break;
            case 16:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m2down.png").toExternalForm()));

                }
                break;
            case 17:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m20left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m20right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m20up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m20down.png").toExternalForm()));

                }
                break;
            case 18:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23down.png").toExternalForm()));

                }
                break;
            case 19:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6down.png").toExternalForm()));

                }
                break;
            case 20:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m14left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m14right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m14up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m14down.png").toExternalForm()));

                }
                break;
            case 21:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m24down.png").toExternalForm()));

                }
                break;
            case 22:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29down.png").toExternalForm()));

                }
                break;
            case 23:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26down.png").toExternalForm()));

                }
                break;
            case 24:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31down.png").toExternalForm()));

                }
                break;
            case 25:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m29down.png").toExternalForm()));

                }
                break;
            case 26:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3down.png").toExternalForm()));

                }
                break;
            case 27:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m32left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m32right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m32up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m32down.png").toExternalForm()));

                }
                break;
            case 28:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m10left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m10right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m10up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m10down.png").toExternalForm()));

                }
                break;
            case 29:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m3down.png").toExternalForm()));

                }
                break;
            case 30:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23down.png").toExternalForm()));

                }
                break;
            case 31:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m23down.png").toExternalForm()));

                }
                break;
            case 32:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m28down.png").toExternalForm()));

                }
                break;
            case 33:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m17left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m17right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m17up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m17down.png").toExternalForm()));

                }
                break;
            case 34:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m26down.png").toExternalForm()));

                }
                break;
            case 35:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m31down.png").toExternalForm()));

                }
                break;
            case 36:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m30down.png").toExternalForm()));

                }
                break;
            case 37:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m21left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m21right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m21up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m21down.png").toExternalForm()));

                }
                break;
            case 38:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m8left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m8right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m8up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m8down.png").toExternalForm()));

                }
                break;
            case 39:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m6down.png").toExternalForm()));
                }
                break;
            case 40:
                if (lastDirection.equals("LEFT")) {

                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15down.png").toExternalForm()));

                }
                break;
            case 41:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15down.png").toExternalForm()));

                }
                break;
            case 42:
                if (lastDirection.equals("LEFT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15left.png").toExternalForm()));
                } else if (lastDirection.equals("RIGHT")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15right.png").toExternalForm()));

                } else if (lastDirection.equals("UP")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15up.png").toExternalForm()));

                } else if (lastDirection.equals("DOWN")) {
                    getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/m15down.png").toExternalForm()));

                }
                break;

            default:
                System.err.println("Invalid monster ID: " + monsterId);
                break;
        }
    }

    public void idle() {
        updateMonsterImage(monsterId,lastDirection);
        returnToInitialPosition();
    }

    public void returnToInitialPosition() {
        if (getPosX() < initialX) {
            setPosX(getPosX() + getSpeed());
        } else if (getPosX() > initialX) {
            setPosX(getPosX() - getSpeed());
        }

        if (getPosY() < initialY) {
            setPosY(getPosY() + getSpeed());
        } else if (getPosY() > initialY) {
            setPosY(getPosY() - getSpeed());
        }

        updatePosition();
    }
    public void reset() {
        this.isAlive = true;
        this.hitCount = 0;
        this.setHealth(getInitialHealth());
        this.setPosX(initialX); // Reset position to initialX
        this.setPosY(initialY); // Reset position to initialY
        this.getCharacterImageView().setVisible(true); // Make the monster visible
        updateMonsterImage(monsterId,lastDirection); // Ensure the correct image is displayed
        updatePosition(); // Update the position in the UI
    }
    public int getInitialHealth() {
        return initialHealth;
    }
    public int  getHealth(){
        return health;
    }

    public void setHealth(int health){
        this.health=health;
    }
    public void takeDamage(int damage) {
        if (isAlive) {
            int newHealth = getHealth() - damage;
            setHealth(newHealth);

            if (newHealth <= 0) {
                System.out.println("Monster defeated!");
                getCharacterImageView().setVisible(false);
                isAlive = false;
            }
        }
    }
    public boolean isAlive() { // Add a getter for isAlive
        return isAlive;
    }
}