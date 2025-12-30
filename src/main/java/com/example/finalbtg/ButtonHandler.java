package com.example.finalbtg;

import com.sun.prism.paint.Color;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javafx.stage.Stage;

import java.io.*;

public class ButtonHandler {

    @FXML
    private AnchorPane sellAnchorPane, setPriceAnchorPane,shopStore,cardsAnchorPane,cardsAnchorPaneHolder,leaderboardAnchorPane;

    @FXML
    private Button shopSellButton, sellBackButton, setPriceBackButton, sellItemButton;
    @FXML
    private TextField setPriceTextField;
    @FXML
    private ScrollPane shopScrollPane;
    @FXML
    private GridPane itemGridPane;

    private ShopClient shopClient;
    private boolean isShopClientConnected = false;
    private ConcurrentHashMap<String, String> onSaleItems = new ConcurrentHashMap<>();



    @FXML
    private Label toggleAnchorPaneLabel,shopGemsLabel, createErrorMassage,superCharacterPower, loginErrorMassage, profileUsernameLabel, btgLabel,label1,label2,label3,gemLabel,weaponPower,shieldPower,specialCardPower;
    @FXML
    private TextField createUser, existUser,chatTextField;
    @FXML
    private PasswordField createPass, existPass;
    @FXML
    private AnchorPane pvpWaitingAnchorPane,settingAnchorPane,shopAnchorPane,supercharacterAnchorpane,spaceshipAnchorpane,upButtonAnchorPane,downButtonAnchorPane, createAccountAnchorPane, profileAnchorPane, loginAccountAnchorPane, chatAnchorPane;
    @FXML
    private Button chatSentButton, loginPageToggleButton, level1PlayButton, level2PlayButton, level3PlayButton, level4PlayButton;
    @FXML
    private ImageView soundImage, musicImage,specialCardImg;
    @FXML
    private AnchorPane level1AnchorPane, level2AnchorPane, level3AnchorPane;
    @FXML
    private VBox chatVbox;
    @FXML
    private ScrollPane chatScrollPane;

    private ChatClient chatClient;
    private boolean isChatClientConnected = false; // Add this line
    private PvpClient pvpClient;
    //    private boolean isAwaitingPlayer = false;

    private boolean isSoundMute = false;
    private boolean isMusicMute = false;
    private boolean isSignupMode = true;


    public static String loggedInUsername;
    private static final String DEFAULT_PROGRESS = "false";

    private Stage window;


    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public ButtonHandler getButtonHandler(){ return this;}

    @FXML
    public void initialize() {
        updateMusicMuteIcon();
        updateSoundMuteIcon();
        if (profileUsernameLabel != null && ButtonHandler.loggedInUsername != null) {
            profileUsernameLabel.setText(ButtonHandler.loggedInUsername);
        }
    }

    public void setWindow(Stage stage) {
        this.window = stage;
    }

    private void changeScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 912, 624);

            Object controller = loader.getController();

            if (controller instanceof GuestGameController guestGameController) {
                guestGameController.setWindow(window);
            } else if (controller instanceof ButtonHandler buttonHandler) {
                buttonHandler.setWindow(window);
            }
            window.setScene(scene);
            window.setTitle("Beyond The Galaxy");
            window.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Home Page.................................................................................

    @FXML
    private void GuestButtonAction() {
        changeScene("guestDashboard.fxml");
    }

    @FXML
    private void LoginButtonAction() {
        changeScene("loginPage.fxml");
    }




    // guest dashboard................................................................................................

    @FXML
    private void guestPlayButtonAction() {
        changeScene("guestGameWorld.fxml");
    }

    @FXML
    private void guestReturnButtonAction() {
        changeScene("homePage.fxml");
    }






    // Login Dashboard............................................................................................


    @FXML
    private void loginPlayButtonAction() {
        changeScene("levels.fxml");
    }

    @FXML
    private void logOutButtonAction() {
        if (chatClient != null) {
            chatClient.disconnectFromServer(); // Disconnect the client
            isChatClientConnected = false; // Update status
        }
        changeScene("loginPage.fxml");
    }

    @FXML
    private void profileButtonAction() {
        profileAnchorPane.setVisible(true);
        upButtonAnchorPane.setVisible(false);
        downButtonAnchorPane.setVisible(false);
        settingAnchorPane.setVisible(false);
        btgLabel.setVisible(false);

        // --- Read the user file and set the number of gems to the gemLabel ---
        updateGemLabelForProfile();
    }

    // The updateGemLabel() method remains the same as before:
    private void updateGemLabelForProfile() {
        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        int gems = Integer.parseInt(line.split(",")[1]);
                        Platform.runLater(() -> gemLabel.setText(String.valueOf(gems)));
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void profileBackButtonAction() {
        profileAnchorPane.setVisible(false);
        upButtonAnchorPane.setVisible(true);
        downButtonAnchorPane.setVisible(true);
        btgLabel.setVisible(true);

    }



    //leaderBoard..............................................................................................

    @FXML
    private void leaderboardButtonAction() {
       leaderboardAnchorPane.setVisible(true);
       profileAnchorPane.setVisible(false);
    }

    @FXML
    private void leaderboardBackButtonAction() {
        leaderboardAnchorPane.setVisible(false);
        profileAnchorPane.setVisible(true);
    }


    //pvp button................................................................................................

    @FXML
    private void pvpButtonAction() {
        String attackPower = weaponPower.getText();
        String health = shieldPower.getText();

        if (pvpClient == null) {
            pvpClient = new PvpClient(window, this); // Pass 'this' (ButtonHandler)
            pvpClient.connect();
        }
        pvpWaitingAnchorPane.setVisible(true);
    }

    public void showWaitingForOpponent() {
        pvpWaitingAnchorPane.setVisible(true);
    }

    public void hideWaitingForOpponent() {
        pvpWaitingAnchorPane.setVisible(false);
    }



    // spaceship...........................................................................

    @FXML
    private void spaceshipButtonAction() {
        spaceshipAnchorpane.setVisible(true);
        settingAnchorPane.setVisible(false);
        upButtonAnchorPane.setVisible(false);
        downButtonAnchorPane.setVisible(false);
        btgLabel.setVisible(false);

        // --- Read resource
        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4 && parts[0].equals("resource")) {

                        int resource1 = Integer.parseInt(parts[1]);
                        int  resource2 = Integer.parseInt(parts[2]);
                        int  resource3 = Integer.parseInt(parts[3]);
                        label1.setText(resource1+"/10");
                        label2.setText(resource2+"/10");
                        label3.setText(resource3+"/10");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @FXML
    private void spaceshipBackButtonAction() {
        spaceshipAnchorpane.setVisible(false);
        upButtonAnchorPane.setVisible(true);
        downButtonAnchorPane.setVisible(true);
        btgLabel.setVisible(true);

    }


    // super character........................................................................

    @FXML
    private void supercharacterButtonAction() {
        supercharacterAnchorpane.setVisible(true);
        settingAnchorPane.setVisible(false);
        upButtonAnchorPane.setVisible(false);
        downButtonAnchorPane.setVisible(false);
        btgLabel.setVisible(false);

        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals("weapon")) {

                        int weapon = Integer.parseInt(parts[1]);
                        weaponPower.setText(weapon+"");

                    }
                    else if( parts[0].equals("shield")){
                        int shields=Integer.parseInt(parts[1]);
                        shieldPower.setText(shields+"");

                    }
                    else if(parts[0].equals("cardPower")){
                        int cardspower=Integer.parseInt(parts[1]);
                        specialCardPower.setText(cardspower+"");

                    }
                    else if(parts[0].equals("specialcard")){

                        int highestSpecialCardNumber = 0;
                        // Start from index 1 as index 0 is the identifier "specialcard"
                        for (int i = 1; i < parts.length; i++) {
                            int currentNumber = Integer.parseInt(parts[i]);
                            if (currentNumber > highestSpecialCardNumber) {
                                highestSpecialCardNumber = currentNumber;
                            }
                        }

                        if(highestSpecialCardNumber==1){
                            specialCardImg.setImage(new Image("image/icon/card1.png"));
                            specialCardPower.setText(1000+"");
                        }
                        else if(highestSpecialCardNumber==2){
                            specialCardImg.setImage(new Image("image/icon/card2.png"));
                            specialCardPower.setText(1100+"");
                        } else if (highestSpecialCardNumber==3) {
                            specialCardImg.setImage(new Image("image/icon/card3.png"));
                            specialCardPower.setText(1200+"");
                        }
                        else if (highestSpecialCardNumber==4) {
                            specialCardImg.setImage(new Image("image/icon/card4.png"));
                            specialCardPower.setText(1300+"");
                        }
                        else if (highestSpecialCardNumber==5) {
                            specialCardImg.setImage(new Image("image/icon/card5.png"));
                            specialCardPower.setText(1400+"");
                        }
                        else if (highestSpecialCardNumber==6) {
                            specialCardImg.setImage(new Image("image/icon/card6.png"));
                            specialCardPower.setText(1500+"");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
        superCharacterPower.setText(Integer.parseInt(shieldPower.getText())+Integer.parseInt(weaponPower.getText())+Integer.parseInt(specialCardPower.getText())+"");
    }

    @FXML
    private void supercharacterBackButtonAction() {
        supercharacterAnchorpane.setVisible(false);
        upButtonAnchorPane.setVisible(true);
        downButtonAnchorPane.setVisible(true);
        btgLabel.setVisible(true);
    }

    @FXML
    private void cardStorageButtonAction() {
        cardsAnchorPane.setVisible(true);

        // Read the user file and set the image in cardsAnchorPaneHolder
        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("specialcard,")) {
                        String[] parts = line.split(",");

                        // Create GridPane with padding
                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10); // Set horizontal gap
                        gridPane.setVgap(10); // Set vertical gap

                        // Calculate padding (10px on each side)
                        Insets padding = new Insets(10, 10, 10, 10);
                        gridPane.setPadding(padding);

                        cardsAnchorPaneHolder.getChildren().clear(); // Clear previous content

                        // Set the GridPane's layout parameters to fill the cardsAnchorPaneHolder
                        AnchorPane.setTopAnchor(gridPane, 0.0);
                        AnchorPane.setRightAnchor(gridPane, 0.0);
                        AnchorPane.setBottomAnchor(gridPane, 0.0);
                        AnchorPane.setLeftAnchor(gridPane, 0.0);

                        cardsAnchorPaneHolder.getChildren().add(gridPane);

                        int col = 0;
                        int row = 0;

                        // Start from index 1 as index 0 is the identifier "specialcard"
                        for (int i = 1; i < parts.length; i++) {
                            String cardId = parts[i];
                            String imagePath;

                            // Determine image path based on card ID
                            if (cardId.equals("1")) {
                                imagePath = "image/icon/card1.png";
                            } else if (cardId.equals("2")) {
                                imagePath = "image/icon/card2.png";
                            } else if (cardId.equals("3")) {
                                imagePath = "image/icon/card3.png";
                            } else if (cardId.equals("4")) {
                                imagePath = "image/icon/card4.png";
                            } else if (cardId.equals("5")) {
                                imagePath = "image/icon/card5.png";
                            } else if (cardId.equals("6")) {
                                imagePath = "image/icon/card6.png";
                            } else {
                                // Default image or error handling
                                imagePath = "image/icon/defaultCard.png";
                                System.err.println("Invalid card ID: " + cardId);
                            }

                            // Create ImageView and add to GridPane
                            ImageView cardImageView = new ImageView(new Image(imagePath));
                            cardImageView.setFitWidth(80); // Adjust as needed
                            cardImageView.setFitHeight(80); // Adjust as needed
                            cardImageView.setPreserveRatio(true);

                            gridPane.add(cardImageView, col, row);

                            col++;
                            if (col >= 3) { // Adjust for desired number of columns
                                col = 0;
                                row++;
                            }
                        }
                        break; // Exit loop after processing specialcard line
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void cardsBackButtonAction(){
        cardsAnchorPane.setVisible(false);
    }


    // shop....................................................................................

    @FXML
    private void shopButtonAction() {
        if (!isShopClientConnected) {
            // Start the ShopServer if it's not already running
            if (!ChatServer.isServerRunning) {
                new Thread(() -> {
                    ShopServer shopServer = new ShopServer();
                    shopServer.start();
                }).start();
            }

            // Connect the ShopClient to the server
            shopClient = new ShopClient(this, loggedInUsername);
            shopClient.connect();

            isShopClientConnected = true;
        }

        shopAnchorPane.setVisible(true);
        settingAnchorPane.setVisible(false);
        upButtonAnchorPane.setVisible(false);
        downButtonAnchorPane.setVisible(false);
        btgLabel.setVisible(false);
        updateShopGemsLabel();
    }



    @FXML
    private void shopSellButtonAction() {
        sellAnchorPane.setVisible(true);
        shopStore.setVisible(false);
        List<String> specialCards = getSpecialCardsForUser(loggedInUsername);
        addSpecialCardButtons(specialCards);
    }

    @FXML
    private void sellBackButtonAction() {
        sellAnchorPane.setVisible(false);
        shopStore.setVisible(true);
    }

    @FXML
    private void setPriceBackButtonAction() {
        setPriceAnchorPane.setVisible(false);
        sellAnchorPane.setVisible(true);
    }

    @FXML
    private void shopBackButtonAction() {
        if (shopClient != null && shopClient.isConnected()) {
            shopClient.disconnect(); // Disconnect the client when leaving the shop
            isShopClientConnected = false; // Update status
        }

        // Clear all buttons from the shop
        itemGridPane.getChildren().clear();

        shopAnchorPane.setVisible(false);
        sellAnchorPane.setVisible(false);
        setPriceAnchorPane.setVisible(false);
        shopStore.setVisible(true);
        upButtonAnchorPane.setVisible(true);
        downButtonAnchorPane.setVisible(true);
        btgLabel.setVisible(true);
    }



    // chat.................................................................................


    @FXML
    private void chatButtonAction() {

        if (!isChatClientConnected) {
            if (!ChatServer.isServerRunning) { // Check the flag here
                // start server
                new Thread(() -> {
                    ChatServer chatServer = new ChatServer();
                    chatServer.startServer();
                }).start();
            }

            // connecting the client to the server
            chatClient = new ChatClient(loggedInUsername);
            chatClient.connectToServer();

            new Thread(() -> {
                String message;
                while ((message = chatClient.receiveMessage()) != null) {
                    displayChatMessage(message);
                }
            }).start();

            isChatClientConnected = true;
        }

        applyScrollBarStyles();

        chatAnchorPane.setVisible(true);
        upButtonAnchorPane.setVisible(false);
        downButtonAnchorPane.setVisible(false);
        settingAnchorPane.setVisible(false);
        btgLabel.setVisible(false);
    }

    @FXML
    private void chatBackButtonAction() {
        chatTextField.clear();
        chatAnchorPane.setVisible(false);
        upButtonAnchorPane.setVisible(true);
        downButtonAnchorPane.setVisible(true);
        btgLabel.setVisible(true);
    }

    @FXML
    private void chatSentButtonAction() {
        String message = chatTextField.getText();
        if (!message.isEmpty()) {
            chatClient.sendMessage(message); // Send message to the server
            chatTextField.clear();
        }
    }

    private void displayChatMessage(String message) {
        Platform.runLater(() -> {
            TextFlow textFlow = new TextFlow();
            Text messageText = new Text(message);
            messageText.setStyle(
                    "-fx-font-family: 'Arial Black';" +
                            "-fx-font-size: 13px;" +
                            "-fx-text-fill: #FFFF00;"
            );
            textFlow.getChildren().add(messageText);
            textFlow.setStyle(
                    "-fx-padding: 5px;" +
                            "-fx-background-color: #FFFFFF;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-border-radius: 8px;" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-color: #000000;" +
                            "-fx-wrap-text: true;" // Enable wrapping
            );
            chatVbox.getChildren().add(textFlow);

        });
    }

    private void applyScrollBarStyles() {
        chatScrollPane.applyCss();
        Skin<?> skin = chatScrollPane.getSkin();
        if (skin instanceof javafx.scene.control.skin.ScrollPaneSkin) {
            ScrollBar vScrollBar = ((javafx.scene.control.skin.ScrollPaneSkin) skin).getVerticalScrollBar();
            vScrollBar.setStyle(
                    "-fx-background-color: #202020; " +
                            "-fx-border-color: transparent; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-pref-width: 10px;"
            );
            vScrollBar.lookup(".thumb").setStyle(
                    "-fx-background-color: #A0A0A0; " +
                            "-fx-background-insets: 2px; " +
                            "-fx-background-radius: 8px;"
            );
        }
    }



    //Login Page..................................................................................................

    @FXML
    private void loginPageReturnButtonAction() {
        changeScene("homePage.fxml");
    }

    @FXML
    private void loginPageToggleButtonAction() {
        if (isSignupMode) {
            loginAccountAnchorPane.setVisible(false);
            createAccountAnchorPane.setVisible(true);
            toggleAnchorPaneLabel.setText("Already Have An Account?");
            loginPageToggleButton.setText("Login");
        } else {
            loginAccountAnchorPane.setVisible(true);
            createAccountAnchorPane.setVisible(false);
            toggleAnchorPaneLabel.setText("Don't Have An Account?");
            loginPageToggleButton.setText("Sign up");
        }
        clearTextFields();
        clearErrorMessages();
        isSignupMode = !isSignupMode;
    }

    @FXML
    private void loginPageLoginButtonAction() {
        String username = existUser.getText();
        String password = existPass.getText();
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorMassage.setText("Please enter both username and password.");
            return;
        }
        if (usernameExist(username, password)) {
            loginErrorMassage.setText("Incorrect Username or Password!");
        } else if (userExists(username, password)) {
            loginErrorMassage.setText("Login Successful!");
            navigateToLoginGamePage(username);
        } else {
            loginErrorMassage.setText("Account Does Not Exist !");
        }
        clearTextFields();
    }

    @FXML
    private void loginPageSignupButtonAction() {
        String username = createUser.getText();
        String password = createPass.getText();
        if (username.isEmpty() || password.isEmpty()) {
            createErrorMassage.setText("Please enter both username and password.");
            return;
        }
        if (usernameExist(username, password)) {
            createErrorMassage.setText("Username Already Exist ! Try Another Name.");
        } else if (userExists(username, password)) {
            createErrorMassage.setText("Account Already Exist !");
        } else {
            addUser(username, password);
            createErrorMassage.setText("Account Created Successfully!");
            navigateToLoginGamePage(username);
        }
        clearTextFields();
    }

    public void setLoggedInUsername(String username) {
        ButtonHandler.loggedInUsername = username; // Update static variable
        if (profileUsernameLabel != null) {
            profileUsernameLabel.setText(username);
        }
    }

    private void navigateToLoginGamePage(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginDashboard.fxml"));
            Scene scene = new Scene(loader.load(), 912, 624);
            ButtonHandler controller = loader.getController();
            controller.setLoggedInUsername(username); // Pass username to the next controller
            controller.setWindow(window); // Pass the window to the next controller
            if (controller.profileUsernameLabel != null) {
                controller.profileUsernameLabel.setText(username);
            }
            window.setScene(scene);
            window.setTitle("Beyond The Galaxy");
            window.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearTextFields() {
        createUser.clear();
        createPass.clear();
        existUser.clear();
        existPass.clear();
    }

    private void clearErrorMessages() {
        createErrorMassage.setText("");
        loginErrorMassage.setText("");
    }

    private boolean userExists(String username, String password) {
        File userFile = new File("user/" + username + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    return parts[0].equals(username) && parts[1].equals(password);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean usernameExist(String username, String password) {
        File userFile = new File("user/" + username + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    return parts[0].equals(username) && !parts[1].equals(password);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void addUser(String username, String password) {
        File userFile = new File("user/" + username + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile))) {
            writer.write(username + "," + password );
            writer.newLine();

            writer.write("resource,0,0,0");
            writer.newLine();
            writer.write("gems,0");
            writer.newLine();
            writer.write("weapon,0");
            writer.newLine();
            writer.write("shield,0");
            writer.newLine();
            writer.write("cardPower,0");
            writer.newLine();
            writer.write("specialcard,");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //settings........................................................................................................

    @FXML
    private void settingButtonAction() {
        settingAnchorPane.setVisible(true);
    }

    @FXML
    private void soundButtonAction() {
        MainGame.isSoundMuted = !MainGame.isSoundMuted;
        updateSoundMuteIcon();
    }

    public void updateSoundMuteIcon() {
        Platform.runLater(() -> {
            if (soundImage != null) {
                soundImage.setImage(null);
                if (MainGame.isSoundMuted) {
                    soundImage.setImage(new Image("/image/icon/mutesound.png"));
                } else {
                    soundImage.setImage(new Image("/image/icon/sound.png"));
                }
            }
        });
    }

    @FXML
    private void musicButtonAction() {
        if (MainGame.mediaPlayer != null) {
            boolean currentMuteState = MainGame.mediaPlayer.isMute();
            MainGame.mediaPlayer.setMute(!currentMuteState);
            isMusicMute = !currentMuteState;
            updateMusicMuteIcon();
        }
    }

    private void updateMusicMuteIcon() {
        Platform.runLater(() -> {
            if (musicImage != null) {
                musicImage.setImage(null);
                if (MainGame.mediaPlayer != null && MainGame.mediaPlayer.isMute()) {
                    musicImage.setImage(new Image("/image/icon/mutemusic.png"));
                } else {
                    musicImage.setImage(new Image("/image/icon/music.png"));
                }
            }
        });
    }

    @FXML
    private void settingReturnButtonAction() {
        settingAnchorPane.setVisible(false);
    }


    //Levels.........................................................................................................


    @FXML
    private void level1RightButtonAction() {
        level1AnchorPane.setVisible(false);
        level2AnchorPane.setVisible(true);
    }

    @FXML
    private void level2RightButtonAction() {
        level2AnchorPane.setVisible(false);
        level3AnchorPane.setVisible(true);
    }

    @FXML
    private void level3RightButtonAction() {
        level3AnchorPane.setVisible(false);
        level1AnchorPane.setVisible(true);
    }

    @FXML
    private void level1LeftButtonAction() {
        level1AnchorPane.setVisible(false);
        level3AnchorPane.setVisible(true);
    }

    @FXML
    private void level2LeftButtonAction() {
        level2AnchorPane.setVisible(false);
        level1AnchorPane.setVisible(true);
    }

    @FXML
    private void level3LeftButtonAction() {
        level3AnchorPane.setVisible(false);
        level2AnchorPane.setVisible(true);
    }

    @FXML
    private void level1PlayButtonAction() {
        changeScene("level1.fxml");
    }

    @FXML
    private void level2PlayButtonAction() {
        if ("Play".equals(level2PlayButton.getText())) {
            changeScene("level2.fxml");
        }
        else{
            level2Required.setVisible(true);
        }
        //  changeScene("level2.fxml");
    }

    @FXML
    private void level3PlayButtonAction() {
        if ("Play".equals(level3PlayButton.getText())) {
            changeScene("level3.fxml");
        }
        else{
            level3Required.setVisible(true);
        }
        // changeScene("level3.fxml");
    }

    @FXML
    private void levelPageReturnButtonAction() {
        changeScene("loginDashboard.fxml");
    }



    @FXML
    private AnchorPane level2Required, level3Required;

    @FXML
    private void req2BackAction(){
        level2Required.setVisible(false);
    }

    @FXML
    private void req3BackAction(){
        level3Required.setVisible(false);
    }





















    private List<String> getSpecialCardsForUser(String username) {
        List<String> cards = new ArrayList<>();
        File userFile = new File("user/" + username + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("specialcard,")) {
                        String[] parts = line.split(",");
                        for (int i = 1; i < parts.length; i++) {
                            cards.add(parts[i]);
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cards;
    }

    private void addSpecialCardButtons(List<String> cards) {
        sellAnchorPane.getChildren().clear(); // Clear existing buttons

        double x = 25;
        double y = 35; // Adjusted y to accommodate image and text
        double buttonWidth = 80; // Adjust button width as needed
        double buttonHeight = 80; // Adjust button height as needed
        double xGap = 35;
        double yGap = 40; // Adjust yGap for spacing

        for (String card : cards) {
            // Create ImageView with if-else ladder for card image selection
            String imagePath;
            if (card.equals("1")) {
                imagePath = "image/icon/card1.png";
            } else if (card.equals("2")) {
                imagePath = "image/icon/card2.png";
            } else if (card.equals("3")) {
                imagePath = "image/icon/card3.png";
            } else if (card.equals("4")) {
                imagePath = "image/icon/card4.png";
            } else if (card.equals("5")) {
                imagePath = "image/icon/card5.png";
            } else if (card.equals("6")) {
                imagePath = "image/icon/card6.png";
            } else {
                // Default image or error handling if card value is invalid
                imagePath = "image/icon/defaultCard.png"; // Provide a default image
                System.err.println("Invalid card value: " + card);
            }

            ImageView imageView = new ImageView(new Image(imagePath));
            imageView.setFitWidth(50); // Adjust image width
            imageView.setFitHeight(50); // Adjust image height
            imageView.setPreserveRatio(true);

            // Create Label (optional, if you want to keep the "Card X" text)
            Label label = new Label("Card " + card); // Keep the text if needed
            label.setTextFill(javafx.scene.paint.Color.WHITE);

            // Create VBox to hold ImageView and Label
            VBox buttonContent = new VBox(5); // Spacing between image and text
            buttonContent.setAlignment(javafx.geometry.Pos.CENTER);
            buttonContent.getChildren().addAll(imageView, label);

            // Create Button
            Button button = new Button();
            button.setId(card); // Set the card number as the button's ID
            button.setLayoutX(x);
            button.setLayoutY(y);
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setGraphic(buttonContent); // Set VBox as button graphic
            button.setOnAction(e -> handleCardButtonClicked(card));
            button.setStyle("-fx-background-color: #315666;");
            sellAnchorPane.getChildren().add(button);

            // Update x and y for the next button
            x += buttonWidth + xGap;
            if (x + buttonWidth > sellAnchorPane.getWidth()) {
                x = 25;
                y += buttonHeight + yGap;
            }
        }
    }

    private void handleCardButtonClicked(String card) {
        // Show the setPriceAnchorPane
        setPriceAnchorPane.setVisible(true);
        sellAnchorPane.setVisible(false);

        // Set the action for the "Set on Sale" button
        sellItemButton.setOnAction(e -> {
            String priceString = setPriceTextField.getText();
            try {
                int price = Integer.parseInt(priceString);
                if (shopClient != null) {
                    shopClient.sellItem(card, priceString);
                }

                // Remove the card from the user's file
                removeCardFromFile(loggedInUsername, card);

                // Update the UI
                setPriceAnchorPane.setVisible(false);
                shopStore.setVisible(true);

                // Show a confirmation dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Item on Sale");
                alert.setHeaderText(null);
                alert.setContentText("Your item (Card " + card + ") is now on sale for " + price + " gems.");
                alert.showAndWait();

            } catch (NumberFormatException ex) {
                // Handle invalid price input
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Price");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid price.");
                alert.showAndWait();
            }
        });
    }

    private void removeCardFromFile(String username, String cardToRemove) {
        File userFile = new File("user/" + username + ".txt");
        List<String> lines = new ArrayList<>();

        // Read all lines from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return; // Exit if there's an error reading the file
        }

        // Modify the specialcard line
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("specialcard,")) {
                String[] parts = lines.get(i).split(",");
                StringBuilder newCardLine = new StringBuilder("specialcard");
                for (int j = 1; j < parts.length; j++) {
                    if (!parts[j].equals(cardToRemove)) {
                        newCardLine.append(",").append(parts[j]);
                    }
                }
                lines.set(i, newCardLine.toString());
                break;
            }
        }

        // Write the modified lines back to the file
        try (PrintWriter writer = new PrintWriter(userFile)) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateShopItemList(String message) {
        System.out.println("Received message: " + message);
        if (message.startsWith("ITEM_LIST:")) {
            onSaleItems.clear();
            String[] items = message.substring("ITEM_LIST:".length()).split(":");
            if (items.length % 3 == 0) {
                for (int i = 0; i < items.length; i += 3) {
                    String itemId = items[i];
                    String sellerName = items[i + 1];
                    String price = items[i + 2];
                    onSaleItems.put(itemId, sellerName + ":" + price);
                }
                refreshShopItemList();
            } else {
                System.out.println("Invalid item list format.");
            }
        } else if (message.startsWith("ITEM_SOLD:") || message.startsWith("ITEM_ADDED:")) {
            shopClient.requestItemListUpdate(); // Request an update from the server
        } else if (message.startsWith("BOUGHT:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String itemId = parts[1];
                String sellerName = parts[2].split(":")[0];
                String price = parts[2].split(":")[1];
                // Add the card to the user's file
                addCardToFile(loggedInUsername, itemId);

                // Update gems for buyer (deduct gems)
                updateGems(loggedInUsername, -Integer.parseInt(price));

                // Update gems for seller (add gems)
                updateGems(sellerName, Integer.parseInt(price));

                // Show a confirmation dialog
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Purchase Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("You have bought Card " + itemId + " for " + price + " gems.");
                    alert.showAndWait();
                });
            }
        } else if (message.startsWith("SOLD:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String itemId = parts[1];
                String buyerName = parts[2];

                // Show a notification to the seller
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Item Sold");
                    alert.setHeaderText(null);
                    alert.setContentText("Your Card " + itemId + " has been sold to " + buyerName + ".");
                    alert.showAndWait();
                });
            }
        } else if (message.equals("ITEM_LIST_EMPTY")) {
            // Clear the item list if the server indicates it's empty
            itemGridPane.getChildren().clear();
        }
    }

    private void addCardToFile(String username, String cardToAdd) {
        File userFile = new File("user/" + username + ".txt");
        List<String> lines = new ArrayList<>();
        boolean cardLineExists = false;

        // Read all lines from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("specialcard,")) {
                    line += "," + cardToAdd; // Append the new card
                    cardLineExists = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Add a new specialcard line if it doesn't exist
        if (!cardLineExists) {
            lines.add("specialcard," + cardToAdd);
        }

        // Write the modified lines back to the file
        try (PrintWriter writer = new PrintWriter(userFile)) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateGems(String username, int gemChange) {
        File userFile = new File("user/" + username + ".txt");
        List<String> lines = new ArrayList<>();
        int currentGems=0;

        // Read all lines from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("gems,")) {
                    currentGems = Integer.parseInt(line.split(",")[1]);
                    line = "gems," + (currentGems + gemChange); // Update gem count
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Write the modified lines back to the file
        try (PrintWriter writer = new PrintWriter(userFile)) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update the gem label if the user is the logged-in user
        if (username.equals(loggedInUsername)) {
            Platform.runLater(() -> {
                updateShopGemsLabel();
                updateGemLabel();

            });
        }
    }



    private void refreshShopItemList() {
        itemGridPane.getChildren().clear(); // Clear existing items

        int row = 0;
        int col = 0;
        for (String itemId : onSaleItems.keySet()) {
            String details = onSaleItems.get(itemId);
            String[] parts = details.split(":");
            String sellerName = parts[0];
            String price = parts[1];

            // Create ImageView for the card image
            ImageView cardImageView = new ImageView(new Image("/image/icon/card" + itemId + ".png"));
            cardImageView.setFitHeight(50); // Set the desired height
            cardImageView.setFitWidth(50); // Set the desired width
            cardImageView.setPreserveRatio(true);

            // Create a label for the text
            Label itemLabel = new Label("Seller: " + sellerName + "\nPrice: " + price);
            itemLabel.setTextFill(javafx.scene.paint.Color.WHITE); // Set text color to white

            // Create a VBox to hold the ImageView and Label
            VBox buttonContent = new VBox(5); // 5 is the spacing between elements
            buttonContent.setAlignment(javafx.geometry.Pos.CENTER);
            buttonContent.getChildren().addAll(cardImageView, itemLabel);

            // Create a button and set its graphic to the VBox
            Button itemButton = new Button();
            itemButton.setId("item-" + itemId);
            itemButton.setPrefSize(100, 100); // Adjust size as needed
            itemButton.setStyle("-fx-background-color: #315666; -fx-border-color: #FFFFFF; -fx-border-radius: 8px; -fx-border-width: 2px; -fx-background-radius: 8px");
            itemButton.setGraphic(buttonContent);
            itemButton.setOnAction(e -> handleBuyItem(itemId, sellerName, price));

            itemGridPane.add(itemButton, col, row);
            col++;
            if (col == 4) { // 4 items per row
                col = 0;
                row++;
            }
        }
    }

    private void handleBuyItem(String itemId, String sellerName, String price) {
        if (loggedInUsername.equals(sellerName)) {
            showAlert("You cannot buy your own item.");
            return;
        }

        // Check if the user has enough gems
        int userGems = getUserGems(loggedInUsername);
        if (userGems < Integer.parseInt(price)) {
            // User does not have enough gems
            showAlert("You do not have enough gems to buy this item.");
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText("Buy Card " + itemId);
        alert.setContentText("Are you sure you want to buy this item for " + price + " gems?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (shopClient != null) {
                shopClient.buyItem(itemId);
            }
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private int getUserGems(String username) {
        File userFile = new File("user/" + username + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        return Integer.parseInt(line.split(",")[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0; // Default to 0 if no gems found or error occurs
    }


    private void updateShopGemsLabel() {
        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        int gems = Integer.parseInt(line.split(",")[1]);
                        Platform.runLater(() -> shopGemsLabel.setText(String.valueOf(gems)));
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGemLabel() {
        File userFile = new File("user/" + loggedInUsername + ".txt");
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("gems,")) {
                        int gems = Integer.parseInt(line.split(",")[1]);
                        Platform.runLater(() -> gemLabel.setText(String.valueOf(gems)));
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}