public class Message {
    private final String text;
    private final boolean mine;

    public Message(String text, boolean mine) {
        this.text = text;
        this.mine = mine;
    }

    public String getText() { return text; }
    public boolean isMine() { return mine; }
}