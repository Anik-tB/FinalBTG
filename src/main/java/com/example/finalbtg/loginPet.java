package com.example.finalbtg;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Set;

public class loginPet extends AllCharacter{
    private final Set<KeyCode> activeKeys;
    private static final int PET_OFFSET_X = 50; // Adjust for desired distance
    private static final int PET_OFFSET_Y = 0;  // Adjust for vertical offset
    private double targetX, targetY; // Target position for smooth movement

    private boolean movingUp = true; // Initial vertical direction
    private final double movementSpeedFactor = 1; // Adjust this value for slower movement
    private String direction = "DOWN";
    private boolean isSpeedBoostActive = false;
    private AnchorPane gameWorld;
    private List<Projectile> petProjectiles;
    private ImageView PetProjectileImage;
    private List<loginMonster> monsters;
    private long lastAttackTime = 0;
    private final long attackCooldown = 1_000_000_000; // 1 second cooldown// Add this for speed boost

    public loginPet(ImageView characterImageView, int speed, int posX, int posY, Set<KeyCode> activeKeys, List<loginMonster> monsters, AnchorPane gameWorld, ImageView PetProjectileImage) {
        super(characterImageView, speed, posX, posY, 100, 200,1000);
        this.activeKeys = activeKeys;
        this.targetX = posX;
        this.targetY = posY;
        this.gameWorld = gameWorld;
        this.PetProjectileImage = PetProjectileImage;
        this.monsters = monsters;

    }

    public void followPlayer(MainCharacter player) {
        int speed = getSpeed(); // Use this.speed to access the Pet's speed
        if (isSpeedBoostActive) {
            speed = (int) (speed * 2); // Increase speed by 10%
        }
        int playerX = player.getPosX();
        int playerY = player.getPosY();

        targetX = playerX + PET_OFFSET_X;

        // Simple up and down movement
        if (movingUp) {
            targetY = playerY - 30; // Move up
        } else {
            targetY = playerY + 30; // Move down
        }

        // Change direction if the pet reaches a certain distance from the player
        if (Math.abs(getPosY() - playerY) >= 30) {
            movingUp = !movingUp;
        }

        double deltaX = targetX - getPosX();
        double deltaY = targetY - getPosY();

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Use the 'speed' variable here (multiplied by movementSpeedFactor)
        double stepX = (distance > speed * movementSpeedFactor)
                ? (deltaX / distance) * speed * movementSpeedFactor
                : deltaX;
        double stepY = (distance > speed * movementSpeedFactor)
                ? (deltaY / distance) * speed * movementSpeedFactor
                : deltaY;

        // Limit the step size to prevent the pet from moving too far in one frame
        stepX = Math.signum(stepX) * Math.min(Math.abs(stepX), speed * movementSpeedFactor);
        stepY = Math.signum(stepY) * Math.min(Math.abs(stepY), speed * movementSpeedFactor);

        // Update the pet's position smoothly
        setPosX((int) (getPosX() + stepX));
        setPosY((int) (getPosY() + stepY));

        updatePosition();

    }


    public void shoot(List<Projectile> petProjectiles) {
        long now = System.nanoTime();
        if (now - lastAttackTime >= attackCooldown) {
            String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};

            for (String direction : directions) {
                ImageView projectileImage = new ImageView(new Image(getClass().getResource("/image/mprojectile3.png").toExternalForm()));
                projectileImage.setFitWidth(15);
                projectileImage.setFitHeight(15);
                gameWorld.getChildren().add(projectileImage);

                Projectile projectile = new Projectile(projectileImage, 30, getPosX(), getPosY(), 3);
                projectile.setDirection(direction);

                // Offset projectiles slightly from the pet's center
                int offset = 20;
                switch (direction) {
                    case "UP":
                        projectile.setPosition(getPosX(), getPosY() - offset);
                        break;
                    case "DOWN":
                        projectile.setPosition(getPosX(), getPosY() + offset);
                        break;
                    case "LEFT":
                        projectile.setPosition(getPosX() - offset, getPosY());
                        break;
                    case "RIGHT":
                        projectile.setPosition(getPosX() + offset, getPosY());
                        break;
                }

                petProjectiles.add(projectile);
                projectile.setVisible(true);
            }
            lastAttackTime = now;
        }
    }

    public void update(MainCharacter player, List<Projectile> petProjectiles) {
        this.petProjectiles = petProjectiles; // Update the projectile list
        followPlayer(player);

    }






    public String getDirection() { // Add this method
        return direction;
    }

    public void updatePetImage(Set<KeyCode> activeKeys) {
        if (activeKeys.contains(KeyCode.UP)) {
            getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/petup.png").toExternalForm()));
        } else if (activeKeys.contains(KeyCode.DOWN)) {
            getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/petdown.png").toExternalForm()));

        } else if (activeKeys.contains(KeyCode.LEFT)) {
            getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/petleft.png").toExternalForm()));
        } else if (activeKeys.contains(KeyCode.RIGHT)) {
            getCharacterImageView().setImage(new Image(getClass().getResource("/image/monster/petright.png").toExternalForm()));
        }
    }
    public void setSpeedBoostActive(boolean active) {
        this.isSpeedBoostActive = active;
    }


}
