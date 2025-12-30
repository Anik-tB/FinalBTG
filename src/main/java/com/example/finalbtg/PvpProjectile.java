package com.example.finalbtg;

// PvpProjectile.java
import javafx.scene.image.ImageView;

public class PvpProjectile {
    private ImageView projectileView;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double speed;
    private int damage;
    private PvpController pvpController;
    private int shooterId; // Add shooterId to track who fired the projectile

    public PvpProjectile(ImageView projectileView, double startX, double startY, double directionX, double directionY, double speed, int damage, PvpController pvpController, int shooterId) {
        this.projectileView = projectileView;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.velocityX = directionX * speed;
        this.velocityY = directionY * speed;
        this.damage = damage;
        this.pvpController = pvpController;
        this.shooterId = shooterId; // Initialize shooterId

        projectileView.setLayoutX(x);
        projectileView.setLayoutY(y);
    }

    public int getDamage() {
        return damage;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
        projectileView.setLayoutX(x);
        projectileView.setLayoutY(y);
    }

    public boolean isOffScreen() {
        // Check if the projectile is outside the game world boundaries
        return x < 0 || x > PvpLoop.WORLD_WIDTH || y < 0 || y > PvpLoop.WORLD_HEIGHT;
    }

    public boolean hasCollided(MainCharacter player1, MainCharacter player2, PvpController pvpController) {
        // Get projectile bounds
        double projectileMinX = x;
        double projectileMaxX = x + projectileView.getFitWidth();
        double projectileMinY = y;
        double projectileMaxY = y + projectileView.getFitHeight();

        // Get the actual player ID from PvpController
        int localPlayerId = pvpController.getPlayerId();

        // Determine the target player based on shooterId and localPlayerId
        MainCharacter targetPlayer;
        if (shooterId == localPlayerId) {
            targetPlayer = player2; // Shooter is the local player, target the opponent
        } else {
            targetPlayer = player1; // Shooter is the opponent, target the local player
        }

        if (checkCollision(projectileMinX, projectileMaxX, projectileMinY, projectileMaxY, targetPlayer)) {
            // Damage the target player using PvpController.damagePlayer()
            // Pass the targetPlayer's ID (which we deduce from being the target)
            pvpController.damagePlayer((targetPlayer == player1) ? 1 : 2, damage);
            return true;
        }

        return false;
    }

    private boolean checkCollision(double projectileMinX, double projectileMaxX, double projectileMinY, double projectileMaxY, MainCharacter player) {
        // Get player bounds
        double playerMinX = player.getPosX();
        double playerMaxX = player.getPosX() + player.getCharacterImageView().getFitWidth();
        double playerMinY = player.getPosY();
        double playerMaxY = player.getPosY() + player.getCharacterImageView().getFitHeight();

        // Check for intersection
        return (projectileMaxX > playerMinX &&
                projectileMinX < playerMaxX &&
                projectileMaxY > playerMinY &&
                projectileMinY < playerMaxY);
    }

    public ImageView getProjectileView() {
        return projectileView;
    }
}