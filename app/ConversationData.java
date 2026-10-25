/*  
This class holds the name of a the conversation and temporarily holds the message sent by the ither user

*/
import java.util.ArrayList;
import java.util.List;

public class ConversationData{

    public static List<Conversation> getSampleConversations(){
        List<Conversation> conversations = new ArrayList<>();

        conversations.add(new Conversation("Ivan", "Hey, what's up?"));
        conversations.add(new Conversation("Andy", "See you tomorrow!"));
        conversations.add(new Conversation("Sony", "Did you finish the project?"));
        
        return conversations;
    }
}