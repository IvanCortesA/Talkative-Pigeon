/*  
this class contains the section of chat screen where messages(message bubbles)
of the active conversation will be seen.
*/

//required imports
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChatPanel extends BorderPane {

    private final VBox messageBox = new VBox(8);
    private final Label nameLabel = new Label("Select a conversation");
    private Conversation current;

    //This method builds the panel with the available elements
    public ChatPanel() {
        // Set up the header that changes to the name of the other user
        nameLabel.getStyleClass().add("chat-name");
        HBox header = new HBox(nameLabel);
        header.getStyleClass().add("chat-header");
        setTop(header);

        //scroll bar
        messageBox.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(messageBox);
        scroll.setFitToWidth(true);
        setCenter(scroll);
        messageBox.heightProperty().addListener((obs, old, h) -> scroll.setVvalue(1.0));

        // Set up the input bar with ghost prompt
        TextField input = new TextField();
        input.setPromptText("Type a message...");
        input.getStyleClass().add("message-input");
        HBox.setHgrow(input, Priority.ALWAYS);

        //send button code
        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        sendButton.setOnAction(e -> {
            if (current == null) return;
            String text = input.getText().trim();
            if (text.isEmpty()) return;
            current.addMessage(new Message(text, true));
            render();
            input.clear();
        });
        input.setOnAction(e -> sendButton.fire()); //send button action

        HBox inputBar = new HBox(10, input, sendButton);
         input.getStyleClass().add("message-input");
        setBottom(inputBar);
    }

    public void showConversation(Conversation c) {
        current = c;
        nameLabel.setText(c.getname());
        render();
    }

    private void render() { //gather children to build a conversation window inside the conversation panel
        messageBox.getChildren().clear();
        for (Message m : current.getMessages()) {
            messageBox.getChildren().add(bubble(m.getText(), m.isMine()));
        }
    }
    //bubble method that builds the chat bubbles from ChatStyles.css abd the string passed as a parameter
    private HBox bubble(String text, boolean mine) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.getStyleClass().add(mine ? "bubble-mine" : "bubble-theirs");
        HBox row = new HBox(label);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
