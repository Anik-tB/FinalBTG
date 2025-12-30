package com.example.finalbtg;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Level3Controller {

    @FXML
    private AnchorPane level3World,pauseAnchorPane,restartConfirmAnchorPane;
    @FXML
    private ImageView mainCharacterImage,soundImage,musicImage,spaceship,fire;
    @FXML
    private Button pauseButton;

    private MainCharacter player;
    private Level3Loop level3Loop;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private List<ImageView> obstacles = new ArrayList<>();
    private static final double tileSize = 48.0;

    private boolean isGamePaused = false;
    private boolean isPauseButtonVisible =true;
    private boolean isSoundMute=false;
    private boolean isMusicMute=false;

    private Stage window;
    public void setWindow(Stage stage) {
        this.window = stage;
    }

    @FXML
    public void initialize() {
        setupLevel3();
        Platform.runLater(() -> {
            this.window = (Stage) level3World.getScene().getWindow();
        });

    }

    private void setupLevel3() {
        try{
            fillMoonSurface();
        }catch (IOException e){
            e.printStackTrace();
        }

        player = new MainCharacter(mainCharacterImage, 25, 1056, 1942, 100, 200,1000);
        player.setGameWorld(level3World);
        level3Loop = new Level3Loop(player, activeKeys, obstacles, level3World);

        Platform.runLater(() -> {
            level3World.getScene().setOnKeyPressed(this::handleKeyPressed);
            level3World.getScene().setOnKeyReleased(this::handleKeyReleased);
        });
        level3Loop.start();

    }

    public void setupLevel3(MainCharacter player, int x, int y) throws IOException {
        if (player == null) {
            System.out.println("Error: Player object is null.");
            return;
        }
        this.player = player;
        this.player.setGameWorld(level3World);
        this.player.setPosition(x, y);
        fillMoonSurface();
    }

    private void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    private void fillMoonSurface() throws IOException {

        String mapFilePath = "level3map.txt";

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
            level3World.getChildren().add(canvas);
        } catch (IOException e) {
            e.printStackTrace();
        }

        spaceship.toFront();
        fire.toFront();
        mainCharacterImage.toFront();
    }

    private void addRockObstacle(double x, double y) {
        ImageView rockObstacle = new ImageView(new Image(getClass().getResource("/image/tile/4.png").toExternalForm()));
        rockObstacle.setFitWidth(tileSize);
        rockObstacle.setFitHeight(tileSize);
        rockObstacle.setLayoutX(x);
        rockObstacle.setLayoutY(y);

        obstacles.add(rockObstacle);

    }


    @FXML
    private void pauseButtonAction() {
        if (!isGamePaused) {
            level3Loop.stop();
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
            level3Loop.start();
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
}
