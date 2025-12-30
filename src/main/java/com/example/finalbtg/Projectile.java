package com.example.finalbtg;


import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Projectile {
    public ImageView projectileImage;
    private boolean isVisible;
    private int speed;
    public int id;// Store initial Y position
    private int posX;
    private int posY;
    public String direction; // "UP", "DOWN", "LEFT", "RIGHT"
    public int Id;

    public Projectile(ImageView projectileImage, int speed, int posX, int posY,int Id) {
        this.projectileImage = projectileImage;
        this.speed = speed;
        this.posX = posX;
        this.posY = posY;
        this.Id = Id;
        this.isVisible = false; // Initially hidden
        this.projectileImage.setVisible(false); // Initially hidden
    }

    public void setPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        updatePosition();
    }

    public void setDirection(String direction) {
        this.direction = direction;
        updateProjectileImage(direction,Id);
    }
    private void updateProjectileImage(String direction,int Id) {
        Image image = null;
        // Determine image based on projectile type (player or monster)
        switch (direction) {
            case "UP":
                if(Id ==1){
                    image = new Image(getClass().getResource("/image/projectileUp.gif").toExternalForm());
                }
                else if (Id==2) {
                    image = new Image(getClass().getResource("/image/mprojectile2.png").toExternalForm());


                } else if(Id==3){
                    image = new Image(getClass().getResource("/image/mprojectile3.png").toExternalForm());
                }
                else if(Id==4){
                    image = new Image(getClass().getResource("/image/mprojectile4.png").toExternalForm());
                } else {
                    image = new Image(getClass().getResource("/image/mprojectile5.png").toExternalForm());
                }
                break;
            case "DOWN":
                if(Id ==1){
                    image = new Image(getClass().getResource("/image/projectileDown.gif").toExternalForm());
                }
                else if (Id==2) {
                    image = new Image(getClass().getResource("/image/mprojectile2.png").toExternalForm());
                }
                else if(Id==3){
                    image = new Image(getClass().getResource("/image/mprojectile3.png").toExternalForm());
                }
                else if(Id==4){
                    image = new Image(getClass().getResource("/image/mprojectile4.png").toExternalForm());
                }
                else{
                    image = new Image(getClass().getResource("/image/mprojectile5.png").toExternalForm());
                }
                break;
            case "LEFT":
                if(Id ==1){
                    image = new Image(getClass().getResource("/image/projectileLeft.gif").toExternalForm());
                }
                else if (Id==2) {
                    image = new Image(getClass().getResource("/image/mprojectile2.png").toExternalForm());
                }
                else if(Id==3){
                    image = new Image(getClass().getResource("/image/mprojectile3.png").toExternalForm());
                }
                else if(Id==4){
                    image = new Image(getClass().getResource("/image/mprojectile4.png").toExternalForm());
                }
                else{
                    image = new Image(getClass().getResource("/image/mprojectile5.png").toExternalForm());
                }
                break;

            case "RIGHT":
                if(Id ==1){
                    image = new Image(getClass().getResource("/image/projectileRight.gif").toExternalForm());
                }
                else if (Id==2) {
                    image = new Image(getClass().getResource("/image/mprojectile2.png").toExternalForm());
                }
                else if(Id==3){
                    image = new Image(getClass().getResource("/image/mprojectile3.png").toExternalForm());
                }
                else if(Id==4){
                    image = new Image(getClass().getResource("/image/mprojectile4.png").toExternalForm());
                }
                else{
                    image = new Image(getClass().getResource("/image/mprojectile5.png").toExternalForm());
                }
                break;
        }
        if (image != null) {
            final Image finalImage = image; // Create a final variable
            Platform.runLater(() -> projectileImage.setImage(finalImage));
        }
    }


    public void move() {
        if (Id == 1) {  // Player projectile
            switch (direction) {
                case "UP":
                    posY -= speed;
                    break;
                case "DOWN":
                    posY += speed;
                    break;
                case "LEFT":
                    posX -= speed;
                    break;
                case "RIGHT":
                    posX += speed;
                    break;
            }
        }
        else if(Id==2){

            switch (direction) {
                case "UP":
                    posY -= speed;
                    break;
                case "DOWN":
                    posY += speed;
                    break;
                case "LEFT":
                    posX -= speed;
                    break;
                case "RIGHT":
                    posX += speed;
                    break;
                case "UP_RIGHT":
                    posY -= speed * 0.7071;
                    posX += speed * 0.7071;
                    break;
                case "UP_LEFT":
                    posY -= speed * 0.7071;
                    posX -= speed * 0.7071;
                    break;
                case "DOWN_LEFT":
                    posY += speed * 0.7071;
                    posX -= speed * 0.7071;
                    break;
                case "DOWN_RIGHT":
                    posY += speed * 0.7071;
                    posX += speed * 0.7071;
                    break;
                case "UP_LEFT_LEFT":
                    posY -= speed * 0.3827; // Approx. sin(22.5)
                    posX -= speed * 0.9239; // Approx. cos(22.5)
                    break;
                case "UP_LEFT_UP":
                    posY -= speed * 0.9239;
                    posX -= speed * 0.3827;
                    break;
                case "UP_RIGHT_UP":
                    posY -= speed * 0.9239;
                    posX += speed * 0.3827;
                    break;
                case "UP_RIGHT_RIGHT":
                    posY -= speed * 0.3827;
                    posX += speed * 0.9239;
                    break;
                case "DOWN_RIGHT_RIGHT":
                    posY += speed * 0.3827;
                    posX += speed * 0.9239;
                    break;
                case "DOWN_RIGHT_DOWN":
                    posY += speed * 0.9239;
                    posX += speed * 0.3827;
                    break;
                case "DOWN_LEFT_DOWN":
                    posY += speed * 0.9239;
                    posX -= speed * 0.3827;
                    break;
                case "DOWN_LEFT_LEFT":
                    posY += speed * 0.3827;
                    posX -= speed * 0.9239;
                    break;
            }
        }
        else {
            // Calculate diagonal movement based on direction
            switch (direction) {
                case "UP":
                    posY -= speed;
                    break;
                case "DOWN":
                    posY += speed;
                    break;
                case "LEFT":
                    posX -= speed;
                    break;
                case "RIGHT":
                    posX += speed;
                    break;
                case "UP_RIGHT":
                    posY -= speed * 0.7071;
                    posX += speed * 0.7071;
                    break;
                case "UP_LEFT":
                    posY -= speed * 0.7071;
                    posX -= speed * 0.7071;
                    break;
                case "DOWN_LEFT":
                    posY += speed * 0.7071;
                    posX -= speed * 0.7071;
                    break;
                case "DOWN_RIGHT":
                    posY += speed * 0.7071;
                    posX += speed * 0.7071;
                    break;
                case "UP_LEFT_LEFT":
                    posY -= speed * 0.3827; // Approx. sin(22.5)
                    posX -= speed * 0.9239; // Approx. cos(22.5)
                    break;
                case "UP_LEFT_UP":
                    posY -= speed * 0.9239;
                    posX -= speed * 0.3827;
                    break;
                case "UP_RIGHT_UP":
                    posY -= speed * 0.9239;
                    posX += speed * 0.3827;
                    break;
                case "UP_RIGHT_RIGHT":
                    posY -= speed * 0.3827;
                    posX += speed * 0.9239;
                    break;
                case "DOWN_RIGHT_RIGHT":
                    posY += speed * 0.3827;
                    posX += speed * 0.9239;
                    break;
                case "DOWN_RIGHT_DOWN":
                    posY += speed * 0.9239;
                    posX += speed * 0.3827;
                    break;
                case "DOWN_LEFT_DOWN":
                    posY += speed * 0.9239;
                    posX -= speed * 0.3827;
                    break;
                case "DOWN_LEFT_LEFT":
                    posY += speed * 0.3827;
                    posX -= speed * 0.9239;
                    break;
            }
        }
        updatePosition();
    }

    public void updatePosition() {
        Platform.runLater(() -> {
            projectileImage.setLayoutX(posX);
            projectileImage.setLayoutY(posY);
        });
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
        Platform.runLater(() -> projectileImage.setVisible(visible));
    }

    public boolean isVisible() {
        return isVisible;
    }

//    public void handleCollision(Monsters monster) {
//        if (projectileImage.getBoundsInParent().intersects(monster.getCharacterImageView().getBoundsInParent())) {
//            monster.takeDamage(10); // Example damage
//            setVisible(false); // Hide projectile after hit
//        }
//    }
}