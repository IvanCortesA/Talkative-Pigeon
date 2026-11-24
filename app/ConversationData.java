/*  
This class holds the name of a the conversation and temporarily holds the message sent by the ither user

*/
/* 


// This is an example of a method that will return the previous message sent by the other user.
    public static List<Conversation> getSampleConversations(){
        List<Conversation> conversations = new ArrayList<>();

        conversations.add(new Conversation("Ivan", "Hey, what's up?"));
        conversations.add(new Conversation("Andy", "See you tomorrow!"));
        conversations.add(new Conversation("Sony", "Did you finish the project?"));
        
        return conversations;
    }
}
*/

/*
This class gives the chat screen its conversations. The conversations and their messages
are loaded from the SQLite database through StoreToDB.

There is no real login yet, so the signed-in user is a sample user called "Me", and the
first time the app runs it creates a few sample contacts and one conversation with each.
After that first run the data already exists, so nothing is created twice.
*/

import java.util.ArrayList;
import java.util.List;

import database.StoreToDB;

public class ConversationData{

    // The sample contacts and the message each one starts the conversation with.
    private static final String[][] CONTACTS = {
        {"Ivan", "Hey, what's up?"},
        {"Andy", "See you tomorrow!"},
        {"Sony", "Did you finish the project?"}
    };

    private static int currentUserId = -1; // user_id of the signed-in user, set by getSampleConversations()

    // The user_id of the person using the app. ChatPanel uses it as the sender of the messages we send.
    public static int getCurrentUserId(){
        return currentUserId;
    }

    // Loads the conversation list (and each conversation's messages) from the database.
    public static List<Conversation> getSampleConversations(){
        List<Conversation> conversations = new ArrayList<>();

        currentUserId = findOrCreateUser("sample_me", "Me");
        if (currentUserId < 0){
            System.out.println("Could not load the signed-in user from the database.");
            return conversations;
        }

        addMissingSampleConversations();

        for (StoreToDB.ConversationRow row : StoreToDB.getConversations(currentUserId)){
            Conversation conversation = new Conversation(row.id(), row.name());

            for (StoreToDB.MessageRow m : StoreToDB.getMessages(row.id())){
                boolean mine = (m.senderId() == currentUserId);   // "mine" draws the purple bubble on the right
                conversation.addMessage(new Message(m.text(), mine));
            }
            conversations.add(conversation);
        }
        return conversations;
    }

    // Creates the sample contacts and their conversations, but only the ones that are not in the database yet.
    private static void addMissingSampleConversations(){
        List<StoreToDB.ConversationRow> existing = StoreToDB.getConversations(currentUserId);

        for (String[] contact : CONTACTS){
            String name = contact[0];
            String firstMessage = contact[1];

            boolean alreadyThere = false;
            for (StoreToDB.ConversationRow row : existing){
                if (row.name().equals(name)){
                    alreadyThere = true;
                }
            }
            if (alreadyThere) continue;

            int contactId = findOrCreateUser("sample_" + name.toLowerCase(), name);
            if (contactId < 0) continue;

            int conversationId = StoreToDB.createConversation(name, "individual", currentUserId, contactId);
            if (conversationId >= 0){
                StoreToDB.addMessage(conversationId, contactId, firstMessage); // the message from the other person
            }
        }
    }

    // Returns the user_id of a username, creating the user first if it does not exist.
    private static int findOrCreateUser(String username, String displayName){
        int id = StoreToDB.findUserId(username);
        if (id < 0){
            // placeholder hash: there are no real passwords until login is built
            id = StoreToDB.createUser(username, "no-password-yet", displayName);
        }
        return id;
    }
}