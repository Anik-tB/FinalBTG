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

public class Phase1Loop {

    @FXML
    private ImageView gate;

    private ImageView monsterBoomImage, projectileImage;

    private AnimationTimer gameLoop;
    private final Set<KeyCode> activeKeys;
    private MainCharacter player;
    private List<Monster> monsters = new ArrayList<>();
    private List<ImageView> obstacles;
    private AnchorPane phase1World;
    private Phase1Controller controller;
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> monsterProjectiles = new ArrayList<>(); // Separate list for monster projectiles
    private Set<Monster> deadMonsters = new HashSet<>();

    private int projectileCount = 0;
    private long lastAttackTime = 0;
    private final long attackCooldown = 1_000_000_000;
    private final long monsterAttackCooldown = 1_000_000_000;// 5 seconds for monster
    private Timer monsterRespawnTimer;
    private final long monsterRespawnDelay = 10_000; // 30 seconds
    private Timer monster9RespawnTimer; // Separate timer for monster 9
    private final long monster9RespawnDelay = 15_000;// Tracks if the boost is active

    private boolean isGateOpen = false; // Flag to track gate status
    private boolean isProjectileLaunched = false;
    private boolean isSpeedBoostActive = false;
    private boolean monsterRespawned = false;
    private boolean monsterRespawned9 = false;
    protected boolean isSpeedBoostPickedUp = false;

    private static double WORLD_WIDTH;
    private static double WORLD_HEIGHT;
    private static final double SCREEN_WIDTH = 912.0;
    private static final double SCREEN_HEIGHT = 624.0;
    private double worldOffsetX = 0;
    private double worldOffsetY = 0;


    public Phase1Loop(MainCharacter player, Set<KeyCode> activeKeys, List<ImageView> obstacles, AnchorPane phase1World, List<Monster> monster, ImageView monsterBoomImage, Phase1Controller controller, ImageView gate) {
        this.player = player;
        this.phase1World = phase1World;
        // Add mouse event handler to the scene
        phase1World.setOnMousePressed(this::handleMousePress);
        this.WORLD_WIDTH = phase1World.getPrefWidth();
        this.WORLD_HEIGHT = phase1World.getPrefHeight();
        this.activeKeys = activeKeys;
        this.obstacles = obstacles;
        this.monsters = monster;
        for (Monster m : this.monsters) {
            m.setMonsters(this.monsters);
        }
        this.gate = gate;
        this.monsterBoomImage = monsterBoomImage;
        this.controller = controller;
        setupGameLoop();

    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            private boolean oxygenCollected = false;
            private boolean healthCollected = false;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 150_000_000) {

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

                        increaseHealth(50); // Increase health by 200
                        healthCollected = true;
                        controller.getHealth().setVisible(false);
                        startMonster9RespawnTimer(); // You might want to start the respawn timer here as well
                    }
                    if (!oxygenCollected && controller.getOxygen().isVisible() && player.getCharacterImageView().getBoundsInParent().intersects(controller.getOxygen().getBoundsInParent())) {

                        increaseOxygen(10);
                        oxygenCollected = true; // Set the flag to true after collecting oxygen
                        controller.getOxygen().setVisible(false); // Hide the oxygen image
                        // Start the respawn timer when oxygen is collected
                        startMonsterRespawnTimer();
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
                                if (monster.isAlive() && monster.getCharacterImageView().isVisible() && projectile.projectileImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                                    // Check if the player's direction matches the monster's direction
                                    if (player.getDirection().equals(getMonsterDirection(monster))) {
                                        monster.takeDamage(50);

                                        if (!monster.isAlive()) {
                                            monster.getCharacterImageView().setVisible(false);
                                            monster.getHealthBar().setVisible(false);
                                            if (monster.monsterId == 9) {
                                                if (!monsterRespawned9) {
                                                    controller.getHealth().setVisible(true);
                                                    monsterRespawned9 = true;
                                                }
                                                obstacles.remove(monster.getCharacterImageView());
                                                healthCollected = false;

                                                startMonster9RespawnTimer(); // Start monster 9 respawn timer
                                            }
                                            if (monster.monsterId == 8) {
                                                if (!monsterRespawned) { // Check the flag here
                                                    controller.getOxygen().setVisible(true);
                                                    monsterRespawned = true; // Set the flag to true
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
                                if (projectile.Id == 2) {
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
                        if (monster.monsterId == 10) {
                            if (monster.isPlayerInActivationZone(player)) {
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
                            if (monster.monsterId == 6 || monster.monsterId == 7 || monster.monsterId == 8 || monster.monsterId == 9 || monster.monsterId == 11|| monster.monsterId == 12) {
                                if (monster.isPlayerInAttackZone(player) &&
                                        now - monster.getLastAttackTime() >= monsterAttackCooldown) {

                                    launchMonsterProjectile(monster, player);
                                    monster.setLastAttackTime(now);
                                }
                            }

                            if (monster.monsterId == 1 || monster.monsterId == 2 || monster.monsterId == 3 || monster.monsterId == 4) {
                                if (player.getPosX() >= 1296 && player.getPosX() <= 2068 &&
                                        player.getPosY() >= 1248 && player.getPosY() <= 1872) {

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
                            } else if (monster.monsterId == 6 || monster.monsterId == 7 ) {
                                if (player.getPosX() >= 192 && player.getPosX() <= 960 &&
                                        player.getPosY() >= 548 && player.getPosY() <= 1344) {

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
                                        monster.chasePlayer(player);
                                        monster.updatePosition();
                                    }
                                } else {
                                    monster.idle();
                                    monster.updatePosition();
                                }
                            } else if (monster.monsterId == 5) { // Special case for monster 5
                                if (player.getPosX() >= 1296 && player.getPosX() <= 2068 &&
                                        player.getPosY() >= 1248 && player.getPosY() <= 1872) {

                                    if (monster.isPlayerInAttackZone(player)) {
                                        if (now - lastAttackTime >= attackCooldown) {
                                            int totalDamage = 0;
                                            for (Monster m : monsters) {
                                                if (m.isAlive() && m.isPlayerInAttackZone(player)) {
                                                    if (m.monsterId == 5) {
                                                        totalDamage += m.getAttackPower() * 2; // Double damage for monster 5
                                                    } else {
                                                        totalDamage += m.getAttackPower();
                                                    }
                                                }
                                            }

                                            controller.decreaseHealth(totalDamage); // Use controller's method
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
                            }else {
                                if (monster.isPlayerInAttackZone(player)) {
                                    if (now - lastAttackTime >= attackCooldown) {
                                        int currentPlayerHealth = player.getHealth();
                                        int monsterAttackPower = monster.getAttackPower();
                                        int newPlayerHealth = currentPlayerHealth - monsterAttackPower;

                                        if (monster.monsterId != 6 || monster.monsterId != 7) {
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
                        } else {
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
        double x = player.getCharacterImageView().getLayoutX();
        double y = player.getCharacterImageView().getLayoutY();

        System.out.println("player x: " + x + " player y: " + y);

        if((x >= 690 && x <= 790) && (y >= 250 && y <= 452)){
            controller.checkLevel2Entry();
        }
    }

    private void checkMonstersAndGate() {
        synchronized (monsters) {
            boolean allRelevantMonstersDead = monsters.stream()
                    .filter(monster -> monster.monsterId != 8 && monster.monsterId != 9 && monster.monsterId != 10) // Filter out monsters 8 and 9
                    .noneMatch(Monster::isAlive); // Check if all other monsters are dead

            if (allRelevantMonstersDead) {
                ImageView currentGate = gate;
                if (currentGate != null) {
                    phase1World.getChildren().remove(currentGate);
                    obstacles.remove(currentGate);
                    isGateOpen = true;
                }
            }
        }
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
                phase1World.setTranslateX(worldOffsetX);
            }
            if (deltaX > 0 && player.getPosX() >= (SCREEN_WIDTH / 2) - (48 / 2.0)) {
                worldOffsetX = newWorldOffsetX;
                phase1World.setTranslateX(worldOffsetX);
            }
        }

        if (newWorldOffsetY <= (WORLD_HEIGHT - SCREEN_HEIGHT) - 144 && newWorldOffsetY >= SCREEN_HEIGHT - WORLD_HEIGHT + (WORLD_HEIGHT - SCREEN_HEIGHT) - 144) {
            if (deltaY < 0 && player.getPosY() <= WORLD_HEIGHT - (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                phase1World.setTranslateY(worldOffsetY);
            }
            if (deltaY > 0 && player.getPosY() >= (SCREEN_HEIGHT / 2) - (48 / 2.0)) {
                worldOffsetY = newWorldOffsetY;
                phase1World.setTranslateY(worldOffsetY);
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
                phase1World.getChildren().add(projectileImage); // Add to the scene
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

        } else if (!activeKeys.contains(KeyCode.SPACE)) {
            isSpeedBoostActive = false;

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
                phase1World.getChildren().add(projectileImage); // Add to the scene

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

    private void startMonster9RespawnTimer() {
        if (monster9RespawnTimer != null) {
            monster9RespawnTimer.cancel();
        }

        monster9RespawnTimer = new Timer();
        monster9RespawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    respawnMonster9();
                });
            }
        }, monster9RespawnDelay);
    }

    private void startMonsterRespawnTimer() {
        // If the timer is already running, cancel it
        if (monsterRespawnTimer != null) {
            monsterRespawnTimer.cancel();
        }

        monsterRespawnTimer = new Timer();
        monsterRespawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    respawnMonster();
                });
            }
        }, monsterRespawnDelay);
    }

    private void respawnMonster9() {
        // Get the monster12 ImageView and monsterHealthBar9 from the FXML
        ImageView monster12 = (ImageView) phase1World.lookup("#monster9");
        ImageView monsterHealthBar9 = (ImageView) phase1World.lookup("#monsterHealthBar9");

        if (monster12 != null && monsterHealthBar9 != null) {
            Monster monster = new Monster(monster12, 0, 1874, 1248, 10, 100, 300, 1874, 1248, 1874, 1248, 9, monsterHealthBar9);
            monster.setGameWorld(phase1World);
            monsters.add(monster);

            monster12.setVisible(true);
            monsterHealthBar9.setVisible(true);
            monsterHealthBar9.setImage(new Image(getClass().getResource("/image/live1_processed.png").toExternalForm())); // Reset health bar

            monsterRespawned9 = false;
        } else {
            System.err.println("Error: Could not find monster12 or monsterHealthBar9 in FXML.");
        }
    }

    private void respawnMonster() {
        // Get the monster11 ImageView from the FXML
        ImageView monster11 = (ImageView) phase1World.lookup("#monster8");

        // Get the monsterHealthBar8 ImageView from the FXML
        ImageView monsterHealthBar8 = (ImageView) phase1World.lookup("#monsterHealthBar8");

        // Make sure the ImageView was found
        if (monster11 != null && monsterHealthBar8 != null) {
            Monster monster = new Monster(monster11, 0, 1654, 828, 10, 100, 300, 1654, 828, 1654, 828, 8, monsterHealthBar8);
            monster.setGameWorld(phase1World);
            monsters.add(monster);

            monster11.setVisible(true); // Make the monster visible again
            monsterHealthBar8.setVisible(true); // Make the health bar visible
            monsterHealthBar8.setImage(new Image(getClass().getResource("/image/live1_processed.png").toExternalForm()));

            //controler.getOxygen().setVisible(true);  // Remove this line
            monsterRespawned = false; // Reset the flag in respawnMonster()
        } else {
            System.err.println("Error: Could not find monster11 or monsterHealthBar8 in FXML.");
        }

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
        projectileImage = new ImageView(new Image(getClass().getResource("/image/mprojectile2.png").toExternalForm()));
        projectileImage.setFitWidth(10);
        projectileImage.setFitHeight(10);
        phase1World.getChildren().add(projectileImage);

        // Calculate direction from monster to player
        double angle = Math.atan2(player.getPosY() - monster.getPosY(), player.getPosX() - monster.getPosX());
        String direction = getDirectionFromAngle(angle); // Get direction string

        Projectile projectile = new Projectile(projectileImage, 15, monster.getPosX(), monster.getPosY(), 2);
        projectile.setDirection(direction); // Set the projectile's direction
        projectile.setPosition(monster.getPosX(), monster.getPosY());

        monsterProjectiles.add(projectile);
        projectile.setVisible(true);
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

    private String getDirectionFromAngle(double angle) {
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