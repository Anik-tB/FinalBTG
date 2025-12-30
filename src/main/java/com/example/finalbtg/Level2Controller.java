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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Level2Controller {

    @FXML
    private AnchorPane level2World, pauseAnchorPane, restartConfirmAnchorPane,resourceCollectionAnchorPane, bagAnchorPane;
    @FXML
    private ImageView monsterImage13, monsterImage14, monsterImage15, monsterImage16, monsterImage17, monsterImage18, monsterImage19, monster20, monster21,monster22, monster23, monster24,monster25, monster26, monster27, monster28, monster29;
    @FXML
    private ImageView monsterHealthBar13, monsterHealthBar14, monsterHealthBar15, monsterHealthBar16, monsterHealthBar17, monsterHealthBar18, monsterHealthBar19, monsterHealthBar20, monsterHealthBar21, monsterHealthBar22, monsterHealthBar23, monsterHealthBar24,monsterHealthBar25, monsterHealthBar26, monsterHealthBar27, monsterHealthBar28, monsterHealthBar29;
    @FXML
    private ImageView mainCharacterImage, soundImage, musicImage, oxygen1, health1, resource12, projectileImage, monsterBoomImage, monsterProjectileImage, gate, speedBoost;
    @FXML
    private Button pauseButton,bagButton;
    @FXML
    private Label timerLabel, oxygenLabel, healthLabel, speedBoostTimerLabel;
    @FXML
    private Button collectResourceButton,engineButton,fuelButton,navigatorButton;

    private boolean isResourceCollected = false;


    private MainCharacter player;
    private Level2Loop level2Loop;
    private AnimationTimer gameLoop;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private List<ImageView> obstacles = new ArrayList<>();
    private List<loginMonster> monsters = new ArrayList<>();

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
    private int oxygenLevel=1000; // Initial oxygen level
    private long lastOxygenDecreaseTime; // To keep track of oxygen decrease
    private int health=1000 ; // Initial health value for the spaceman
    private static final double tileSize = 48.0;
    private int speedBoostTimeRemaining = 30; // Initial time for speed boost
    @FXML
    boolean isInvisible = false;
    private Timer invisibilityTimer;
    private int invisibilityTimeRemaining = 30;
    @FXML
    ImageView invisibilityPowerup;
    boolean isPowerupCollected = false; // Flag for collecting the power-up
    private Stage window;
    public void setWindow(Stage stage) {
        this.window = stage;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            this.window=(Stage)level2World.getScene().getWindow();
        });
        Image cursorImage = new Image(getClass().getResource("/image/Cursor.png").toExternalForm());
        Cursor customCursor = new ImageCursor(cursorImage); // Use ImageCursor
        level2World.setOnMouseEntered(event -> {
            level2World.setCursor(customCursor);
        });

        level2World.setOnMouseExited(event -> {
            level2World.setCursor(Cursor.DEFAULT);
        });
        collectResourceButton.setOnAction(event -> {
            resourceCollectionAnchorPane.setVisible(false);
            isResourceCollected = true; // Set the flag to true
            resource12.setVisible(false); // Hide the resource image
            engineButton.setVisible(true);
            fuelButton.setVisible(true);
            navigatorButton.setVisible(true);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("levels.fxml"));
                Scene scene = new Scene(loader.load(), 912, 624);

                ButtonHandler buttonHandler = loader.getController();
                buttonHandler.setWindow(window);

                // --- Get the level2AnchorPane and make it visible ---
                AnchorPane level3AnchorPane = (AnchorPane) scene.lookup("#level3AnchorPane");
                if (level3AnchorPane != null) {
                    level3AnchorPane.setVisible(true);
                    // --- Get the level2PlayButton and change its text ---
                    Button level2PlayButton = (Button) level3AnchorPane.lookup("#level3PlayButton");
                    if (level2PlayButton != null) {
                        level2PlayButton.setText("Play");
                    } else {
                        System.err.println("Error: level3PlayButton not found!");
                    }
                } else {
                    System.err.println("Error: level3AnchorPane not found!");
                }

                window.setScene(scene);
                window.setTitle("Levels");
                window.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        setupLevel2();
        updateMusicMuteIcon();
        updateSoundMuteIcon();
        // --- Add these lines in initialize() ---
        invisibilityPowerup = (ImageView)  level2World.lookup("#invisibilityPowerup"); // Get the ImageView from FXML
        if (invisibilityPowerup == null) {
            System.err.println("Error: Could not find invisibilityPowerup in FXML.");
        }
    }


    private void setupLevel2() {
        try {
            fillMoonSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player = new MainCharacter(mainCharacterImage, 25, 2592, 2640, 100, 200,1000);
        player.setGameWorld(level2World);
        //for monster chasing
        monsters.add(new loginMonster(monsterImage13, 15, 2342, 1965, 10, 100, 100, 48*48, 48*38, 48*63, 48*52, 13, monsterHealthBar13));
        monsters.add(new loginMonster(monsterImage14, 15, 2867, 1915, 10, 100, 100, 48*48, 48*38, 48*63, 48*52, 14, monsterHealthBar14));
        monsters.add(new loginMonster(monsterImage15, 15, 2792, 2090, 10, 100, 100, 48*48, 48*38, 48*63, 48*52, 15, monsterHealthBar15));
        monsters.add(new loginMonster(monsterImage16, 15, 2717, 2165, 10, 100, 100, 48*48, 48*38, 48*63, 48*52, 16, monsterHealthBar16));
        monsters.add(new loginMonster(monsterImage17, 15, 2492, 1865, 10, 100, 100, 48*48, 48*38, 48*63, 48*52, 17, monsterHealthBar17));
//      shoot kore with chasing
        monsters.add(new loginMonster(monsterImage18, 15, 1367, 1515, 10, 100, 150, 1117, 1165, 1792, 1690, 18, monsterHealthBar18));
        monsters.add(new loginMonster(monsterImage19, 15, 1367, 1240, 10, 1000, 150, 1117, 1165, 1792, 1690, 19, monsterHealthBar19));
        monsters.add(new loginMonster(monster22, 15, 1692, 1290, 10, 100, 150, 1117, 1165, 1792, 1690,22,monsterHealthBar22 ));

        //3rd no boundary r jonno monster create kora
        monsters.add(new loginMonster(monster25, 15, 892, 515, 10, 100, 150, 592, 415, 1167, 840,25,monsterHealthBar25 ));
        monsters.add(new loginMonster(monster26, 15, 892, 865, 10, 100, 150, 592, 415, 1167, 840,26,monsterHealthBar26 ));
        monsters.add(new loginMonster(monster27, 15, 1142, 815, 10, 100, 150, 592, 415, 1167, 840,27,monsterHealthBar27 ));
        monsters.add(new loginMonster(monster28, 15, 1267, 490, 10, 100, 150, 592, 415, 1167, 840,28,monsterHealthBar28 ));
        monsters.add(new loginMonster(monster29, 15, 1092, 540, 10, 100, 150, 592, 415, 1167, 840,29,monsterHealthBar29 ));

// eigula oxygen ar health bar er jonno
        monsters.add(new loginMonster(monster20, 0, 2842, 1740, 10, 100, 300, 2842, 1740, 2842, 1740, 20, monsterHealthBar20));
        monsters.add(new loginMonster(monster21, 0, 1882, 1265, 10, 100, 300, 1882, 1265, 1882, 1265, 21, monsterHealthBar21));

//        eigula daraiya shoot kore
        monsters.add(new loginMonster(monster23, 15, 892, 1265, 10, 100, 300, 892, 1265, 892, 1265,23,monsterHealthBar23 ));
        monsters.add(new loginMonster(monster24, 15, 2567, 1315, 10, 100, 300, 2567, 1315, 2567, 1315,24,monsterHealthBar24 ));

        monsters.forEach(monster -> monster.setGameWorld(level2World)); // Set game world for each monster

        addObstacles();
        level2Loop = new Level2Loop(player, activeKeys, obstacles, level2World, monsters, monsterBoomImage, this, gate);

        // --- Add this line ---
        speedBoostTimerLabel.setText(String.valueOf(speedBoostTimeRemaining));

        Platform.runLater(() -> {
            level2World.getScene().setOnKeyPressed(this::handleKeyPressed);
            level2World.getScene().setOnKeyReleased(this::handleKeyReleased);
        });
        level2Loop.start();

    }
    private void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }
    public void setupLevel2(MainCharacter player, int x, int y) throws IOException {
        if (player == null) {
            System.out.println("Error: Player object is null.");
            return;
        }
        this.player = player;
        this.player.setGameWorld(level2World);
        this.player.setPosition(x, y);
        this.health=health;
        this.oxygenLevel=oxygenLevel;

        updateHealthLabel();
        fillMoonSurface();
    }



    private void fillMoonSurface() throws IOException {

        String mapFilePath = "level2map.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            Image road = new Image(getClass().getResource("/image/tile/11.png").toExternalForm());
            Image road2 = new Image(getClass().getResource("/image/tile/12.png").toExternalForm());
            Image road3 = new Image(getClass().getResource("/image/tile/14.png").toExternalForm());
            Image road4 = new Image(getClass().getResource("/image/tile/13.png").toExternalForm());
            Image road5 = new Image(getClass().getResource("/image/tile/13.png").toExternalForm());
            Image stone = new Image(getClass().getResource("/image/tile/16.png").toExternalForm());
            Image stone2 = new Image(getClass().getResource("/image/tile/15.png").toExternalForm());
            Image star = new Image(getClass().getResource("/image/tile/s.jpg").toExternalForm());

            Canvas canvas = new Canvas(3216, 3120);
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
                        tileImage = road2;
                    }
                    if (tileType == '3') {
                        tileImage = road3;
                    }
                    if (tileType == '4') {
                        tileImage = road4;
                    }
                    if (tileType == '5') {
                        tileImage = road5;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '6') {
                        tileImage = stone;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '7') {
                        tileImage = stone2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '8') {
                        tileImage = star;
                    }
                    if (tileImage != null) {
                        gc.drawImage(tileImage, col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
                row++;
            }
            level2World.getChildren().add(canvas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainCharacterImage.toFront();
        gate.toFront();
        resource12.toFront();
        monsterImage13.toFront();
        monsterImage14.toFront();
        monsterImage15.toFront();
        monsterImage16.toFront();
        monsterImage17.toFront();
        monsterImage18.toFront();
        monsterImage19.toFront();
        monster20.toFront();
        monster21.toFront();
        monster22.toFront();
        monster23.toFront();
        monster24.toFront();
        monster25.toFront();
        monster26.toFront();
        monster27.toFront();
        monster28.toFront();



        projectileImage.toFront();
        monsterBoomImage.toFront();
        monsterProjectileImage.toFront();

        monsterHealthBar13.toFront();
        monsterHealthBar14.toFront();
        monsterHealthBar15.toFront();
        monsterHealthBar16.toFront();
        monsterHealthBar17.toFront();
        monsterHealthBar18.toFront();
        monsterHealthBar19.toFront();
        monsterHealthBar20.toFront();
        monsterHealthBar21.toFront();
        monsterHealthBar22.toFront();
        monsterHealthBar23.toFront();
        monsterHealthBar24.toFront();
        monsterHealthBar25.toFront();
        monsterHealthBar26.toFront();
        monsterHealthBar27.toFront();
        monsterHealthBar28.toFront();

        oxygen1.toFront();
        health1.toFront();
        speedBoost.toFront();
        invisibilityPowerup.toFront();
    }

    private void addRockObstacle(double x, double y) {
        ImageView rockObstacle = new ImageView(new Image(getClass().getResource("/image/tile/16.png").toExternalForm()));
        rockObstacle.setFitWidth(tileSize);
        rockObstacle.setFitHeight(tileSize);
        rockObstacle.setLayoutX(x);
        rockObstacle.setLayoutY(y);
        obstacles.add(rockObstacle);

    }
    private void addObstacles() {

        ImageView Monster8 = (ImageView) level2World.lookup("#monster8");
        ImageView Monster9 = (ImageView) level2World.lookup("#monster9");
        //  ImageView Resource12 = (ImageView) phase2World.lookup("#resource12");
        ImageView Level2Image = (ImageView) level2World.lookup("#level2image");
        ImageView Gate = (ImageView) level2World.lookup("#gate");


        if (Monster8 != null) {
            addObstacle(Monster8);
        }
        if (Monster9 != null) {
            addObstacle(Monster9);
        }
        //  if (Resource12 != null) {
        //     addObstacle(Resource12);
        //  }
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
    private void pauseButtonAction() {
        if (!isGamePaused) {
            level2Loop.stop();
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
            level2Loop.start();
            pauseAnchorPane.setVisible(false);
            if(!isPauseButtonVisible){
                pauseButton.setVisible(true);
            }
        }
    }

//        @FXML
//        private void restartButtonAction() {
//            phase3Loop.stop();
//            activeKeys.clear();
//            phase3World.getChildren().removeIf(node -> node instanceof Canvas);
//            player.setPosition(1056, 1942);
//            player.updateCharacterImage("DOWN");
//            //player.resetStats(100, 1200);
//            phase3World.setTranslateX(0.0);
//            phase3World.setTranslateY(0.0);
//            setupPhase3();
//            pauseAnchorPane.setVisible(false);
//            if(!isPauseButtonVisible){
//                pauseButton.setVisible(true);
//            }
//            isGamePaused = false;
//            phase3Loop.start();
//        }




    @FXML
    private void restartButtonAction() {
        restartConfirmAnchorPane.setVisible(true);
    }

    @FXML
    private void restartYesButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
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

    @FXML
    private void restartNoButtonAction() {
        restartConfirmAnchorPane.setVisible(false);
    }

    @FXML
    private void exitButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
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

    @FXML
    private void collectResourceButtonAction() {
        resourceCollectionAnchorPane.setVisible(false);
        isResourceCollected = true;
        resource12.setVisible(false);
        obstacles.remove(resource12);

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
            onGameOver();
            return;
        }
        long seconds = (remainingTime / 1000) % 60;
        long minutes = (remainingTime / (1000 * 60)) % 60;

        Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
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
                onGameOver();
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
            onGameOver(); // Trigger game over if health is 0
        }
    }

    public void updateHealthLabel() {
        // Update the health label text with current health
        healthLabel.setText(String.valueOf(health));

        // Change the text color based on the health value
        if (health <= 200) {
            healthLabel.setStyle("-fx-text-fill: red;"); // Low health (danger)
        } else if (health <= 500) {
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
        for (loginMonster monster : monsters) { // Check collision with each monster
            if (monster.isAlive() && mainCharacterImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                if (canTakeDamage && !isInvisible) {
                    decreaseHealth(10);
                    startDamageCooldown();
                }
            }
        }
        if (!isResourceCollected && mainCharacterImage.getBoundsInParent().intersects(resource12.getBoundsInParent())) {

            resourceCollectionAnchorPane.setVisible(true);
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
                        if (level2Loop != null) {
                            level2Loop.isSpeedBoostPickedUp = false; // Deactivate in GameLoop1
                        } else {
                            System.err.println("Error: gameLoopLevel1 is null in GuestLevel1Controller");
                        }
                        speedBoostTimeRemaining = 30;
                    }
                });
            }
        }, 0, 1000);
    }
    void startInvisibilityTimer() {
        isInvisible = true;
        mainCharacterImage.setOpacity(0.5); // আবার সম্পূর্ণ দৃশ্যমান করুন
        invisibilityTimer = new Timer();
        invisibilityTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    invisibilityTimeRemaining--;
                    // You can update a label to show the remaining time if needed

                    if (invisibilityTimeRemaining <= 0) {
                        invisibilityTimer.cancel();
                        isInvisible = false;
                        mainCharacterImage.setOpacity(1.0); // আবার সম্পূর্ণ দৃশ্যমান করুন
                        invisibilityTimeRemaining = 30; // Reset the timer
                        isPowerupCollected = false; // Reset the power-up collected flag
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
        level2Loop.stop(); // Stop the level 1 game loop
        gameLoop.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), level2World); // 1-second fade out
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
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