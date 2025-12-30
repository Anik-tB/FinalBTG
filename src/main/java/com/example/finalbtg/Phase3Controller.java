package com.example.finalbtg;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

public class Phase3Controller {

    @FXML
    private AnchorPane phase3World, pauseAnchorPane, restartConfirmAnchorPane,resourceCollectionAnchorPane,bagAnchorPane,winAnchorPane,fixAnchorPane;
    @FXML
    private ImageView monsterImage30, monsterImage31,  monster32, monster33,monster34, monster35,monster36,monster37,monster38,monster39,monster40;
    @FXML
    private ImageView monsterHealthBar30, monsterHealthBar31, monsterHealthBar32, monsterHealthBar33, monsterHealthBar34, monsterHealthBar35,monsterHealthBar36,monsterHealthBar37,monsterHealthBar38,monsterHealthBar39,monsterHealthBar40,petImage;
    @FXML
    private ImageView mainCharacterImage, soundImage, musicImage, oxygen1, health1, resource12, projectileImage, monsterBoomImage, monsterProjectileImage,PetProjectileImage, gate, speedBoost,spaceship,fire;
    @FXML
    private Button pauseButton,bagButton,petButton;
    @FXML
    private Label timerLabel, oxygenLabel, healthLabel, speedBoostTimerLabel;
    @FXML
    private Button collectResourceButton,controllerButton1,controllerButton;
    private boolean isResourceCollected = false;
    @FXML
    protected ImageView monster41,monster42;
    @FXML
    protected ImageView monsterHealthBar41,monsterHealthBar42;


    private MainCharacter player;
    private Phase3Loop phase3Loop;
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
    private  boolean confirm = false;


    private Pet pet;
    @FXML
    boolean isInvisible = false;
    private Timer invisibilityTimer;
    private int invisibilityTimeRemaining = 10;
    @FXML
    ImageView invisibilityPowerup;
    boolean isPowerupCollected = false; // Flag for collecting the power-up

    boolean isPetVisible = false;
    private Timer petTimer;
    private int petTimeRemaining = 20;

    private Stage window;

    public void setWindow(Stage stage) {
        this.window = stage;
    }

    @FXML
    public void initialize() {
        Image cursorImage = new Image(getClass().getResource("/image/Cursor.png").toExternalForm());
        Cursor customCursor = new ImageCursor(cursorImage); // Use ImageCursor

        phase3World.setOnMouseEntered(event -> {
            phase3World.setCursor(customCursor);
        });

        phase3World.setOnMouseExited(event -> {
            phase3World.setCursor(Cursor.DEFAULT);
        });

        collectResourceButton.setOnAction(event -> {
            resourceCollectionAnchorPane.setVisible(false);
            controllerButton.setVisible(true);
            isResourceCollected = true; // Set the flag to true
            resource12.setVisible(false); // Hide the resource image
            isPetVisible = false;


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
        setupPhase3();
        updateMusicMuteIcon();
        updateSoundMuteIcon();
        // --- Add these lines in initialize() ---
        invisibilityPowerup = (ImageView) phase3World.lookup("#invisibilityPowerup"); // Get the ImageView from FXML
        if (invisibilityPowerup == null) {
            System.err.println("Error: Could not find invisibilityPowerup in FXML.");
        }
    }

    private void setupPhase3() {
        try {
            fillMoonSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player = new MainCharacter(mainCharacterImage, 25, 1056, 1942, 100, 200,1000);
        player.setGameWorld(phase3World);

        pet = new Pet(petImage, 20, 720, 1632,activeKeys,monsters, phase3World, PetProjectileImage); // Initialize the pet
        pet.setGameWorld(phase3World); // Set the game world for the pet
        //for monster
        monsters.add(new Monster(monsterImage30, 15, 906, 1417, 10, 100, 50, 681, 1267, 1456, 1742, 30, monsterHealthBar30));
        monsters.add(new Monster(monsterImage31, 15, 1181, 1567, 10, 100, 50, 681, 1267, 1456, 1742, 31, monsterHealthBar31));


//        monsters.add(new Monster(monsterImage6, 15, 1342, 1215, 10, 100, 50, 48*21, 48*24, 48*41, 48*37, 6, monsterHealthBar6));
//        monsters.add(new Monster(monsterImage7, 15, 1167, 1590, 10, 100, 50, 48*21, 48*24, 48*41, 48*37, 7, monsterHealthBar7));
//32 for oxygen
        monsters.add(new Monster(monster32, 0, 681, 1717, 10, 100, 300, 681, 1717, 681, 1717, 32, monsterHealthBar32));
        monsters.add(new Monster(monster33, 0, 206, 1362, 10, 100, 300, 206, 1362, 206, 1362, 33, monsterHealthBar33));
//for shoooting with chasing
        monsters.add(new Monster(monster38, 15, 506, 717, 10, 100, 75, 206, 492, 1006, 917,38,monsterHealthBar38 ));
        monsters.add(new Monster(monster39, 15, 781, 717, 10, 100, 80, 206, 492, 1006, 917,39,monsterHealthBar39 ));

//        darano shoot

//        monsters.add(new Monster(monster34, 0, 1706, 1267, 10, 100, 300, 1706, 1267, 1706, 1267,34,monsterHealthBar34 ));
        monsters.add(new Monster(monster35, 0, 1056, 1267, 10, 100, 300, 1056, 1267, 1056, 1267,35,monsterHealthBar35 ));
        monsters.add(new Monster(monster36, 0, 206, 492, 10, 100, 300, 206, 492, 206, 492,36,monsterHealthBar36 ));
//for invisibilityyyyyy powerb er er jonno]
     //   monsters.add(new Monster(monster37, 0, 806, 492, 10, 100, 300, 806, 492, 806, 492,37,monsterHealthBar37 ));

//       final boss
        monsters.add(new Monster(monster40, 15, 606, 667, 10, 1000, 100, 206, 492, 1006, 917,40,monsterHealthBar40 ));
//        //final boss 3 chelapela
        monsters.add(new Monster(monster41, 15, 506, 717, 10, 300, 90, 206, 492, 1006, 917,41,monsterHealthBar41 ));
        monsters.add(new Monster(monster42, 15, 781, 717, 10, 300, 70, 206, 492, 1006, 917,42,monsterHealthBar42 ));

        monsters.forEach(monster -> monster.setGameWorld(phase3World)); // Set game world for each monster



        addObstacles();
        phase3Loop = new Phase3Loop(player, activeKeys, obstacles, phase3World, monsters,pet, monsterBoomImage, this, gate,fixAnchorPane);


        // --- Add this line ---
        speedBoostTimerLabel.setText(String.valueOf(speedBoostTimeRemaining));

        Platform.runLater(() -> {
            phase3World.getScene().setOnKeyPressed(this::handleKeyPressed);
            phase3World.getScene().setOnKeyReleased(this::handleKeyReleased);
        });
        phase3Loop.start();

    }

    private void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    public void setupPhase3(MainCharacter player, int x, int y,int health,int oxygenLevel) throws IOException {
        if (player == null) {
            System.out.println("Error: Player object is null.");
            return;
        }
        this.player = player;
        this.player.setGameWorld(phase3World);
        this.player.setPosition(x, y);
        this.health=health;
        this.oxygenLevel=oxygenLevel;

        updateHealthLabel();

        fillMoonSurface();
    }


    private void fillMoonSurface() throws IOException {

        String mapFilePath = "phase3map.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            Image road = new Image(getClass().getResource("/image/tile/1.png").toExternalForm());
            Image road2 = new Image(getClass().getResource("/image/tile/2.png").toExternalForm());
            Image wall = new Image(getClass().getResource("/image/tile/3.png").toExternalForm());
            Image wall2 = new Image(getClass().getResource("/image/tile/4.png").toExternalForm());
            Image star = new Image(getClass().getResource("/image/tile/s.jpg").toExternalForm());

            Canvas canvas = new Canvas(1680, 2400);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double tileSize=48.0;
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
                        tileImage = wall;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '4') {
                        tileImage = wall2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == 's') {
                        tileImage = star;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileImage != null) {
                        gc.drawImage(tileImage, col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
                row++;
            }
            phase3World.getChildren().add(canvas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainCharacterImage.toFront();
        petImage.toFront();
        gate.toFront();
        spaceship.toFront();
        fire.toFront();
        resource12.toFront();
        monsterImage30.toFront();
        monsterImage31.toFront();
        monster32.toFront();
        monster33.toFront();
//        monster34.toFront();
        monster35.toFront();
        monster36.toFront();
     //   monster37.toFront();
        monster38.toFront();
        monster39.toFront();
        monster40.toFront();
        monster41.toFront();
        monster42.toFront();

        projectileImage.toFront();
        monsterBoomImage.toFront();
        monsterProjectileImage.toFront();
        PetProjectileImage.toFront();
        monsterHealthBar30.toFront();
        monsterHealthBar31.toFront();
        monsterHealthBar32.toFront();
        monsterHealthBar33.toFront();
        monsterHealthBar34.toFront();
        monsterHealthBar35.toFront();
        monsterHealthBar36.toFront();
        monsterHealthBar37.toFront();
        monsterHealthBar38.toFront();
        monsterHealthBar39.toFront();
        monsterHealthBar40.toFront();
        monsterHealthBar41.toFront();
        monsterHealthBar42.toFront();

        oxygen1.toFront();
        health1.toFront();
        speedBoost.toFront();
        invisibilityPowerup.toFront();
    }

    private void addRockObstacle(double x, double y) {
        ImageView rockObstacle = new ImageView(new Image(getClass().getResource("/image/tile/4.png").toExternalForm()));
        rockObstacle.setFitWidth(tileSize);
        rockObstacle.setFitHeight(tileSize);
        rockObstacle.setLayoutX(x);
        rockObstacle.setLayoutY(y);
        obstacles.add(rockObstacle);

    }


    private void addObstacles() {

        ImageView Monster8 = (ImageView) phase3World.lookup("#monster8");
        ImageView Monster9 = (ImageView) phase3World.lookup("#monster9");
        // ImageView Resource12 = (ImageView) phase3World.lookup("#resource12");
        ImageView Level2Image = (ImageView) phase3World.lookup("#level2image");
        ImageView Gate = (ImageView) phase3World.lookup("#gate");


        if (Monster8 != null) {
            addObstacle(Monster8);
        }
        if (Monster9 != null) {
            addObstacle(Monster9);
        }
        // if (Resource12 != null) {
        //   addObstacle(Resource12);
        // }
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



    public boolean isAttackConfirm() {
        return confirm;

    }
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;}



    // pause and settings.....................................................................................


    @FXML
    private void pauseButtonAction() {
        if (!isGamePaused) {
            phase3Loop.stop();
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
            phase3Loop.start();
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


//    public void checkLevel4Entry(){
//        phase3Loop.stop();
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("phase3.fxml"));
//            Scene scene = new Scene(loader.load(), 912, 624);
//
//            Phase3Controller phase3Controller = loader.getController();
//            phase3Controller.setWindow(window);
//
//            phase3Controller.setupPhase3(player, 1056, 1942);
//
//            // Create a FadeTransition
//            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), window.getScene().getRoot()); // 1 second fade
//            fadeTransition.setFromValue(1.0);
//            fadeTransition.setToValue(0.0);
//            fadeTransition.setOnFinished(event -> {
//                window.setScene(scene);
//                window.setTitle("Beyond The Galaxy");
//                window.centerOnScreen();
//
//                // Fade in the new scene
//                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), scene.getRoot());
//                fadeIn.setFromValue(0.0);
//                fadeIn.setToValue(1.0);
//                fadeIn.play();
//            });
//            fadeTransition.play();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    @FXML
    private void collectResourceButtonAction() {
        resourceCollectionAnchorPane.setVisible(false);
        isResourceCollected = true;
        resource12.setVisible(false);
        obstacles.remove(resource12);
//        controllerButton.setVisible(true);

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
        for (Monster monster : monsters) { // Check collision with each monster
            if (monster.isAlive() && mainCharacterImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
                if (canTakeDamage) {
                    decreaseHealth(10);
                    startDamageCooldown();
                }
            }
        }

        resourceCollectionAnchorPane.setVisible(!isResourceCollected && mainCharacterImage.getBoundsInParent().intersects(resource12.getBoundsInParent()));

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
                        if (phase3Loop != null) {
                            phase3Loop.isSpeedBoostPickedUp = false; // Deactivate in GameLoop1
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
    void updatePetVisibility() {
        if (isPetVisible) {
            // Show the pet by setting the image view to visible
            petImage.setVisible(true);
        } else {
            // Hide the pet by setting the image view to invisible
            petImage.setVisible(false);
        }
    }

    private void onGameOver() {
        // Game over logic here
        // Ensure onGameOver is called only once
        if (isGameOver) {
            return;
        }
        isGameOver = true;
        System.out.println("Game Over!");
        phase3Loop.stop(); // Stop the level 1 game loop
        gameLoop.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), phase3World); // 1-second fade out
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

    @FXML
    private void handlePetButtonAction(ActionEvent event) {
        if (!isPetVisible) {
            isPetVisible = true;
            petImage.setVisible(true);
            startPetTimer();
            // You can remove the button here if needed:
            petButton.setVisible(false);
        }
    }

    void startPetTimer() {
        petTimer = new Timer();
        petTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    petImage.setVisible(false);
                    isPetVisible = false;
                    startPetCooldown(); // Start the 30-second cooldown
                });
            }
        }, 10000); // 10 seconds in milliseconds
    }
    void startPetCooldown() {
        petTimer = new Timer();
        petTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // No need to do anything here, the pet is already invisible
            }
        }, 30000); // 30 seconds in milliseconds
    }
    @FXML
    private void fixButtonAction() {
        winAnchorPane.setVisible(true);
    }
}