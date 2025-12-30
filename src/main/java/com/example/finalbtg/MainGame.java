package com.example.finalbtg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;


public class MainGame extends Application {

    public static MediaPlayer mediaPlayer;
    public static Media media;
    public static boolean isSoundMuted = false;

    @Override
    public void start(Stage window) throws IOException {

        FXMLLoader loader = new FXMLLoader(MainGame.class.getResource("homePage.fxml"));
        Scene scene = new Scene(loader.load(), 912, 624);
        media = new Media(getClass().getResource("/bgm.mp3").toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
//         mediaPlayer.play();
        ButtonHandler controller = loader.getController();
        controller.setWindow(window);
        window.setTitle("Beyond The Galaxy");
        window.setScene(scene);
        window.centerOnScreen();
        window.setResizable(false);
        window.show();
    }

    public static void main(String[] args) {
        launch();
    }
}