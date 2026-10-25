/*
This class manages the messages displayed on the left panel in the chat screen.
Displaying the most recent messages under the name of the sender.
*/

import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private String name;
    private String lastMessage;

    //ChatPanel class reads this list to draw the bubbles purple and grey.
    private final List<Message> messages = new ArrayList<>();

    public Conversation(String name, String lastMessage){ //contructor for the conversation object
        this.name = name;
        this.lastMessage = lastMessage;

        //This is the message in history. "false" is when it was sent by the contact, not me.
        messages.add(new Message(lastMessage, false));
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

    //This is used to update messages if we sent so they sync
    public void addMessage(Message m) {
        messages.add(m);
        lastMessage = m.getText();
    }
}