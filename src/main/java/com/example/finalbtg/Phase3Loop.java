package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.*;

public class Phase3Loop {

    @FXML
    private ImageView gate;

    private ImageView monsterBoomImage, projectileImage;

    private AnimationTimer gameLoop;
    private final Set<KeyCode> activeKeys;
    private MainCharacter player;
    private List<Monster> monsters = new ArrayList<>();
    private List<ImageView> obstacles;
    private AnchorPane phase3World;
    private Phase3Controller controller;
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> monsterProjectiles = new ArrayList<>(); // Separate list for monster projectiles
    private List<Projectile> petProjectiles = new ArrayList<>();
    private Set<Monster> deadMonsters = new HashSet<>();

    private int projectileCount = 0;
    private long lastAttackTime = 0;
    private final long attackCooldown = 1_000_000_000;
    private final long monsterAttackCooldown = 1_000_000_000;// 5 seconds for monster
    private Timer monster32RespawnTimer;
    private final long monster32RespawnDelay = 10_000; // 30 seconds
    private Timer monster33RespawnTimer; // Separate timer for monster 9
    private final long monster33RespawnDelay = 15_000;// Tracks if the boost is active

    private boolean isGateOpen = false; // Flag to track gate status
    private boolean isProjectileLaunched = false;
    private boolean isSpeedBoostActive = false;
    private boolean monsterRespawned32 = false;
    private boolean monsterRespawned33 = false;
    protected boolean isSpeedBoostPickedUp = false;

    private static double WORLD_WIDTH;
    private static double WORLD_HEIGHT;
    private static final double SCREEN_WIDTH = 912.0;
    private static final double SCREEN_HEIGHT = 624.0;
    private double worldOffsetX = 0;
    private double worldOffsetY = 0;

    private Pet pet;
    private AnchorPane fixAnchorPane;



    public Phase3Loop(MainCharacter player, Set<KeyCode> activeKeys, List<ImageView> obstacles, AnchorPane phase3World, List<Monster> monster,Pet pet, ImageView monsterBoomImage, Phase3Controller controller, ImageView gate,AnchorPane fixAnchorPane) {
        this.player = player;
        this.phase3World = phase3World;
        // Add mouse event handler to the scene
        phase3World.setOnMousePressed(this::handleMousePress);
        this.WORLD_WIDTH = phase3World.getPrefWidth();
        this.WORLD_HEIGHT = phase3World.getPrefHeight();
        this.activeKeys = activeKeys;
        this.obstacles = obstacles;
        this.monsters = monster;
        for (Monster m : this.monsters) {
            m.setMonsters(this.monsters);
        }
        this.pet = pet;
        this.gate = gate;
        this.monsterBoomImage = monsterBoomImage;
        this.controller = controller;
        this.fixAnchorPane = fixAnchorPane;
        setupGameLoop();

    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            private boolean oxygenCollected = false;
            private boolean healthCollected = false;

            @Override
            public void handle(long now) {

                boolean monsterNearPet = monsters.stream()
                        .anyMatch(monster -> isMonsterNearPet(monster, pet));

                if (monsterNearPet && controller.isPetVisible) {
                    pet.shoot(petProjectiles);
                }
                if (now - lastUpdate >= 150_000_000) {
                    pet.followPlayer(player);
                    pet.updatePetImage(activeKeys);
                    pet.update(player, petProjectiles);
                    controller.updatePetVisibility();

                    // Check for speed booster, oxygen and health collections......................

                    if (!isSpeedBoostPickedUp && controller.getSpeedBoostImage().isVisible() && player.getCharacterImageView().getBoundsInParent().intersects(controller.getSpeedBoostImage().getBoundsInParent())) {

                        isSpeedBoostPickedUp = true;
                        controller.getSpeedBoostImage().setVisible(false);
                        // Start the speed boost timer immediately when picked up
                        if (controller != null) {
                            controller.startSpeedBoostTimer();
                        } else {
                            System.err.println("Error: controler is null in GuestGameLoop1");
                        }
                    }
                    if (!healthCollected && controller.getHealth().isVisible() && player.getCharacterImageView().getBoundsInParent().intersects(controller.getHealth().getBoundsInParent())) {

                        increaseHealth(200); // Increase health by 200
                        healthCollected = true;
                        controller.getHealth().setVisible(false);
                        startMonster33RespawnTimer(); // You might want to start the respawn timer here as well
                    }
                    if (!oxygenCollected && controller.getOxygen().isVisible() && player.getCharacterImageView().getBoundsInParent().intersects(controller.getOxygen().getBoundsInParent())) {

                        increaseOxygen(10);
                        oxygenCollected = true; // Set the flag to true after collecting oxygen
                        controller.getOxygen().setVisible(false); // Hide the oxygen image
                        // Start the respawn timer when oxygen is collected
                        startMonster32RespawnTimer();
                    }
                    // Invisibility Power-up Collision
                    if (!controller.isPowerupCollected && controller.invisibilityPowerup.isVisible() &&
                            player.getCharacterImageView().getBoundsInParent().intersects(controller.invisibilityPowerup.getBoundsInParent())) {

                        controller.isPowerupCollected = true;
                        controller.invisibilityPowerup.setVisible(false);
                    }

                    // Player projectiles....................................................................................

                    for (int i = projectiles.size() - 1; i >= 0; i--) {
                        Projectile projectile = projectiles.get(i);
                        if (projectile.isVisible()) {
                            projectile.move();

                            boolean projectileRemoved = false;
                            Iterator<Monster> monsterIterator = monsters.iterator();
                            while (monsterIterator.hasNext()) {
                                Monster monster = monsterIterator.next();
                                if (monster.isAlive() && projectile.projectileImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                                    // Check if the player's direction matches the monster's direction
                                    if (player.getDirection().equals(getMonsterDirection(monster))) {
                                        monster.takeDamage(50);

                                        if (!monster.isAlive()) {
                                            monster.getCharacterImageView().setVisible(false);
                                            monster.getHealthBar().setVisible(false);
                                            if (monster.monsterId == 33) {
                                                if (!monsterRespawned33) {
                                                    controller.getHealth().setVisible(true);
                                                    monsterRespawned33 = true;
                                                }
                                                obstacles.remove(monster.getCharacterImageView());
                                                healthCollected = false;

                                                startMonster33RespawnTimer(); // Start monster 9 respawn timer
                                            }
                                            if (monster.monsterId == 32) {
                                                if (!monsterRespawned32) { // Check the flag here
                                                    controller.getOxygen().setVisible(true);
                                                    monsterRespawned32 = true; // Set the flag to true
                                                }
                                                obstacles.remove(monster.getCharacterImageView());
                                                oxygenCollected = false; // Reset oxygenCollected flag here
                                            }
                                            showMonsterExplosion(monster);
                                            monsterIterator.remove();
                                        }
                                    }
                                    projectile.setVisible(false);
                                    projectiles.remove(i);
                                    projectileRemoved = true;
                                    break;
                                } else {
                                    deadMonsters.add(monster);
                                }
                            }
                            if (!projectileRemoved && hasProjectileTraveledDistance(projectile, 100)) {
                                projectile.setVisible(false);
                                projectiles.remove(i);
                            }
                        }
                    }


                    // Pet projectiles........................................................................................

                    Iterator<Projectile> petProjectileIterator = petProjectiles.iterator();
                    while (petProjectileIterator.hasNext()) {
                        Projectile projectile = petProjectileIterator.next();
                        if (projectile.isVisible()) {
                            projectile.move();

                            boolean projectileRemoved = false;

                            // Check collision with monsters
                            Iterator<Monster> monsterIterator = monsters.iterator();
                            while (monsterIterator.hasNext()) {
                                Monster monster = monsterIterator.next();
                                if (monster.isAlive() && projectile.projectileImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                                    monster.takeDamage(10); // Adjust damage as needed

                                    if (!monster.isAlive()) {
                                        monster.getCharacterImageView().setVisible(false);
                                        monster.getHealthBar().setVisible(false);

                                        // Handle specific monster behaviors
                                        if (monster.monsterId == 33) {
                                            if (!monsterRespawned33) {
                                                controller.getHealth().setVisible(true);
                                                monsterRespawned33 = true;
                                            }
                                            obstacles.remove(monster.getCharacterImageView());
                                            healthCollected = false;

                                            startMonster33RespawnTimer(); // Start monster respawn timer
                                        }
                                        if (monster.monsterId == 32) {
                                            if (!monsterRespawned32) {
                                                controller.getOxygen().setVisible(true);
                                                monsterRespawned32 = true;
                                            }
                                            obstacles.remove(monster.getCharacterImageView());
                                            oxygenCollected = false;
                                        }

                                        // Show explosion effect
                                        showMonsterExplosion(monster);
                                    }

                                    // Mark projectile for removal
                                    projectile.setVisible(false);
                                    petProjectileIterator.remove(); // Safely remove the projectile
                                    projectileRemoved = true;
                                    break;
                                }
                            }

                            // Remove projectile if it has traveled a certain distance
                            if (!projectileRemoved && hasProjectileTraveledDistance(projectile, 100)) {
                                projectile.setVisible(false);
                                petProjectileIterator.remove(); // Safely remove the projectile
                            }
                        }
                    }

                    // Update monster health bars
                    for (Monster monster : monsters) {
                        updateMonsterHealthBar(monster);
                    }


                    // Monster projectiles................................................................................

                    for (int i = monsterProjectiles.size() - 1; i >= 0; i--) {
                        Projectile projectile = monsterProjectiles.get(i);
                        if (projectile.isVisible()) {
                            projectile.move();

                            if (projectile.projectileImage.getBoundsInParent().intersects(player.getCharacterImageView().getBoundsInParent())) {
                                if (projectile.Id == 5) {
                                    controller.decreaseHealth(10);
                                }
                                projectile.setVisible(false);
                                monsterProjectiles.remove(i);
                            }

                            if (hasProjectileTraveledDistance(projectile, 300)) {
                                projectile.setVisible(false);
                                monsterProjectiles.remove(i);
                            }
                        }
                    }


                    // Monster logic with zone-based activation and removal of dead monsters.......................................

                    Iterator<Monster> monsterIterator = monsters.iterator(); // Create iterator
                    while (monsterIterator.hasNext()) {
                        Monster monster = monsterIterator.next();
                        if(monster.monsterId==41||monster.monsterId==42){
                            if (controller.isAttackConfirm()) {
                                monster.getCharacterImageView().setVisible(true);
                                monster.getHealthBar().setVisible(true);

                                if (monster.isPlayerInAttackZone(player) &&
                                        now - monster.getLastAttackTime() >= monsterAttackCooldown) {

                                    launchMonsterProjectile(monster, player);
                                    monster.setLastAttackTime(now);
                                }

                                monster.chasePlayer(player);
                                monster.updatePosition();

                            } else {
                                monster.getCharacterImageView().setVisible(false);
                                monster.getHealthBar().setVisible(false);
                                continue;
                            }
                        }

                        if (monster.isAlive()) {
                            if (!controller.isInvisible){
                                if (monster.monsterId == 30 || monster.monsterId == 31 || monster.monsterId == 32 || monster.monsterId == 33 || monster.monsterId == 34 || monster.monsterId == 35|| monster.monsterId == 36||monster.monsterId == 37||monster.monsterId == 38||monster.monsterId == 39||monster.monsterId==40) {

                                    if (monster.isPlayerInAttackZone(player) &&
                                            now - monster.getLastAttackTime() >= monsterAttackCooldown) {

                                        launchMonsterProjectile(monster, player);
                                        monster.setLastAttackTime(now);
                                    }
                                }

                                if (monster.monsterId == 30 || monster.monsterId == 31) {
                                    if (player.getPosX() >= 681 && player.getPosX() <= 1456 &&
                                            player.getPosY() >= 1267 && player.getPosY() <= 1742) {

                                        if (monster.isPlayerInAttackZone(player)) {
                                            if (now - lastAttackTime >= attackCooldown) {

                                                int totalDamage = 0;
                                                for (Monster m : monsters) {
                                                    if (m.isAlive() && m.isPlayerInAttackZone(player)) {
                                                        totalDamage += m.getAttackPower();
                                                    }
                                                }

                                                int currentPlayerHealth = player.getHealth();
                                                int newPlayerHealth = currentPlayerHealth - totalDamage;

                                                player.setHealth(newPlayerHealth);
                                                lastAttackTime = now;
                                            }
                                            monster.chasePlayer(player);
                                        } else {
                                            monster.chasePlayer(player);
                                        }
                                        monster.updatePosition();
                                    } else {
                                        monster.idle();
                                        monster.updatePosition();
                                    }
                                } if (monster.monsterId == 38 || monster.monsterId == 39 || monster.monsterId == 40) {
                                    if (player.getPosX() >= 206 && player.getPosX() <= 1006 &&
                                            player.getPosY() >= 492 && player.getPosY() <= 917) {

                                        if (monster.isPlayerInAttackZone(player)) {
                                            if (now - lastAttackTime >= attackCooldown) {

                                                int totalDamage = 0;
                                                for (Monster m : monsters) {
                                                    if (m.isAlive() && m.isPlayerInAttackZone(player)) {
                                                        totalDamage += m.getAttackPower();
                                                    }
                                                }

                                                int currentPlayerHealth = player.getHealth();
                                                int newPlayerHealth = currentPlayerHealth - totalDamage;

                                                player.setHealth(newPlayerHealth);
                                                lastAttackTime = now;
                                            }
                                        } else {
                                            // Only chase if not attacking
                                            monster.chasePlayer(player);
                                            monster.updatePosition();
                                        }
                                    } else {
                                        monster.idle();
                                        monster.updatePosition();
                                    }

                                    // *** Special handling for monster ID 40 ***
                                    if (monster.monsterId == 40) {

                                        if (monster.getHealth() <= monster.getInitialHealth() / 2 && monster.getHealth() >= monster.getInitialHealth() / 3) {
                                            monster.getCharacterImageView().setVisible(false); // Make invisible
                                            monster.getHealthBar().setVisible(false);
                                            Random rand = new Random();
                                            int newX = rand.nextInt(1006 - 206 + 1) + 206;
                                            int newY = rand.nextInt(917 - 492 + 1) + 492;
                                            monster.setInitialPosition(newX, newY);
                                            monster.setBoundary(newX, newY, newX, newY);
                                            monster.updatePosition(); // Update position immediately
                                            if (monster.isPlayerInActivationZone(player)) {
                                                monster.getCharacterImageView().setVisible(true);
                                                monster.getHealthBar().setVisible(true);

                                                if (monster.isPlayerInAttackZone(player) &&
                                                        now - monster.getLastAttackTime() >= monsterAttackCooldown) {

                                                    launchMonsterProjectile(monster, player);
                                                    monster.setLastAttackTime(now);
                                                }

                                            }
                                        }
                                        else if(monster.getHealth() <= monster.getInitialHealth() / 4 && monster.getHealth() >= monster.getInitialHealth() / 5){

                                            controller.setConfirm(true);
                                        }
                                        else {
                                            monster.getCharacterImageView().setVisible(true); // Make visible
                                            monster.setInitialPosition(606, 667);
                                            monster.setBoundary(206, 492, 1006, 917);

                                        }

                                    }

                                }  else {
                                    if (monster.isPlayerInAttackZone(player)) {
                                        if (now - lastAttackTime >= attackCooldown) {
                                            int currentPlayerHealth = player.getHealth();
                                            int monsterAttackPower = monster.getAttackPower();
                                            int newPlayerHealth = currentPlayerHealth - monsterAttackPower;

                                            if (monster.monsterId != 30 ||monster.monsterId != 31 ||monster.monsterId != 38 || monster.monsterId != 39|| monster.monsterId != 40) {
                                                player.setHealth(newPlayerHealth);
                                                lastAttackTime = now;
                                            }
                                        }
                                    } else {
                                        if (monster.isAlive()) {
                                            monster.chasePlayer(player);
                                            monster.updatePosition();
                                        }
                                    }
                                }
                            } }else {
                            // If the monster is NOT alive (dead), remove it using the iterator
                            monsterIterator.remove();
                        }
                    }

                    handleKeyInputs();
                    checkMonstersAndGate();
                    checkPosition();
                    lastUpdate = now;
                }
            }
        };
    }

    private void increaseHealth(int amount) {
        int newHealthLevel = controller.getHealthLevel() + amount; // Assuming you have a getHealth() method in your controller
        if (newHealthLevel > 3000) { // Assuming 1000 is the max health
            newHealthLevel = 3000; // Cap health level
        }
        controller.setHealth(newHealthLevel); // Assuming you have a setHealth() method in your controller
        controller.updateHealthLabel();
    }

    private void increaseOxygen(int amount) {
        int newOxygenLevel = controller.getOxygenLevel() + amount; // Get current oxygen from controller
        if (newOxygenLevel > 1000) {
            newOxygenLevel = 1000; // Cap oxygen level at 100
        }
        controller.setOxygenLevel(newOxygenLevel); // Update oxygen level in controller
        controller.updateOxygenLabel(); // Assuming you have this method in your controller
    }

    private void checkPosition() {
        int x=player.getPosX();
        int y=player.getPosY();
        fixAnchorPane.setVisible((x >=760 && x<= 1000 && y<=250&& y>= 100));
        System.out.println("player x: " + player.getPosX() + " player y: " + player.getPosY());

    }

    private void checkMonstersAndGate() {
        synchronized (monsters) {
            boolean allRelevantMonstersDead = monsters.stream()
                    .filter(monster -> monster.monsterId != 8 && monster.monsterId != 9) // Filter out monsters 8 and 9
                    .noneMatch(Monster::isAlive); // Check if all other monsters are dead

            if (allRelevantMonstersDead) {
                ImageView currentGate = gate;
                if (currentGate != null) {
                    phase3World.getChildren().remove(currentGate);
                    obstacles.remove(currentGate);
                    isGateOpen = true;
                }
            }
        }
    }


    // Helper method to check if a monster is near the pet
    private boolean isMonsterNearPet(Monster monster, Pet pet) {
        double distance = Math.sqrt(Math.pow(monster.getPosX() - pet.getPosX(), 2) +
                Math.pow(monster.getPosY() - pet.getPosY(), 2));
        return distance <= 200; // Adjust the distance threshold as needed
    }

    private void moveWorld(int deltaX, int deltaY) {
        int speed = player.getSpeed();
        if (isSpeedBoostActive) {
            speed = (int) (speed * 3); // Increase speed by 100% (double the speed)
        }
        int playerNewX = player.getPosX() + (deltaX * speed);
        int playerNewY = player.getPosY() + (deltaY * speed);

        //add collision here................................

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


        if (playerNewX >= 144 + 24 && playerNewX <= WORLD_WIDTH - 144 - 72) {
            player.setPosX(playerNewX);
        }
        if (playerNewY >= (144 + 24) && playerNewY <= WORLD_HEIGHT - 144 - 72) {
            player.setPosY(playerNewY);
        }

        double newWorldOffsetX = worldOffsetX - (deltaX * speed);
        double newWorldOffsetY = worldOffsetY - (deltaY * speed);

        if (newWorldOffsetX <= (WORLD_WIDTH - SCREEN_WIDTH) - 144 && newWorldOffsetX >= SCREEN_WIDTH - WORLD_WIDTH + (WORLD_WIDTH - SCREEN_WIDTH) - 144) {
            if (deltaX < 0 && player.getPosX() <= WORLD_WIDTH - ((SCREEN_WIDTH / 2) + (48 / 2.0))) {
                worldOffsetX = newWorldOffsetX;
                phase3World.setTranslateX(worldOffsetX);
            }
            if (deltaX > 0 && player.getPosX() >= (SCREEN_WIDTH / 2) - (48 / 2.0)) {
                worldOffsetX = newWorldOffsetX;
                phase3World.setTranslateX(worldOffsetX);
            }
        }

        if (newWorldOffsetY <= (WORLD_HEIGHT - SCREEN_HEIGHT) - 144 && newWorldOffsetY >= SCREEN_HEIGHT - WORLD_HEIGHT + (WORLD_HEIGHT - SCREEN_HEIGHT) - 144) {
            if (deltaY < 0 && player.getPosY() <= WORLD_HEIGHT - (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                phase3World.setTranslateY(worldOffsetY);
            }
            if (deltaY > 0 && player.getPosY() >= (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                phase3World.setTranslateY(worldOffsetY);
            }
        }
        player.updatePosition();
        for (Monster monster : monsters) { // Update position for each monster
            monster.updatePosition();
        }
    }

    public void handleKeyInputs() {
        if (activeKeys.contains(KeyCode.UP)||activeKeys.contains(KeyCode.W)) {
            player.updateCharacterImage("UP");
            moveWorld(0, -1);
        }
        if (activeKeys.contains(KeyCode.DOWN)||activeKeys.contains(KeyCode.S)) {
            player.updateCharacterImage("DOWN");
            moveWorld(0, 1);
        }
        if (activeKeys.contains(KeyCode.LEFT)||activeKeys.contains(KeyCode.A)) {
            player.updateCharacterImage("LEFT");
            moveWorld(-1, 0);
        }
        if (activeKeys.contains(KeyCode.RIGHT)||activeKeys.contains(KeyCode.D)) {
            player.updateCharacterImage("RIGHT");
            moveWorld(1, 0);
        }


        if (activeKeys.contains(KeyCode.F)) {
            if (!isProjectileLaunched) {
                isProjectileLaunched = true;
// Create a new Projectile object with a unique ID
                ImageView projectileImage = new ImageView(new Image(getClass().getResource("/image/bullet1.gif").toExternalForm()));
                projectileImage.setFitWidth(40);
                projectileImage.setFitHeight(40);
// projectileImage.setVisible(false);
                phase3World.getChildren().add(projectileImage); // Add to the scene
                Projectile projectile = new Projectile(projectileImage, 25, player.getPosX(), player.getPosY(), 1);
                projectile.id = projectileCount++;
// Adjust initial projectile position based on direction
                int projectileX = player.getPosX();
                int projectileY = player.getPosY();
                switch (player.getDirection()) {
                    case "UP":
                        projectileY -= 28; // Adjust upward offset as needed
                        break;
                    case "DOWN":
                        projectileY += 20; // Adjust downward offset as needed
                        break;
// No need to adjust for LEFT and RIGHT
                }
                projectile.setPosition(projectileX, projectileY);
                projectile.setDirection(player.getDirection());

                projectiles.add(projectile);// Add the new projectile to the list
                projectile.setVisible(true);
            }
        } else {
            isProjectileLaunched = false; // Reset the flag when F key is released
        }

        // Add this block for speed boost activation with 'S' key
        if (isSpeedBoostPickedUp && activeKeys.contains(KeyCode.SPACE) && !isSpeedBoostActive) {
            isSpeedBoostActive = true;
            pet.setSpeedBoostActive(true);
        } else if (!activeKeys.contains(KeyCode.SPACE)) {
            isSpeedBoostActive = false;
            pet.setSpeedBoostActive(false);
        }
        // Activate Invisibility with 'I' key
        if (controller.isPowerupCollected && activeKeys.contains(KeyCode.I) && !controller.isInvisible) {
            controller.startInvisibilityTimer();
        }
    }

    private void handleMousePress(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) { // Check for right-click
            if (!isProjectileLaunched) {
                isProjectileLaunched = true;

                // Create a new Projectile object with a unique ID
                ImageView projectileImage = new ImageView(new Image(getClass().getResource("/image/bullet1.gif").toExternalForm()));
                projectileImage.setFitWidth(40);
                projectileImage.setFitHeight(40);
                // projectileImage.setVisible(false);
                phase3World.getChildren().add(projectileImage); // Add to the scene

                Projectile projectile = new Projectile(projectileImage, 25, player.getPosX(), player.getPosY(), 1);
                projectile.id = projectileCount++;

                // Adjust initial projectile position based on direction
                int projectileX = player.getPosX();
                int projectileY = player.getPosY();
                switch (player.getDirection()) {
                    case "UP":
                        projectileY -= 28; // Adjust upward offset as needed
                        break;
                    case "DOWN":
                        projectileY += 20; // Adjust downward offset as needed
                        break;
                    // No need to adjust for LEFT and RIGHT
                }
                projectile.setPosition(projectileX, projectileY);
                projectile.setDirection(player.getDirection());

                projectiles.add(projectile);// Add the new projectile to the list
                projectile.setVisible(true);

                // ... (rest of your projectile launching code remains the same)
            }
            else {
                isProjectileLaunched = false; // Reset the flag when F key is released
            }
        }
        // You can add an else block here to handle left-click if needed
    }



    private void startMonster33RespawnTimer() {
        if (monster33RespawnTimer != null) {
            monster33RespawnTimer.cancel();
        }

        monster33RespawnTimer = new Timer();
        monster33RespawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    respawnMonster33();
                });
            }
        }, monster33RespawnDelay);
    }


    private void respawnMonster33() {
        // Get the monster12 ImageView and monsterHealthBar9 from the FXML
        ImageView monster33 = (ImageView) phase3World.lookup("#monster33");
        ImageView monsterHealthBar33 = (ImageView) phase3World.lookup("#monsterHealthBar33");

        if (monster33 != null && monsterHealthBar33 != null) {
            Monster monster = new Monster(monster33, 0, 206, 1392, 10, 100, 300, 206, 1392, 206, 1392, 33, monsterHealthBar33);
            monster.setGameWorld(phase3World);
            monsters.add(monster);

            monster33.setVisible(true);
            monsterHealthBar33.setVisible(true);
            monsterHealthBar33.setImage(new Image(getClass().getResource("/image/live1_processed.png").toExternalForm())); // Reset health bar

            monsterRespawned33 = false;
        } else {
            System.err.println("Error: Could not find monster12 or monsterHealthBar9 in FXML.");
        }
    }

    private void respawnMonster32() {
        // Get the monster11 ImageView from the FXML
        ImageView monster32 = (ImageView) phase3World.lookup("#monster32");

        // Get the monsterHealthBar8 ImageView from the FXML
        ImageView monsterHealthBar32 = (ImageView) phase3World.lookup("#monsterHealthBar32");

        // Make sure the ImageView was found
        if (monster32 != null && monsterHealthBar32 != null) {
            Monster monster = new Monster(monster32, 0, 681, 1717, 10, 100, 300, 681, 1717, 681, 1717, 32, monsterHealthBar32);
            monster.setGameWorld(phase3World);
            monsters.add(monster);

            monster32.setVisible(true); // Make the monster visible again
            monsterHealthBar32.setVisible(true); // Make the health bar visible
            monsterHealthBar32.setImage(new Image(getClass().getResource("/image/live1_processed.png").toExternalForm()));

            //controler.getOxygen().setVisible(true);  // Remove this line
            monsterRespawned32 = false; // Reset the flag in respawnMonster()
        } else {
            System.err.println("Error: Could not find monster11 or monsterHealthBar8 in FXML.");
        }

    }
    private void startMonster32RespawnTimer() {
        // If the timer is already running, cancel it
        if (monster32RespawnTimer != null) {
            monster32RespawnTimer.cancel();
        }

        monster32RespawnTimer = new Timer();
        monster32RespawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    respawnMonster32();
                });
            }
        }, monster32RespawnDelay);
    }


    private void updateMonsterHealthBar(Monster monster) {
        ImageView healthBarView = monster.getHealthBar();
        int currentHealth = monster.getHealth();
        int initialHealth = monster.getInitialHealth();
        if (monster.isAlive()) {
            healthBarView.setLayoutX(monster.getPosX());
            healthBarView.setLayoutY(monster.getPosY() - 20);
            healthBarView.setVisible(true);
            if (currentHealth <= initialHealth * 0.8) {
                healthBarView.setImage(new Image(getClass().getResource("/image/live2_processed.png").toExternalForm()));
            }
            if (currentHealth <= initialHealth * 0.6) {
                healthBarView.setImage(new Image(getClass().getResource("/image/live3_processed.png").toExternalForm()));
            }
            if (currentHealth <= initialHealth * 0.4) {
                healthBarView.setImage(new Image(getClass().getResource("/image/live4_processed.png").toExternalForm()));
            }
            if (currentHealth <= initialHealth * 0.2) {
                healthBarView.setImage(new Image(getClass().getResource("/image/live5_processed.png").toExternalForm()));
            }
        }

    }

    private void showMonsterExplosion(Monster monster) {
        // Show the explosion GIF
        monsterBoomImage.setVisible(true);
        monsterBoomImage.setLayoutX(monster.getPosX());
        monsterBoomImage.setLayoutY(monster.getPosY());

        // Hide the explosion GIF after a delay
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> monsterBoomImage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void launchMonsterProjectile(Monster monster, MainCharacter player) {
        if (monster.monsterId == 30||monster.monsterId==31 )  {
            int numProjectiles = 4;

            for (int i = 0; i < numProjectiles; i++) {
                // Get the direction for this projectile (cycle through UP, DOWN, LEFT, RIGHT)
                String projectileDirection = getMonsterDirection(monster, i);

                // Create a new projectile
                ImageView projectileImage = new ImageView(new Image(getClass().getResource("/image/mprojectile4.png").toExternalForm()));
                projectileImage.setFitWidth(15);
                projectileImage.setFitHeight(15);
                phase3World.getChildren().add(projectileImage);

                Projectile projectile = new Projectile(projectileImage, 15, monster.getPosX(), monster.getPosY(), 4);

                // Set the projectile's direction
                projectile.setDirection(projectileDirection);

                // Set the projectile's position to slightly offset from the monster's center (adjust offset as needed)
                int offset = 20; // Adjust this value to control the distance from the monster
                switch (projectileDirection) {
                    case "UP":
                        projectile.setPosition(monster.getPosX(), monster.getPosY() - offset);
                        break;
                    case "DOWN":
                        projectile.setPosition(monster.getPosX(), monster.getPosY() + offset);
                        break;
                    case "LEFT":
                        projectile.setPosition(monster.getPosX() - offset, monster.getPosY());
                        break;
                    case "RIGHT":
                        projectile.setPosition(monster.getPosX() + offset, monster.getPosY());
                        break;


                }

                monsterProjectiles.add(projectile);
                projectile.setVisible(true);
            }
        }
        else{  projectileImage = new ImageView(new Image(getClass().getResource("/image/mprojectile5.png").toExternalForm()));
            projectileImage.setFitWidth(18);
            projectileImage.setFitHeight(18);
            phase3World.getChildren().add(projectileImage);

            // Calculate direction from monster to player
            double angle = Math.atan2(player.getPosY() - monster.getPosY(), player.getPosX() - monster.getPosX());
            String direction = getDirectionFromAngle(angle); // Get direction string

            Projectile projectile = new Projectile(projectileImage, 15, monster.getPosX(), monster.getPosY(), 5);
            projectile.setDirection(direction); // Set the projectile's direction
            projectile.setPosition(monster.getPosX(), monster.getPosY());

            monsterProjectiles.add(projectile);
            projectile.setVisible(true);}
    }

    private boolean hasProjectileTraveledDistance(Projectile projectile, int distance) {
        int initialX = player.getPosX();
        int initialY = player.getPosY();
        int currentX = (int) projectile.projectileImage.getLayoutX();
        int currentY = (int) projectile.projectileImage.getLayoutY();

        int distanceTraveled = (int) Math.sqrt(Math.pow(currentX - initialX, 2) +
                Math.pow(currentY - initialY, 2));
        return distanceTraveled >= distance;
    }
    private String getMonsterDirection(Monster monster, int index) {
        String[] directions = {
                "UP", "DOWN", "LEFT", "RIGHT"
        };
        int directionIndex = index % directions.length;
        return directions[directionIndex];
    }

    // Add this helper function to determine the monster's direction
    private String getMonsterDirection(Monster monster) {
        String direction = monster.lastDirection;
        switch (direction) {
            case "UP":
                return "DOWN";
            case "DOWN":
                return "UP";
            case "LEFT":
                return "RIGHT";
            case "RIGHT":
                return "LEFT";
            default:
                return "DOWN"; // Default to down if player direction is invalid
        }
    }

    public static String getDirectionFromAngle(double angle) {
        // Convert radians to degrees
        double degrees = Math.toDegrees(angle);

        // Adjust for screen coordinates (y-axis is flipped)
        degrees = (degrees + 180) % 360;

        if (degrees >= 11.25 && degrees < 33.75) {
            return "UP_LEFT_LEFT"; // Between UP_LEFT and LEFT
        } else if (degrees >= 33.75 && degrees < 56.25) {
            return "UP_LEFT";
        } else if (degrees >= 56.25 && degrees < 78.75) {
            return "UP_LEFT_UP"; // Between UP_LEFT and UP
        } else if (degrees >= 78.75 && degrees < 101.25) {
            return "UP";
        } else if (degrees >= 101.25 && degrees < 123.75) {
            return "UP_RIGHT_UP"; // Between UP and UP_RIGHT
        } else if (degrees >= 123.75 && degrees < 146.25) {
            return "UP_RIGHT";
        } else if (degrees >= 146.25 && degrees < 168.75) {
            return "UP_RIGHT_RIGHT"; // Between UP_RIGHT and RIGHT
        } else if (degrees >= 168.75 && degrees < 191.25) {
            return "RIGHT";
        } else if (degrees >= 191.25 && degrees < 213.75) {
            return "DOWN_RIGHT_RIGHT"; // Between RIGHT and DOWN_RIGHT
        } else if (degrees >= 213.75 && degrees < 236.25) {
            return "DOWN_RIGHT";
        } else if (degrees >= 236.25 && degrees < 258.75) {
            return "DOWN_RIGHT_DOWN"; // Between DOWN_RIGHT and DOWN
        } else if (degrees >= 258.75 && degrees < 281.25) {
            return "DOWN";
        } else if (degrees >= 281.25 && degrees < 303.75) {
            return "DOWN_LEFT_DOWN"; // Between DOWN and DOWN_LEFT
        } else if (degrees >= 303.75 && degrees < 326.25) {
            return "DOWN_LEFT";
        } else if (degrees >= 326.25 && degrees < 348.75) {
            return "DOWN_LEFT_LEFT"; // Between DOWN_LEFT and LEFT
        } else {
            return "LEFT";
        }
    }


    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }


    // Method to get the dead monsters
    public Set<Monster> getDeadMonsters() {
        return deadMonsters;
    }

    public void clearDeadMonsters() {
        deadMonsters.clear();
    }



}