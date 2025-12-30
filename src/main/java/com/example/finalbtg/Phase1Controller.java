package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.*;

public class Phase1Controller {

    @FXML
    private AnchorPane phase1World, pauseAnchorPane, restartConfirmAnchorPane,resourceCollectionAnchorPane,bagAnchorPane;
    @FXML
    private ImageView monsterImage1, monsterImage2, monsterImage3, monsterImage4, monsterImage5, monsterImage6, monsterImage7, monster8, monster9,monster10,monster11,monster12;
    @FXML
    private ImageView monsterHealthBar1, monsterHealthBar2, monsterHealthBar3, monsterHealthBar4, monsterHealthBar5, monsterHealthBar6, monsterHealthBar7, monsterHealthBar8, monsterHealthBar9,monsterHealthBar10,monsterHealthBar11,monsterHealthBar12;
    @FXML
    private ImageView mainCharacterImage, soundImage, musicImage, oxygen1, health1, resource12, projectileImage, monsterBoomImage, monsterProjectileImage, gate, speedBoost;
    @FXML
    private Button pauseButton,bagButton;
    @FXML
    private Label timerLabel, oxygenLabel, healthLabel, speedBoostTimerLabel;
    @FXML
    private Button collectResourceButton,fireButton,nutButton;

    private boolean isResourceCollected = false;


    private MainCharacter player;
    private Phase1Loop phase1Loop;
    private AnimationTimer gameLoop;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private List<ImageView> obstacles = new ArrayList<>();
    private List<Monster> monsters = new ArrayList<>();

    private boolean isGamePaused = false;
    private boolean isPauseButtonVisible = true;
    private boolean isSoundMute = false;
    private boolean isMusicMute = false;
    private boolean isShooting = false;
    private boolean canTakeDamage = true;
    private boolean isGameOver = false; // Add this flag

    private long startTime;
    private Timer speedBoostTimer;
    private long elapsedTime = 0; // Add this to track elapsed time
    private long lastPauseTime = 0; // Add this to track pause time
    private long levelTimeLimit = 240000;// 4 minutes in milliseconds
    private int oxygenLevel = 1000; // Initial oxygen level
    private long lastOxygenDecreaseTime; // To keep track of oxygen decrease
    private int health = 3000; // Initial health value for the spaceman
    private static final double tileSize = 48.0;
    private int speedBoostTimeRemaining = 30; // Initial time for speed boost


    private Stage window;

    public void setWindow(Stage stage) {
        this.window = stage;
    }

    @FXML
    public void initialize() {
        Image cursorImage = new Image(getClass().getResource("/image/Cursor.png").toExternalForm());
        Cursor customCursor = new ImageCursor(cursorImage); // Use ImageCursor

        phase1World.setOnMouseEntered(event -> {
            phase1World.setCursor(customCursor);
        });

        phase1World.setOnMouseExited(event -> {
            phase1World.setCursor(Cursor.DEFAULT);
        });
        collectResourceButton.setOnAction(event -> {
            resourceCollectionAnchorPane.setVisible(false);
            isResourceCollected = true; // Set the flag to true
            resource12.setVisible(false); // Hide the resource image
            fireButton.setVisible(true);
            nutButton.setVisible(true);

        });
        startTime = System.currentTimeMillis(); // Start the timer when the level begins
        lastOxygenDecreaseTime = System.currentTimeMillis(); // Initialize oxygen decrease time
        oxygen1.setVisible(false);
        health1.setVisible(false);
        speedBoostTimerLabel.setVisible(false); // Initially hide the timer label

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                checkCollision(); // Check for collision in every frame
                updateTimer(); // Update the timer in every frame
                decreaseOxygen(); // Call the oxygen decrease method in each frame
            }
        };

        gameLoop.start(); // Start the game loop
        updateHealthLabel();
        setupLevel1Game();
        updateMusicMuteIcon();
        updateSoundMuteIcon();
    }

    private void setupLevel1Game() {
        try {
            fillMoonSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player = new MainCharacter(mainCharacterImage, 20, 1634, 2208, 100, 200, 1000);
        player.setGameWorld(phase1World);
        //for monster
        monsters.add(new Monster(monsterImage1, 15, 1994, 1408, 10, 100, 50, 1296, 1248, 2068, 1872, 1, monsterHealthBar1));
        // monsters.add(new Monster(monsterImage2, 15, 1934, 1548, 10, 100, 50, 1296, 1248, 2068, 1872, 2, monsterHealthBar2));
        //  monsters.add(new Monster(monsterImage3, 15, 1994, 1648, 10, 100, 50, 1296, 1248, 2068, 1872, 3, monsterHealthBar3));
        //  monsters.add(new Monster(monsterImage4, 15, 1314, 1488, 10, 100, 50, 1296, 1248, 2068, 1872, 4, monsterHealthBar4));
        monsters.add(new Monster(monsterImage5, 10, 1434, 1308, 10, 100, 50, 1296, 1248, 2068, 1872, 5, monsterHealthBar5));

        monsters.add(new Monster(monsterImage6, 15, 594, 768, 10, 100, 150, 192, 548, 960, 1344, 6, monsterHealthBar6));
        //  monsters.add(new Monster(monsterImage7, 15, 374, 1128, 10, 100, 150, 192, 548, 960, 1344, 7, monsterHealthBar7));

        monsters.add(new Monster(monster8, 0, 1654, 800, 10, 200, 300, 1654, 800, 1654, 800, 8, monsterHealthBar8));
        monsters.add(new Monster(monster9, 0, 1884, 1218, 10, 100, 300, 1884, 1218, 1884, 1218, 9, monsterHealthBar9));

        monsters.add(new Monster(monster10, 0, 344, 288, 10, 100, 300, 344, 288, 344, 288, 10, monsterHealthBar10));
        monsters.add(new Monster(monster11, 0, 814, 628, 10, 100, 300, 814, 628, 814, 628, 11, monsterHealthBar11));
        monsters.add(new Monster(monster12, 0, 224, 628, 10, 100, 300, 224, 628, 224, 628, 12, monsterHealthBar12));
        monsters.forEach(monster -> monster.setGameWorld(phase1World)); // Set game world for each monster

        addObstacles();
        phase1Loop = new Phase1Loop(player, activeKeys, obstacles, phase1World, monsters, monsterBoomImage, this, gate);

        // --- Add this line ---
        speedBoostTimerLabel.setText(String.valueOf(speedBoostTimeRemaining));

        Platform.runLater(() -> {
            phase1World.getScene().setOnKeyPressed(this::handleKeyPressed);
            phase1World.getScene().setOnKeyReleased(this::handleKeyReleased);
        });
        phase1Loop.start();

    }

    private void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    public void setupLevel1Game(MainCharacter player, int x, int y) throws IOException {
        if (player == null) {
            System.out.println("Error: Player object is null.");
            return;
        }
        this.player = player;
        this.player.setGameWorld(phase1World);
        this.player.setPosition(x, y);

        fillMoonSurface();
    }


    private void fillMoonSurface() throws IOException {

        String mapFilePath = "phase1map.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            Image road = new Image(getClass().getResource("/image/tile/5.png").toExternalForm());
            Image road2 = new Image(getClass().getResource("/image/tile/10.png").toExternalForm());
            Image tree = new Image(getClass().getResource("/image/tile/7.png").toExternalForm());
            Image tree2 = new Image(getClass().getResource("/image/tile/8.png").toExternalForm());
            Image stone = new Image(getClass().getResource("/image/tile/6.png").toExternalForm());
            Image fire = new Image(getClass().getResource("/image/tile/9.png").toExternalForm());
            Image star = new Image(getClass().getResource("/image/tile/s.jpg").toExternalForm());
            Canvas canvas = new Canvas(2256, 2688);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double tileSize = 48.0;
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char tileType = line.charAt(col);
                    Image tileImage = null;

                    if (tileType == '1') {
                        tileImage = road;
                    }
                    if (tileType == '2') {
                        tileImage = tree;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '3') {
                        tileImage = tree2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '4') {
                        tileImage = stone;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '5') {
                        tileImage = fire;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '6') {
                        tileImage = star;
                    }
                    if (tileType == '7') {
                        tileImage = road2;
                    }
                    if (tileImage != null) {
                        gc.drawImage(tileImage, col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
                row++;
            }
            phase1World.getChildren().add(canvas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gate.toFront();
        resource12.toFront();
        mainCharacterImage.toFront();
        monsterImage1.toFront();
        //  monsterImage2.toFront();
        //  monsterImage3.toFront();
        //  monsterImage4.toFront();
        monsterImage5.toFront();
        monsterImage6.toFront();
        //   monsterImage7.toFront();
        monster8.toFront();
        monster9.toFront();
        monster10.toFront();
        monster11.toFront();
        monster12.toFront();


        projectileImage.toFront();
        monsterBoomImage.toFront();
        monsterProjectileImage.toFront();
        monsterHealthBar1.toFront();
        monsterHealthBar2.toFront();
        monsterHealthBar3.toFront();
        monsterHealthBar4.toFront();
        monsterHealthBar5.toFront();
        monsterHealthBar6.toFront();
        monsterHealthBar7.toFront();
        monsterHealthBar8.toFront();
        monsterHealthBar9.toFront();
        monsterHealthBar10.toFront();
        monsterHealthBar11.toFront();
        monsterHealthBar12.toFront();

        oxygen1.toFront();
        health1.toFront();
        speedBoost.toFront();
    }

    private void addRockObstacle(double x, double y) {
        ImageView rockObstacle = new ImageView(new Image(getClass().getResource("/image/tile/6.png").toExternalForm()));
        rockObstacle.setFitWidth(tileSize);
        rockObstacle.setFitHeight(tileSize);
        rockObstacle.setLayoutX(x);
        rockObstacle.setLayoutY(y);

        obstacles.add(rockObstacle);

    }

    private void addObstacles() {

        ImageView Monster8 = (ImageView) phase1World.lookup("#monster8");
        ImageView Monster9 = (ImageView) phase1World.lookup("#monster9");
       // ImageView Resource12 = (ImageView) phase1World.lookup("#resource12");
        ImageView Level2Image = (ImageView) phase1World.lookup("#level2image");
        ImageView Gate = (ImageView) phase1World.lookup("#gate");


        if (Monster8 != null) {
            addObstacle(Monster8);
        }
        if (Monster9 != null) {
            addObstacle(Monster9);
        }
//        if (Resource12 != null) {
//            addObstacle(Resource12);
//        }
        if (Level2Image != null) {
            addObstacle(Level2Image);
        }
        if (Gate != null) {
            addObstacle(Gate);
        }

    }

    private void addObstacle(ImageView obstacle) {
        if (obstacle != null) {
            obstacles.add(obstacle);
        }
    }
    @FXML
    private void collectResourceButtonAction() {
        resourceCollectionAnchorPane.setVisible(false);
        isResourceCollected = true;
        resource12.setVisible(false);
        obstacles.remove(resource12);
//        phase1Loop.start();
//        gameLoop.start();

    }




    // pause and settings.....................................................................................


    @FXML
    private void pauseButtonAction() {
        if (!isGamePaused) {
            phase1Loop.stop();
            gameLoop.stop();
            isGamePaused = true;
            pauseAnchorPane.setVisible(true);
            pauseButton.setVisible(false);
            isPauseButtonVisible=false;
        }
    }

    @FXML
    private void resumeButtonAction() {
        if (isGamePaused) {
            isGamePaused = false;
            phase1Loop.start();
            pauseAnchorPane.setVisible(false);
            if (!isPauseButtonVisible) {
                pauseButton.setVisible(true);
            }
            startTime = System.currentTimeMillis() - elapsedTime; // Reset startTime
            gameLoop.start(); // Restart the AnimationTimer when resuming
        }
    }

    @FXML
    private void restartButtonAction() {
        restartConfirmAnchorPane.setVisible(true);
    }

    @FXML
    private void restartYesButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guestGameWorld.fxml"));
            Scene scene = new Scene(loader.load(), 912, 624);

            GuestGameController guestGameController = loader.getController();
            guestGameController.setWindow(window);

            guestGameController.setupGuestGame();

            window.setScene(scene);
            window.setTitle("Beyond The Galaxy");
            window.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void restartNoButtonAction() {
        restartConfirmAnchorPane.setVisible(false);
    }

    @FXML
    private void exitButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guestDashboard.fxml"));
            Scene scene = new Scene(loader.load(), 912, 624);

            Object controller = loader.getController();
            if (controller != null && controller instanceof ButtonHandler buttonHandler) {
                buttonHandler.setWindow(window);
            }

            window.setScene(scene);
            window.setTitle("Beyond The Galaxy");
            window.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void checkLevel2Entry(){
        phase1Loop.stop();
        gameLoop.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("phase2.fxml"));
            Scene scene = new Scene(loader.load(), 912, 624);

            Phase2Controller phase2Controller = loader.getController();
            phase2Controller.setWindow(window);

            phase2Controller.setupPhase2(player, 2592, 2640,health,oxygenLevel);

            // Create a FadeTransition
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), window.getScene().getRoot()); // 1 second fade
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(event -> {
                window.setScene(scene);
                window.setTitle("Beyond the Galaxy");
                window.centerOnScreen();

                // Fade in the new scene
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), scene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void soundButtonAction() {
        MainGame.isSoundMuted = !MainGame.isSoundMuted;
        updateSoundMuteIcon();
    }

    public void updateSoundMuteIcon(){
        Platform.runLater(() -> {
            soundImage.setImage(null);
            if (MainGame.isSoundMuted) {
                soundImage.setImage(new Image("/image/icon/mutesound.png"));
            } else {
                soundImage.setImage(new Image("/image/icon/sound.png"));
            }
        });
    }

    @FXML
    private void musicButtonAction() {
        boolean currentMuteState = MainGame.mediaPlayer.isMute();
        MainGame.mediaPlayer.setMute(!currentMuteState);
        isMusicMute = !currentMuteState;
        updateMusicMuteIcon();
    }

    public void updateMusicMuteIcon(){
        Platform.runLater(() -> {
            musicImage.setImage(null);
            if (MainGame.mediaPlayer.isMute()) {
                musicImage.setImage(new Image("/image/icon/mutemusic.png"));
            } else {
                musicImage.setImage(new Image("/image/icon/music.png"));
            }
        });
    }




    // Hedar code............................................................................................


    private void updateTimer() {
        if (!isGamePaused) { // Only update time if game is not paused
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        long remainingTime = levelTimeLimit - elapsedTime;

        if (remainingTime <= 0) {
            gameLoop.stop();
            // onGameOver();
            timeDeathAnchorPane.setVisible(true);
        }
        long seconds = (remainingTime / 1000) % 60;
        long minutes = (remainingTime / (1000 * 60)) % 60;

        Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
    }

    @FXML
    private AnchorPane timeDeathAnchorPane,healthDeathAnchorPane,oxygenDeathAnchorPane;

    @FXML
    private void goBackButtonAction(){
        onGameOver();
    }

    private void decreaseOxygen() {
        long currentTime = System.currentTimeMillis();

        if (isGamePaused) {
            lastPauseTime = currentTime; // Record the time when paused
            return; // Don't decrease oxygen while paused
        }
        // Adjust currentTime to account for pause time
        if (lastPauseTime > 0) {
            long pauseDuration = currentTime - lastPauseTime;
            lastOxygenDecreaseTime += pauseDuration; // Add pause duration
            lastPauseTime = 0; // Reset lastPauseTime
        }
        if (currentTime - lastOxygenDecreaseTime >= 1000) {
            oxygenLevel--;
            if (oxygenLevel < 0) {
                oxygenLevel = 0;
            }
            updateOxygenLabel();
            lastOxygenDecreaseTime = currentTime;
            if (oxygenLevel == 0) {
                gameLoop.stop();
                oxygenDeathAnchorPane.setVisible(true);
            }
        }
    }

    public void decreaseHealth(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0; // Prevent health from going below 0
        }
        updateHealthLabel(); // Update the health label after taking damage

        if (health == 0) {
            gameLoop.stop();
            healthDeathAnchorPane.setVisible(true);        }
    }
    public void updateHealthLabel() {
        // Update the health label text with current health
        healthLabel.setText(String.valueOf(health));

        // Change the text color based on the health value
        if (health <= 500) {
            healthLabel.setStyle("-fx-text-fill: red;"); // Low health (danger)
        } else if (health <= 2000) {
            healthLabel.setStyle("-fx-text-fill: orange;"); // Medium health (warning)
        } else {
            healthLabel.setStyle("-fx-text-fill: white;"); // Healthy
        }
    }

    public ImageView getOxygen() {
        return oxygen1;
    }

    public int getOxygenLevel() {
        return oxygenLevel;
    }

    public void setOxygenLevel(int oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    public void updateOxygenLabel() {
        Platform.runLater(() -> oxygenLabel.setText(oxygenLevel + "%"));
    }

    public ImageView getHealth() {
        return health1;
    }

    public int getHealthLevel() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public ImageView getSpeedBoostImage() {
        return speedBoost;
    }

    private void checkCollision() {
        for (Monster monster : monsters) { // Check collision with each monster
            if (monster.isAlive() && mainCharacterImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                if (canTakeDamage) {
                    decreaseHealth(10);
                    startDamageCooldown();
                }
            }
        }

        // Check if monster with ID 10 is dead
        boolean isMonster10Dead = monsters.stream().filter(m -> m.monsterId == 10).noneMatch(Monster::isAlive);

        if (isMonster10Dead) {
            resourceCollectionAnchorPane.setVisible((!isResourceCollected && mainCharacterImage.getBoundsInParent().intersects(resource12.getBoundsInParent())));
        }

    }

    private void startDamageCooldown() {
        // Prevent continuous damage by introducing a cooldown mechanism
        canTakeDamage = false;
        new Thread(() -> {
            try {
                Thread.sleep(500); // Cooldown period (500ms)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            canTakeDamage = true; // Allow taking damage again after cooldown
        }).start();
    }

    void startSpeedBoostTimer() {
        speedBoostTimer = new Timer();
        speedBoostTimerLabel.setVisible(true);
        speedBoostTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    speedBoostTimeRemaining--;
                    speedBoostTimerLabel.setText(String.valueOf(speedBoostTimeRemaining));
                    if (speedBoostTimeRemaining <= 0) {
                        speedBoostTimer.cancel();
                        speedBoostTimerLabel.setVisible(false);

                        // Access isSpeedBoostPickedUp through gameLoopLevel1
                        if (phase1Loop != null) {
                            phase1Loop.isSpeedBoostPickedUp = false; // Deactivate in GameLoop1
                        } else {
                            System.err.println("Error: gameLoopLevel1 is null in GuestLevel1Controller");
                        }
                        speedBoostTimeRemaining = 30;
                    }
                });
            }
        }, 0, 1000);
    }

    private void onGameOver() {
        // Game over logic here
        // Ensure onGameOver is called only once
        if (isGameOver) {
            return;
        }
        isGameOver = true;
        System.out.println("Game Over!");
        phase1Loop.stop(); // Stop the level 1 game loop
        gameLoop.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), phase1World); // 1-second fade out
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GuestDashboard.fxml"));
                Scene scene = new Scene(loader.load(), 912, 624);

                Object controller = loader.getController();
                if (controller != null && controller instanceof ButtonHandler buttonHandler) {
                    buttonHandler.setWindow(window);
                }

                window.setScene(scene);
                window.setTitle("Beyond the Galaxy");
                window.centerOnScreen();

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), scene.getRoot()); // 1-second fade in
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fadeOut.play();
    }
    @FXML
    private void bagButtonAction() {
        bagAnchorPane.setVisible(true);
        bagButton.setVisible(false);
    }

    @FXML
    private void bagAnchorPaneBackButton(){
        bagAnchorPane.setVisible(false);
        bagButton.setVisible(true);

    }
}