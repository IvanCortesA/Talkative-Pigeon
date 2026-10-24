import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class ChatScreen extends BorderPane {

    public ChatScreen() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(25));
        sidebar.setPrefWidth(260);
        
        Label appTitle = new Label("connect.");
        Label messagesTitle = new Label("MESSAGES");

        VBox conversations = new VBox(10);

        for (Conversation conversation : ConversationData.getSampleConversations()){
            Label conversationLabel = new Label(conversation.getname() + "\n" + conversation.getlastMessage());

            conversations . getChildren().add(conversationLabel);
        }
        sidebar.getChildren().addAll(appTitle, messagesTitle, conversations);

        setLeft(sidebar);
    }
}   
