package com.example.finalbtg;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombatUpgradeHelper {
    
    // Core game state references
    private final Pane rootPane;
    private final AnchorPane gameWorld;
    private final MainCharacter player;
    private final List<loginMonster> monsters;
    private final List<Projectile> projectiles;
    
    // Equipped Card Details
    private int equippedCardId = 0; // 0 = None, 1-6 = Card ID
    private String username;
    
    // HUD Elements
    private AnchorPane cardSlotPane;
    private ImageView cardImageView;
    private Rectangle cooldownOverlay;
    private Label cooldownLabel;
    private Label promptLabel;
    
    // Active Ability States
    private boolean isShieldActive = false;
    private boolean isTimeWarpActive = false;
    private boolean isCooldownActive = false;
    private int currentCooldownRemaining = 0;
    
    // Visual overlays
    private Circle shieldCircle;
    private Rectangle timeWarpOverlay;
    
    // Timers
    private Timeline cooldownTimeline;
    private Timeline shieldTimeline;
    private Timeline timeWarpTimeline;

    public CombatUpgradeHelper(Pane rootPane, AnchorPane gameWorld, MainCharacter player, List<loginMonster> monsters, List<Projectile> projectiles) {
        this.rootPane = rootPane;
        this.gameWorld = gameWorld;
        this.player = player;
        this.monsters = monsters;
        this.projectiles = projectiles;
        this.username = ButtonHandler.loggedInUsername;
        
        loadEquippedCard();
        if (equippedCardId > 0) {
            setupActiveCardHUD();
        }
    }

    private void loadEquippedCard() {
        if (username == null || username.isEmpty()) {
            return;
        }
        File userFile = new File("user/" + username + ".txt");
        if (!userFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            int highestCard = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("specialcard,")) {
                    String[] parts = line.split(",");
                    for (int i = 1; i < parts.length; i++) {
                        try {
                            int cardNum = Integer.parseInt(parts[i].trim());
                            if (cardNum > highestCard) {
                                highestCard = cardNum;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    break;
                }
            }
            this.equippedCardId = highestCard;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupActiveCardHUD() {
        Platform.runLater(() -> {
            try {
                // Card slot container (sleek glassmorphism look)
                cardSlotPane = new AnchorPane();
                cardSlotPane.setPrefSize(80, 95);
                cardSlotPane.setLayoutX(416); // Center of the 912 width screen
                cardSlotPane.setLayoutY(518); // Close to bottom
                cardSlotPane.setStyle(
                    "-fx-background-color: rgba(1, 48, 69, 0.85); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: linear-gradient(to right, #9a00d7, #00b3ff); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 2px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 179, 255, 0.4), 10, 0, 0, 0);"
                );

                // Card Image
                String imagePath = "/image/icon/card" + equippedCardId + ".png";
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                cardImageView = new ImageView(img);
                cardImageView.setFitWidth(56);
                cardImageView.setFitHeight(56);
                cardImageView.setPreserveRatio(true);
                AnchorPane.setTopAnchor(cardImageView, 8.0);
                AnchorPane.setLeftAnchor(cardImageView, 12.0);

                // Prompt/Keybind Label
                promptLabel = new Label("[E] CAST");
                promptLabel.setStyle(
                    "-fx-text-fill: #00b3ff; " +
                    "-fx-font-family: 'System'; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold;"
                );
                promptLabel.setAlignment(Pos.CENTER);
                promptLabel.setPrefWidth(80);
                AnchorPane.setBottomAnchor(promptLabel, 6.0);

                // Cooldown overlay
                cooldownOverlay = new Rectangle(76, 91);
                cooldownOverlay.setArcWidth(28);
                cooldownOverlay.setArcHeight(28);
                cooldownOverlay.setFill(Color.color(0, 0, 0, 0.65));
                cooldownOverlay.setVisible(false);
                AnchorPane.setTopAnchor(cooldownOverlay, 2.0);
                AnchorPane.setLeftAnchor(cooldownOverlay, 2.0);

                // Cooldown countdown label
                cooldownLabel = new Label("15s");
                cooldownLabel.setStyle(
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'System'; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold;"
                );
                cooldownLabel.setAlignment(Pos.CENTER);
                cooldownLabel.setPrefSize(80, 95);
                cooldownLabel.setVisible(false);

                // Mouse interaction
                cardSlotPane.setOnMouseClicked(event -> castActiveAbility());
                cardSlotPane.setOnMouseEntered(event -> {
                    if (!isCooldownActive) {
                        cardSlotPane.setStyle(
                            "-fx-background-color: rgba(1, 60, 85, 0.95); " +
                            "-fx-background-radius: 15; " +
                            "-fx-border-color: #00b3ff; " +
                            "-fx-border-radius: 15; " +
                            "-fx-border-width: 2px; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, #9a00d7, 15, 0.5, 0, 0);"
                        );
                    }
                });
                cardSlotPane.setOnMouseExited(event -> {
                    if (!isCooldownActive) {
                        cardSlotPane.setStyle(
                            "-fx-background-color: rgba(1, 48, 69, 0.85); " +
                            "-fx-background-radius: 15; " +
                            "-fx-border-color: linear-gradient(to right, #9a00d7, #00b3ff); " +
                            "-fx-border-radius: 15; " +
                            "-fx-border-width: 2px; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0, 179, 255, 0.4), 10, 0, 0, 0);"
                        );
                    } else {
                        cardSlotPane.setStyle(
                            "-fx-background-color: rgba(1, 48, 69, 0.5); " +
                            "-fx-background-radius: 15; " +
                            "-fx-border-color: #555555; " +
                            "-fx-border-radius: 15; " +
                            "-fx-border-width: 2px;"
                        );
                    }
                });

                cardSlotPane.getChildren().addAll(cardImageView, promptLabel, cooldownOverlay, cooldownLabel);
                rootPane.getChildren().add(cardSlotPane);
            } catch (Exception e) {
                System.err.println("Failed to build active card HUD: " + e.getMessage());
            }
        });
    }

    public void castActiveAbility() {
        if (equippedCardId == 0 || isCooldownActive) {
            return;
        }
        
        isCooldownActive = true;
        promptLabel.setText("ON COOLDOWN");
        promptLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 9px; -fx-font-weight: bold;");
        cardSlotPane.setStyle(
            "-fx-background-color: rgba(1, 48, 69, 0.5); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #555555; " +
            "-fx-border-radius: 15; " +
            "-fx-border-width: 2px;"
        );
        
        cooldownOverlay.setVisible(true);
        cooldownLabel.setVisible(true);
        currentCooldownRemaining = 15; // 15 seconds cooldown
        cooldownLabel.setText(currentCooldownRemaining + "s");

        // Cooldown timer timeline
        cooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            currentCooldownRemaining--;
            if (currentCooldownRemaining <= 0) {
                cooldownOverlay.setVisible(false);
                cooldownLabel.setVisible(false);
                isCooldownActive = false;
                promptLabel.setText("[E] CAST");
                promptLabel.setStyle("-fx-text-fill: #00b3ff; -fx-font-size: 11px; -fx-font-weight: bold;");
                cardSlotPane.setStyle(
                    "-fx-background-color: rgba(1, 48, 69, 0.85); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: linear-gradient(to right, #9a00d7, #00b3ff); " +
                    "-fx-border-radius: 15; " +
                    "-fx-border-width: 2px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 179, 255, 0.4), 10, 0, 0, 0);"
                );
                triggerHUDGlowEffect();
            } else {
                cooldownLabel.setText(currentCooldownRemaining + "s");
            }
        }));
        cooldownTimeline.setCycleCount(15);
        cooldownTimeline.play();

        // Perform specific ability
        switch (equippedCardId) {
            case 1:
                activateStardustShield();
                break;
            case 2:
                activateNebulaSpreadShot();
                break;
            case 3:
                activateCosmicNovaBomb();
                break;
            case 4:
                activateChronosTimeWarp();
                break;
            case 5:
                activateVampiricDrain();
                break;
            case 6:
                activateGigaLaser();
                break;
        }
    }

    // Ability 1: Stardust Shield
    private void activateStardustShield() {
        isShieldActive = true;
        triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.CYAN, 30);
        
        Platform.runLater(() -> {
            shieldCircle = new Circle(28);
            shieldCircle.setFill(Color.TRANSPARENT);
            shieldCircle.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.CYAN), new Stop(1, Color.BLUE)));
            shieldCircle.setStrokeWidth(3);
            shieldCircle.setEffect(new javafx.scene.effect.Glow(0.8));
            
            // Pulse animation
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.5), shieldCircle);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.2); pulse.setToY(1.2);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            gameWorld.getChildren().add(shieldCircle);
            updateShieldPosition();

            // Track position timer
            shieldTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> updateShieldPosition()));
            shieldTimeline.setCycleCount(Timeline.INDEFINITE);
            shieldTimeline.play();

            // Dispel after 5 seconds
            Timeline dispel = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
                isShieldActive = false;
                shieldTimeline.stop();
                gameWorld.getChildren().remove(shieldCircle);
                triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.BLUE, 15);
            }));
            dispel.play();
        });
    }

    private void updateShieldPosition() {
        if (shieldCircle != null) {
            shieldCircle.setCenterX(player.getPosX() + 16);
            shieldCircle.setCenterY(player.getPosY() + 16);
        }
    }

    private boolean isSpreadShotActive = false;

    public boolean isSpreadShotActive() {
        return isSpreadShotActive;
    }

    // Ability 2: Nebula Spread Shot
    private void activateNebulaSpreadShot() {
        isSpreadShotActive = true;
        triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.PURPLE, 20);
        
        // Fires extra projectiles in flanking directions during next 6 seconds
        Timeline duration = new Timeline(new KeyFrame(Duration.seconds(6), event -> {
            isSpreadShotActive = false;
        }));
        duration.play();
    }

    // Ability 3: Cosmic Nova Bomb
    private void activateCosmicNovaBomb() {
        triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.ORANGE, 40);
        
        // Spawn 8 projectiles in all directions
        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT", "UP_RIGHT", "UP_LEFT", "DOWN_RIGHT", "DOWN_LEFT"};
        Platform.runLater(() -> {
            for (String dir : directions) {
                ImageView projImg = new ImageView(new Image(getClass().getResource("/image/bullet1.gif").toExternalForm()));
                projImg.setFitWidth(40);
                projImg.setFitHeight(40);
                gameWorld.getChildren().add(projImg);
                
                Projectile p = new Projectile(projImg, 22, player.getPosX(), player.getPosY(), 1);
                p.setDirection(dir);
                p.setPosition(player.getPosX(), player.getPosY());
                p.setVisible(true);
                projectiles.add(p);
            }
        });
    }

    // Ability 4: Chronos Time Warp
    private void activateChronosTimeWarp() {
        isTimeWarpActive = true;
        triggerVisualParticles(456, 312, Color.MEDIUMPURPLE, 50); // Large screen center burst
        
        Platform.runLater(() -> {
            timeWarpOverlay = new Rectangle(912, 624);
            timeWarpOverlay.setFill(Color.color(0.58, 0.44, 0.86, 0.15));
            timeWarpOverlay.setMouseTransparent(true);
            rootPane.getChildren().add(timeWarpOverlay);
            
            // Slow down monsters (sets flag)
            // Restore speed after 6 seconds
            timeWarpTimeline = new Timeline(new KeyFrame(Duration.seconds(6), event -> {
                isTimeWarpActive = false;
                rootPane.getChildren().remove(timeWarpOverlay);
                triggerVisualParticles(456, 312, Color.VIOLET, 25);
            }));
            timeWarpTimeline.play();
        });
    }

    // Ability 5: Vampiric Drain
    private void activateVampiricDrain() {
        triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.RED, 30);
        
        List<loginMonster> targets = new ArrayList<>(monsters);
        int hitCount = 0;
        
        for (loginMonster m : targets) {
            if (m.isAlive() && m.getCharacterImageView().isVisible()) {
                m.takeDamage(100); // Heavy instant damage
                triggerVisualParticles(m.getPosX() + 24, m.getPosY() + 24, Color.DARKRED, 15);
                hitCount++;
            }
        }
        
        if (hitCount > 0) {
            // Heals the player proportional to monsters hit (up to 200 HP)
            int healAmt = Math.min(200, hitCount * 40);
            player.setHealth(player.getHealth() + healAmt);
            triggerFloatingCombatText(player.getPosX() + 16, player.getPosY() - 25, "+" + healAmt + " HP", Color.LIGHTGREEN);
        }
    }

    // Ability 6: Giga Laser
    private void activateGigaLaser() {
        triggerVisualParticles(player.getPosX() + 16, player.getPosY() + 16, Color.YELLOW, 35);
        
        // Spawn 3 rapid heavy projectiles in player's current facing direction
        Platform.runLater(() -> {
            String dir = player.getDirection();
            int currentX = player.getPosX();
            int currentY = player.getPosY();
            
            for (int i = 0; i < 4; i++) {
                final int delay = i * 200; // Rapid succession delay
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(delay), event -> {
                    ImageView projImg = new ImageView(new Image(getClass().getResource("/image/bullet1.gif").toExternalForm()));
                    projImg.setFitWidth(60);
                    projImg.setFitHeight(60); // Giant laser bullet!
                    gameWorld.getChildren().add(projImg);
                    
                    Projectile p = new Projectile(projImg, 30, currentX, currentY, 1);
                    p.setDirection(dir);
                    p.setPosition(currentX, currentY);
                    p.setVisible(true);
                    projectiles.add(p);
                }));
                timeline.play();
            }
        });
    }

    // Floating text feedback helper
    private void triggerFloatingCombatText(double x, double y, String text, Color color) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            label.setStyle(
                "-fx-text-fill: " + toHexColor(color) + "; " +
                "-fx-font-family: 'Arial Black'; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: bold;"
            );
            label.setLayoutX(x);
            label.setLayoutY(y);
            gameWorld.getChildren().add(label);

            // Floating up & fading animation
            TranslateTransition floatUp = new TranslateTransition(Duration.seconds(1.2), label);
            floatUp.setByY(-50);
            
            FadeTransition fade = new FadeTransition(Duration.seconds(1.2), label);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> gameWorld.getChildren().remove(label));

            ParallelTransition pt = new ParallelTransition(floatUp, fade);
            pt.play();
        });
    }

    // Sleek particle bursts helper
    private void triggerVisualParticles(double centerX, double centerY, Color color, int count) {
        Platform.runLater(() -> {
            for (int i = 0; i < count; i++) {
                Circle particle = new Circle(Math.random() * 4 + 2, color);
                particle.setLayoutX(centerX);
                particle.setLayoutY(centerY);
                gameWorld.getChildren().add(particle);

                double angle = Math.random() * 2 * Math.PI;
                double speed = Math.random() * 120 + 40;
                double targetX = Math.cos(angle) * speed;
                double targetY = Math.sin(angle) * speed;

                TranslateTransition move = new TranslateTransition(Duration.seconds(0.8), particle);
                move.setByX(targetX);
                move.setByY(targetY);

                FadeTransition fade = new FadeTransition(Duration.seconds(0.8), particle);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(event -> gameWorld.getChildren().remove(particle));

                ParallelTransition pt = new ParallelTransition(move, fade);
                pt.play();
            }
        });
    }

    // Glow highlight for HUD slot when cooldown finishes
    private void triggerHUDGlowEffect() {
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.seconds(0.2), cardSlotPane);
            ft.setFromValue(1.0); ft.setToValue(0.3);
            ft.setAutoReverse(true);
            ft.setCycleCount(4);
            ft.setOnFinished(e -> {
                cardSlotPane.setOpacity(1.0);
                triggerVisualParticles(456, 565, Color.CYAN, 12);
            });
            ft.play();
        });
    }

    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }

    // State getters for loops to query
    public int getEquippedCardId() {
        return equippedCardId;
    }

    public boolean isShieldActive() {
        return isShieldActive;
    }

    public boolean isTimeWarpActive() {
        return isTimeWarpActive;
    }

    // Cleanup resources
    public void cleanup() {
        if (cooldownTimeline != null) cooldownTimeline.stop();
        if (shieldTimeline != null) shieldTimeline.stop();
        if (timeWarpTimeline != null) timeWarpTimeline.stop();
    }
}
