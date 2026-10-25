/*
    This class builds the left panel that shows recent messages and who sent them
*/

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/*
    This is the main screen after the login. It has two areas
    left side is the sidebar with the list of conversations, and you can choose for it
    center is the actual ChatPanel, which shows the selected conversation
 */
public class ChatScreen extends BorderPane {

    public ChatScreen() {
        //Sidebar container which has title and list of conversations
        getStyleClass().add("chat-root");
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        
        Label appTitle = new Label("TalkativePigeon");
        appTitle.getStyleClass().add("app-name");
        Label messagesTitle = new Label("MESSAGES");
        messagesTitle.getStyleClass().add("section-title");

        ChatPanel chatPanel = new ChatPanel();

        VBox conversations = new VBox(10);

        //One label per contact
        for (Conversation conversation : ConversationData.getSampleConversations()){
            Label conversationLabel = new Label(conversation.getname() + "\n" + conversation.getlastMessage());

            //when this label is clicked, show contact's chat in the panel.
            conversationLabel.setOnMouseClicked(e -> chatPanel.showConversation(conversation));

            conversationLabel.getStyleClass().add("contact-name");

            conversations . getChildren().add(conversationLabel);
        }
       
        sidebar.getChildren().addAll(appTitle, messagesTitle, conversations);
        setLeft(sidebar);
        setCenter(chatPanel);

    }
}   
