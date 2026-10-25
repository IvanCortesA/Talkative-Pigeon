import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private String name;
    private String lastMessage;
    private final List<Message> messages = new ArrayList<>();

    public Conversation(String name, String lastMessage){
        this.name = name;
        this.lastMessage = lastMessage;
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

    public void addMessage(Message m) {
        messages.add(m);
        lastMessage = m.getText();
    }
}