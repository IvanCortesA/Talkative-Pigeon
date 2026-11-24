/*
This class manages the messages displayed on the left panel in the chat screen.
Displaying the most recent messages under the name of the sender.
*/

import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private final int id;
    private final String name;
    private String lastMessage = "";

    //ChatPanel class reads this list to draw the bubbles purple and grey.
    private final List<Message> messages = new ArrayList<>();

    public Conversation(int id, String name ){ //contructor for the conversation object
        this.name = name;
        this.id = id;
    }

    public String getname(){
        return name;
    }

    public String getlastMessage(){
        return lastMessage;
    }

    public List<Message> getMessages() {
        return messages;
    }


        public int getId(){
        return id;
    }

        //Adds a message to this conversation (used when loading from the database and when we send one)
    public void addMessage(Message m) {
        messages.add(m);
        lastMessage = m.getText();
    }
}