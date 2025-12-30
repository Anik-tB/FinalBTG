package com.example.finalbtg;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuestGameController {

    @FXML
    private ImageView mainCharacterImage, spaceship, fire, instructor, musicImage, soundImage;
    @FXML
    private AnchorPane guestGameWorld, bagAnchorPane, pauseAnchorPane,convo1, convo2, convo3, convo4, convo5, convoAnchorpane;
    @FXML
    private Button pauseButton,bagButton;


    @FXML
    public void initialize() {
        setupGuestGame();

        Platform.runLater(() -> {
            this.window = (Stage) guestGameWorld.getScene().getWindow();
        });
        updateMusicMuteIcon();
        updateSoundMuteIcon();
    }


    private boolean isGamePaused = false;
    private boolean isPauseButtonVisible = true;
    private boolean isSoundMute = false;
    private boolean isMusicMute = false;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private List<ImageView> obstacles = new ArrayList<>();
    private MainCharacter player;
    private GuestGameLoop gameLoop;
    private static final double tileSize = 48.0;
    private int convoState=0;
    private Stage window;

    public void setWindow(Stage stage) {
        this.window = stage;
    }

    private void changeScene(String fxmlfile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlfile));
            Scene scene = new Scene(loader.load(), 912, 624);

            Object controller = loader.getController();
            if (controller != null && controller instanceof GuestGameController guestController) {
                guestController.setWindow(window);
            } else if (controller != null && controller instanceof ButtonHandler buttonHandler) {
                buttonHandler.setWindow(window);
            }
            window.setScene(scene);
            window.setTitle(title);
            window.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setupGuestGame() {

        fillMoonSurface();
        player = new MainCharacter(mainCharacterImage, 20, 1488, 1200, 100,200, 1200);
        player.setGameWorld(guestGameWorld);
        gameLoop = new GuestGameLoop(player, activeKeys, obstacles,convoAnchorpane,instructor,this);
        Platform.runLater(() -> {
            guestGameWorld.getScene().setOnKeyPressed(this::handleKeyPressed);
            guestGameWorld.getScene().setOnKeyReleased(this::handleKeyReleased);
        });
        gameLoop.start();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (!isGamePaused) {
            activeKeys.add(event.getCode());
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    private void fillMoonSurface() {

        String mapFilePath = "worldmap.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            Image road = new Image(getClass().getResource("/image/tile/1.png").toExternalForm());
            Image road2 = new Image(getClass().getResource("/image/tile/2.png").toExternalForm());
            Image wall1 = new Image(getClass().getResource("/image/tile/3.png").toExternalForm());
            Image wall2 = new Image(getClass().getResource("/image/tile/4.png").toExternalForm());
            Image stone = new Image(getClass().getResource("/image/tile/4.png").toExternalForm());
            Image star = new Image(getClass().getResource("/image/tile/s.jpg").toExternalForm());
            Canvas canvas = new Canvas(2112, 1680);
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
                        tileImage = wall1;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '4') {
                        tileImage = wall2;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileType == '6') {
                        tileImage = star;
                        addRockObstacle(col * tileSize, row * tileSize);
                    }
                    if (tileImage != null) {
                        gc.drawImage(tileImage, col * tileSize, row * tileSize, tileSize, tileSize);
                    }
                }
                row++;
            }
            guestGameWorld.getChildren().add(canvas);

        } catch (IOException e) {
            e.printStackTrace();
        }
        addObstacles();

        if (instructor!=null) {instructor.toFront();}
        spaceship.toFront();
        mainCharacterImage.toFront();
        fire.toFront();
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

        ImageView spaceship = (ImageView) guestGameWorld.lookup("#spaceship");
        ImageView instructor = (ImageView) guestGameWorld.lookup("#instructor");

        if (spaceship != null) {
            addObstacle(spaceship);
        }
        if (instructor != null) {
            addObstacle(instructor);
        }
    }

    private void addObstacle(ImageView obstacle) {
        if (obstacle != null) {
            obstacles.add(obstacle);
        }
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
    private void pauseButtonAction() {
        if (!isGamePaused) {
            gameLoop.stop();
            isGamePaused = true;
            pauseAnchorPane.setVisible(true);
            pauseButton.setVisible(false);
            isPauseButtonVisible=false;
        }
    }

    @FXML
    private void soundButtonAction() {
        MainGame.isSoundMuted = !MainGame.isSoundMuted;
        updateSoundMuteIcon();
    }

    @FXML
    private void musicButtonAction() {
        boolean currentMuteState = MainGame.mediaPlayer.isMute();
        MainGame.mediaPlayer.setMute(!currentMuteState);
        isMusicMute = !currentMuteState;
        updateMusicMuteIcon();
    }

    private void updateSoundMuteIcon() {
        Platform.runLater(() -> {
            soundImage.setImage(null);
            if (MainGame.isSoundMuted) {
                soundImage.setImage(new Image("/image/icon/mutesound.png"));
            } else {
                soundImage.setImage(new Image("/image/icon/sound.png"));
            }
        });
    }

    private void updateMusicMuteIcon() {
        Platform.runLater(() -> {
            musicImage.setImage(null);
            if (MainGame.mediaPlayer.isMute()) {
                musicImage.setImage(new Image("/image/icon/mutemusic.png"));
            } else {
                musicImage.setImage(new Image("/image/icon/music.png"));
            }
        });
    }

    @FXML
    private void resumeButtonAction() {
        if (isGamePaused) {
            isGamePaused = false;
            gameLoop.start();
            pauseAnchorPane.setVisible(false);
            if (!isPauseButtonVisible) {
                pauseButton.setVisible(true);
            }
        }
    }

    @FXML
    private void restartButtonAction() {
        gameLoop.stop();
        activeKeys.clear();
        guestGameWorld.getChildren().removeIf(node -> node instanceof Canvas);
        player.setPosition(1488, 1200);
        player.updateCharacterImage("DOWN");
        guestGameWorld.setTranslateX(0.0);
        guestGameWorld.setTranslateY(0.0);
        setupGuestGame();

        pauseAnchorPane.setVisible(false);
        if (!isPauseButtonVisible) {
            pauseButton.setVisible(true);
        }
        isGamePaused = false;
        gameLoop.start();
    }

    @FXML
    private void exitButtonAction() {
        changeScene("guestDashboard.fxml", "Beyond the Galaxy");
    }


    public void checkLevel1Entry() {
        double x = player.getCharacterImageView().getLayoutX();
        double y = player.getCharacterImageView().getLayoutY();

        if ((x >= 1410 && x <= 1620 && y >= 260 && y <= 450)) {
            gameLoop.stop();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("phase1.fxml"));
                Scene scene = new Scene(loader.load(), 912, 624);

                Phase1Controller phase1Controller = loader.getController();
                phase1Controller.setWindow(window);

                phase1Controller.setupLevel1Game(player, 1634,2208);

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
    }

    @FXML
    private void convoNextButtonAction() {

        switch (convoState) {
            case 0:
                convo2.setVisible(true);
                break;
            case 1:
                convo3.setVisible(true);
                convo2.setVisible(false);
                convo1.setVisible(false);
                break;
            case 2:
                convo4.setVisible(true);
                break;
            case 3:
                convo5.setVisible(true);
                convo4.setVisible(false);
                convo3.setVisible(false);
                break;
            case 4:
                convo5.setVisible(false);
                convoAnchorpane.setVisible(false);
                instructor.setVisible(false); // Hide the instructor ImageView
                obstacles.remove(instructor);
                gameLoop.start(); // Restart the game loop
                convoState = -1; // Set convoState to -1 to prevent further conversations
                break;
        }
        convoState++;
    }
}