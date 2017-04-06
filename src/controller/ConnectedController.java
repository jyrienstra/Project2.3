package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.server_connection.GameReader;
import model.server_connection.PreGameReader;
import model.server_connection.TelnetWriter;

import java.io.IOException;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.ResourceBundle;

public class ConnectedController extends PreGameView implements Initializable {
    TelnetWriter connectionWriter;
    @FXML RadioButton bke;
    @FXML RadioButton reversi;
    @FXML ComboBox opponentSelection;
    @FXML TextArea logArea;
    @FXML ToggleGroup test;
    @FXML Label loggedInAs;
    private String selectedOpponent;
    boolean succesfull = false;
    String command = "no command yet"; //@todo create command class
    String playerName = null;

    public void clickButton(){
        System.out.println("test");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> {
            getOpponents();
        });
        updateOpponentSelection();
    }

    @Override
    public void setSuccesfull(boolean status) {
        succesfull = status;
        System.out.println("ConnectedController: Last executed command was succesfull");
        opponentSelection.getItems().add("test");
        updateOpponentSelection();
    }

    public void setPlayerName(String playerName){
        this.playerName = playerName;
        loggedInAs.setText("Logged in as: " + playerName);
    }

    @Override
    public void setConnectionWriter(TelnetWriter w) {
        connectionWriter = w;
    }

    @Override
    public void startGame(String game, String playerToMove) {
        System.out.println("startgame wel aangeroepen?");
        //change view and start game controller of certain game
        Platform.runLater(() -> {
            String gridtoGame = "tictactoeGrid";
            if(game.equals("Reversi")){gridtoGame = "othelloGrid";}
            System.out.println(gridtoGame);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/"+ gridtoGame+ ".fxml"));
            Parent root = null;
            try {
                root = (Parent) fxmlLoader.load();

                //Set writer in controller
                TictactoeController tictactoeController= fxmlLoader.getController();
                tictactoeController.setConnectionWriter(connectionWriter);

                //Create a game telnet reader @todo dirty please think of something cleaner
                GameReader gameReader = new GameReader(super.getSocket());
                Thread t1 = new Thread(gameReader);
                t1.start();
                tictactoeController.setConnectionReader(gameReader);


                //Notify if we have to start or opponent is starting with a move, after this the gamereader will handle everything
                if(playerToMove.equals(this.playerName)){
                    tictactoeController.ourturn();
                }
                // Remove this view from the views that get notified on updates from the reader
                connectionReader.removeView(this);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConcurrentModificationException cme) {
                System.out.println("Test");
                cme.printStackTrace();
            }
            stage.setTitle("Our playername = " + playerName);
            stage.setScene(new Scene(root, 800, 800));
        });
    }


    public void logout(){
        System.out.println("Logged out of the server");
        super.updateLog("logout");
        connectionWriter.sendData("logout");
    }

    public void updateLog(String currentLine){
        super.updateLog(currentLine);
        logArea.clear();
        logArea.appendText(super.getLog());
    }

    /**
     * Get all possible commands from server
     */
    public void help(){
        super.updateLog("help");
        connectionWriter.sendData("help");
    }

    /**
     * Subscribe to a game
     * Usage Subscribe <game> @ server
     */
    public void subscribe(){
        String game = "";
        if(reversi.isSelected()){
            game = "Reversi";
        }else{
            game = "Tic-tac-toe";
        }

        //Options server-sided are "Reversi" and "Tic-tac-toe"
        super.updateLog("subscribe " + game);
        connectionWriter.sendData("subscribe " + game);
    }

    public void getOpponents(){
        connectionWriter.sendData("get playerlist"); //ask for a playerlist

    }

    private void updateOpponentSelection(){
        if(super.getPlayerList()!=null){
//            opponentSelection.getItems().clear();
            for(String player: super.getPlayerList()){
                opponentSelection.getItems().add(player);
            }
            System.out.println(super.getPlayerList().size());
        }
    }

    public void getOpponentIndividual() {
        selectedOpponent = opponentSelection.getSelectionModel().getSelectedItem().toString();
    }

    public void invPlayer() {
        String selectedGame = "";
        if (bke.isSelected()) {
            selectedGame = "Tic-tac-toe";
        } else if (reversi.isSelected()) {
            selectedGame = "Reversi";
        }
        connectionWriter.sendData("challenge " + "\"" + selectedOpponent.replace(" ", "") +"\""  + " " + "\"" + selectedGame + "\"");
    }

    public TelnetWriter getConnectionWriter() {
        return connectionWriter;
    }

}
