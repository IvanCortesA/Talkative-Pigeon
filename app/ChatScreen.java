import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class ChatScreen extends BorderPane {

    public ChatScreen() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(25));
        sidebar.setPrefWidth(260);
        
        Label appTitle = new Label("TalaktivePigeon");
        Label messagesTitle = new Label("MESSAGES");

        ChatPanel chatPanel = new ChatPanel();

        VBox conversations = new VBox(10);

        for (Conversation conversation : ConversationData.getSampleConversations()){
            Label conversationLabel = new Label(conversation.getname() + "\n" + conversation.getlastMessage());

            conversationLabel.setOnMouseClicked(e -> chatPanel.showConversation(conversation));

            conversationLabel.setStyle("-fx-cursor: hand;");

            conversations . getChildren().add(conversationLabel);
        }
        sidebar.getChildren().addAll(appTitle, messagesTitle, conversations);

        setLeft(sidebar);
        setCenter(chatPanel);

    }
}   
