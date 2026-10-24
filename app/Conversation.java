public class Conversation {

    private String name;
    private String lastMessage;

    public Conversation(String name, String lastMessage){
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getname(){
        return name;
    }

    public String getlastMessage(){
        return lastMessage;
    }
}