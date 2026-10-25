/*  
this class contains the section of chat screen whre messages(message bubles) 
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

    public ChatPanel() {
        HBox header = new HBox(nameLabel);
        setTop(header);

        messageBox.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(messageBox);
        scroll.setFitToWidth(true);
        setCenter(scroll);
        messageBox.heightProperty().addListener((obs, old, h) -> scroll.setVvalue(1.0));

        TextField input = new TextField();
        input.setPromptText("Type a message...");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            if (current == null) return;
            String text = input.getText().trim();
            if (text.isEmpty()) return;
            current.addMessage(new Message(text, true));
            render();
            input.clear();
        });
        input.setOnAction(e -> sendButton.fire());

        HBox inputBar = new HBox(10, input, sendButton);
        inputBar.setPadding(new Insets(10));
        setBottom(inputBar);
    }

    public void showConversation(Conversation c) {
        current = c;
        nameLabel.setText(c.getname());
        render();
    }

    private void render() {
        messageBox.getChildren().clear();
        for (Message m : current.getMessages()) {
            messageBox.getChildren().add(bubble(m.getText(), m.isMine()));
        }
    }

    private HBox bubble(String text, boolean mine) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.setPadding(new Insets(8, 14, 8, 14));
        label.setStyle(mine
                ? "-fx-background-color:#5865f2; -fx-text-fill:white; -fx-background-radius:16;"
                : "-fx-background-color:#e4e6eb; -fx-background-radius:16;");
        HBox row = new HBox(label);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
