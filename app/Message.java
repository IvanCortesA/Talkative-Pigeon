/*
This class represents one chat message. Each message stores two things:
    1. the text that was written
    2. who sent it (me or the other person)

    mine = true would be purple bubble on the right (my text)
    mine = false would be grey bubble on the left (the contact text)
 */

public class Message {
    private final String text;
    private final boolean mine;

    public Message(String text, boolean mine) {
        this.text = text; //The message contents
        this.mine = mine; //check if messages are sent by me or the contact
    }

    public String getText() { return text; }
    public boolean isMine() { return mine; }
}