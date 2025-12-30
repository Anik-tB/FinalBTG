package com.example.finalbtg;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;


public class Resource {


    public int gemsCollected;
    public int engineCollected;
    public  int communicationCollected;
    public  int controllerCollected;
    public  int weaponPower;
    public  int shieldPower;
    public int cardPower;
    public  void   initializeResources(AnchorPane resourceCollectionAnchorPane,Label engineLabelCount,Label communicationLabelCount,Label controllerLabelCount,Label gemsLabelCount,Label weaponPoweLabel,Label shieldPowerLabel,Label SpecialCardPowerLabel,Label resourceNotFoundLabel,ImageView specialCardImage) {
        ButtonHandler buttonHandler = new ButtonHandler();
        String user = buttonHandler.getLoggedInUsername();

        // Make the AnchorPane visible
        resourceCollectionAnchorPane.setVisible(false);
        Random random = new Random();



        // Generate a random number of gems
        int randomGems = 0 + random.nextInt(50);
        gemsLabelCount.setText(String.valueOf(randomGems));
        gemsCollected = randomGems;

        //Generate random number of engine
        int randomEngine = 0 + random.nextInt(3);
        engineLabelCount.setText(String.valueOf(randomEngine));
        engineCollected = randomEngine;

        //Generate random number of communication
        int randomCommunication = 0 + random.nextInt(3);
        communicationLabelCount.setText(String.valueOf(randomCommunication));
        communicationCollected = randomCommunication;

        //Generate random number of controller
        int randomController = 0 + random.nextInt(3);
        controllerLabelCount.setText(String.valueOf(randomController));
        controllerCollected = randomController;

        //Generate random number of weaponPower
        int randomWeaponpower=1 + random.nextInt(10);
        if(randomWeaponpower==1){  weaponPower=150;}
        else if(randomWeaponpower==2){ weaponPower=200;  }
        else if(randomWeaponpower==3){ weaponPower=250;}
        else if(randomWeaponpower==4){  weaponPower=300; }
        else if(randomWeaponpower==5){ weaponPower=350;}
        else if(randomWeaponpower==6){ weaponPower=400;}
        else if(randomWeaponpower==7){ weaponPower=450;}
        else if(randomWeaponpower==8){weaponPower=500; }
        else if(randomWeaponpower==9){ weaponPower=550;}
        else { weaponPower=600;}
        File userFile = new File("user/" + user + ".txt");
        if (userFile.exists()) {
            // Create a temporary file
            File tempFile = new File("user/temp_" + user + ".txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(userFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                boolean weaponUpdated = false; // Flag to track if weapon power is updated

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2 && parts[0].equals("weapon") && !weaponUpdated) {
                        int weapons = Integer.parseInt(parts[1]);
                        if (weapons < weaponPower) {
                            weaponPoweLabel.setText(String.valueOf(weaponPower));
                            writer.write(parts[0] + "," + weaponPower);
                            weaponUpdated = true; // Set the flag after updating
                        } else {
                            // Write the original weapon power if it's not less
                            writer.write(line);
                        }
                    } else {
                        // Write other lines unchanged
                        writer.write(line);
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Replace the original file with the temporary file
            if (userFile.delete() && tempFile.renameTo(userFile)) {
                System.out.println("Weapon power updated successfully!");
            } else {
                System.err.println("Error updating weapon power!");
            }

        }
        //Generate random number of shield
        int randomShield=1  + random.nextInt(10);
        if(randomShield==1){  shieldPower=1000;}
        else if(randomShield==2){ shieldPower=1050;  }
        else if(randomShield==3){ shieldPower=1100;}
        else if(randomShield==4){  shieldPower=1150; }
        else if(randomShield==5){ shieldPower=1200;}
        else if(randomShield==6){ shieldPower=1250;}
        else if(randomShield==7){ shieldPower=1300;}
        else if(randomShield==8){shieldPower=1350; }
        else if(randomShield==9){ shieldPower=1400;}
        else { shieldPower=1450;}

        if (userFile.exists()) {
            // Create a temporary file
            File tempFile = new File("user/temp_" + user + ".txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(userFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                boolean ShieldUpdated = false; // Flag to track if weapon power is updated

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2 && parts[0].equals("shield") && !ShieldUpdated) {
                        int shields = Integer.parseInt(parts[1]);
                        if (shields < shieldPower) {
                            shieldPowerLabel.setText(String.valueOf(shieldPower));
                            writer.write(parts[0] + "," + shieldPower);
                            ShieldUpdated = true; // Set the flag after updating
                        } else {
                            // Write the original weapon power if it's not less
                            writer.write(line);
                        }
                    } else {
                        // Write other lines unchanged
                        writer.write(line);
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Replace the original file with the temporary file
            if (userFile.delete() && tempFile.renameTo(userFile)) {
                System.out.println("Weapon power updated successfully!");
            } else {
                System.err.println("Error updating weapon power!");
            }
        }


        //Generate random number of card
        int randomCard=0  + random.nextInt(7);

        if(randomCard==1){ cardPower=1000;   }
        else if (randomCard==2) {cardPower=1100; }
        else if(randomCard==3){ cardPower=1200;   }
        else if(randomCard==4){ cardPower=1300;   }
        else if(randomCard==5){ cardPower=1400;   }
        else if(randomCard==6){ cardPower=1500;   }

        if (userFile.exists()) {
            // Create a temporary file
            File tempFile = new File("user/temp_" + user + ".txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(userFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                boolean cardPowerUpdated = false; // Flag to track if weapon power is updated

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if ( parts[0].equals("cardPower") && !cardPowerUpdated) {
                        int cardspower = Integer.parseInt(parts[1]);
                        if (cardspower < cardPower) {
                            writer.write(parts[0] + "," + cardPower);
                            cardPowerUpdated = true; // Set the flag after updating
                        } else {
                            // Write the original weapon power if it's not less
                            writer.write(line);
                        }
                    } else {

                        writer.write(line);
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Replace the original file with the temporary file
            if (userFile.delete() && tempFile.renameTo(userFile)) {
                System.out.println("Weapon power updated successfully!");
            } else {
                System.err.println("Error updating weapon power!");
            }


        }



        if(randomCard==1||randomCard==2||randomCard==3||randomCard==4||randomCard==5||randomCard==6){

            if (userFile.exists()) {
                // Create a temporary file
                File tempFile = new File("user/temp_" + user + ".txt");

                try (BufferedReader reader = new BufferedReader(new FileReader(userFile));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    String line;
                    boolean cardUpdated = false; // Flag to track if weapon power is updated

                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if ( parts[0].equals("specialcard") && !cardUpdated) {
                            boolean cardfound=false;
                            for (int i = 1; i < parts.length; i++) {

                                if(Integer.parseInt(parts[i]) == randomCard){
                                    cardfound=true;
                                    break;

                                }
                            }

                            if(!cardfound){
                                // Extract existing card numbers and add the new randomCard
                                List<Integer> cardNumbers = new ArrayList<>();
                                for (int i = 1; i < parts.length; i++) {
                                    cardNumbers.add(Integer.parseInt(parts[i]));
                                }
                                cardNumbers.add(randomCard);

                                // Sort the card numbers
                                Collections.sort(cardNumbers);

                                // Construct the updated line with sorted card numbers
                                StringBuilder newLine = new StringBuilder("specialcard");
                                for (Integer cardNumber : cardNumbers) {
                                    newLine.append(",").append(cardNumber);
                                }
                                // Write the updated line to the file
                                writer.write(newLine.toString());

                                SpecialCardPowerLabel.setText(cardPower + "");
                                Image cardImage;

                                if(randomCard==1) {
                                    cardImage = new Image(getClass().getResource("/image/icon/card1.png").toExternalForm()); // Note the leading "/"
                                } else if (randomCard==2) {
                                    cardImage=new Image(getClass().getResource("/image/icon/card2.png").toExternalForm());
                                }
                                else if (randomCard==3) {
                                    cardImage=new Image(getClass().getResource("/image/icon/card3.png").toExternalForm());
                                }
                                else if (randomCard==4) {
                                    cardImage=new Image(getClass().getResource("/image/icon/card4.png").toExternalForm());
                                }
                                else if (randomCard==5) {
                                    cardImage=new Image(getClass().getResource("/image/icon/card5.png").toExternalForm());
                                }
                                else{    cardImage=new Image(getClass().getResource("/image/icon/card6.png").toExternalForm());
                                }

                                specialCardImage.setImage(cardImage);
                                resourceNotFoundLabel.setVisible(false);
                                specialCardImage.setVisible(true);

                            }
                            else{
                                writer.write(line);
                            }
                            cardUpdated=true;

                        } else {
                            // Write other lines unchanged
                            writer.write(line);
                        }
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Replace the original file with the temporary file
                if (userFile.delete() && tempFile.renameTo(userFile)) {
                    System.out.println("Weapon power updated successfully!");
                } else {
                    System.err.println("Error updating weapon power!");
                }
            }


        }
        else {
            resourceNotFoundLabel.setVisible(true);
            specialCardImage.setVisible(false);
            //  specialCardPowerLabel.setText("");
            System.out.println("No special card found!");
        }


    }


}