/*
 * Main class for the Connect Messaging App.
 * This class extends the JavaFX Application class
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application { 
    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new LoginScreen(this::showChatScreen),1000, 660);
        scene.getStylesheets().addAll( //adds the style resources into the project
            getClass().getResource("/Styles/RootStyles.css").toExternalForm(),
            getClass().getResource("/Styles/LogInStyles.css").toExternalForm(),
            getClass().getResource("/Styles/ChatStyles.css").toExternalForm()
        );

        stage.setTitle("TalaktivePigeon - Messaging App");
        stage.setMinWidth(800);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
}
    private void showChatScreen() {
        scene.setRoot(new ChatScreen());
    }
    
    public static void main(String[] args) {
        launch(args); 
    }
}
