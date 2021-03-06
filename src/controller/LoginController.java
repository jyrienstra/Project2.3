package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.server_connection.ServerHandler;
import model.server_connection.ServerHandlerReader;
import model.server_connection.ServerHandlerWriter;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Map;

public class LoginController extends PreGameView {
    @FXML
    TextField textField;

    boolean succesfull = false;

    public void loginButton() {
        //Get username
        String playerName = textField.getText();

        //Login
        ServerHandlerWriter.login(playerName);
        ServerHandler.playerName = playerName;
    }

    @Override
    public void startGame(String game, String playerToMove) {}

    @Override
    public void useBot(boolean status) {
        ServerHandlerReader.useBot=status;
    }

    public void login() {
        //Perform this in the javafx thread
        Platform.runLater(() -> {
            Stage stage = super.getStage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/connectedView.fxml"));
            Parent root = null;
            try {
                root = (Parent) fxmlLoader.load();

                //Set writer in controller
                ConnectedController connectedController = fxmlLoader.getController();
                ServerHandlerReader.currentController = connectedController;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConcurrentModificationException cme) {
                cme.printStackTrace();
            }
            stage.setTitle("Connected");
            Scene scene = new Scene(root, 300, 400);
            scene.getStylesheets().add("stylesheet.css");
            stage.setScene(scene);
        });
    }
}
