/*
    This class builds the left panel that shows recent messages and who sent them
*/

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class ChatScreen extends BorderPane {

    public ChatScreen() {
        
        getStyleClass().add("chat-root");
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        
        Label appTitle = new Label("TalaktivePigeon");
        appTitle.getStyleClass().add("app-name");
        Label messagesTitle = new Label("MESSAGES");
        messagesTitle.getStyleClass().add("section-title");

        ChatPanel chatPanel = new ChatPanel();

        VBox conversations = new VBox(10);

        for (Conversation conversation : ConversationData.getSampleConversations()){
            Label conversationLabel = new Label(conversation.getname() + "\n" + conversation.getlastMessage());

            conversationLabel.setOnMouseClicked(e -> chatPanel.showConversation(conversation));
            
            conversationLabel.getStyleClass().add("contact-name");

            conversations . getChildren().add(conversationLabel);
        }
       
        sidebar.getChildren().addAll(appTitle, messagesTitle, conversations);

        setLeft(sidebar);
        setCenter(chatPanel);

    }
}   
