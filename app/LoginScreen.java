/*

Class for the login screen of the Connect Messaging App

*/

//required imports
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
 

public class LoginScreen extends HBox {
    
    public LoginScreen(Runnable onSignIn){ //login screen constructor, the parameter is the next page that will be loaded when the user signs in
            StackPane formPane = new StackPane(buildForm(onSignIn));
            formPane.getStyleClass().add("login-pane");
            HBox.setHgrow(formPane, Priority.ALWAYS);
            getChildren().addAll(buildBrandPanel(), formPane);// attach chilfren to scene
    }
    private VBox buildBrandPanel(){  
        //this is a side panel with the app name and logo
        
        Label name = new Label("Talkative-Pigeon");
        name.getStyleClass().add("brand-name");

        Label tagline = new Label("A desktop messenger for your team.");
        tagline.getStyleClass().add("brand-tagline");
        tagline.setWrapText(true);
        
        Region top = new Region();
        Region bottom = new Region();
        
        VBox.setVgrow(top, Priority.ALWAYS);
        VBox.setVgrow(bottom, Priority.ALWAYS);
        VBox panel = new VBox(name, top, bottom, tagline);
        
        panel.getStyleClass().add("login-brand");

        panel.setPrefWidth (440);
        panel.setMinWidth(440);
        panel.setMaxWidth(440);
        return panel;

    }
    private VBox buildForm(Runnable onSignIn) { // thsi panel conatins the login form
        
        Label heading = new Label("Welcome back");
        heading.getStyleClass().add("heading");

        Label sub = new Label("Sign in to continue to your conversations.");
        sub.getStyleClass().add("subtle");
        sub.setWrapText(true);
        
        TextField username = new TextField();
        username.setPromptText("Enter your username"); //ghost prompt for username
 
        PasswordField password = new PasswordField();
        password.setPromptText("Enter your password"); //ghost prompt for password
 
        Button signIn = new Button("Sign in"); //sign in button
        signIn.getStyleClass().add("primary-button");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setDefaultButton(true);
        
        signIn.setOnAction(e -> onSignIn.run()); //sigin button action
 
        /* vbox container fo password and username fields */
        VBox form = new VBox(20,
                new VBox(8, heading, sub),
                labelled("Username", username),
                labelled("Password", password),
                signIn);
            

        form.setMaxWidth(360);
        form.setMaxHeight(Region.USE_PREF_SIZE); //set the max height to the preferred size of the form
        return form; //return the form panel 
    }


    private VBox labelled(String text, Control field) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(8, label, field);
    }
}
