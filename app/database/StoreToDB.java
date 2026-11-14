/*

    Author: Ivan Cortes
    Date: 11/14/2026
    Store: the retrieval and update functions for the Talkative Pigeon database.
    Database.java creates the tables; this class reads from them and writes to them.

    Every method opens its own connection with Database.connect() and closes it when it is done. 
    Values are passed with "?" placeholders 
    DROP TABLE Users  is stored as plain text and can never run as SQL.

    Methods return -1 (or an empty list) when something goes wrong, after printing the error.
*/

package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StoreToDB {

    /** One row of the conversation list: the name and the most recent message under it. */
    public record ConversationRow(int id, String name, String lastMessage) { }

    /** One message in a conversation. */
    public record MessageRow(int id, int senderId, String text, String sentAt) { }

    // ---------- SQL ----------

    private static final String SQL_INSERT_USER = """
            INSERT INTO Users(username, password_hash, display_name) VALUES (?, ?, ?)""";

    private static final String SQL_FIND_USER = """
            SELECT user_id FROM Users WHERE username = ?""";

    private static final String SQL_INSERT_CONVERSATION = """
            INSERT INTO Conversations(conversation_name, conversation_type) VALUES (?, ?)""";

    private static final String SQL_INSERT_MEMBER = """
            INSERT INTO ConversationMembers(conversation_id, user_id) VALUES (?, ?)""";

    private static final String SQL_INSERT_MESSAGE = """
            INSERT INTO Messages(conversation_id, sender_id, message_content) VALUES (?, ?, ?)""";

    // the conversation list for one user, with the newest message of each conversation
    private static final String SQL_CONVERSATIONS_FOR_USER = """
            SELECT c.conversation_id,
                   c.conversation_name,
                   (SELECT m.message_content
                      FROM Messages m
                     WHERE m.conversation_id = c.conversation_id
                     ORDER BY m.sent_at DESC, m.message_id DESC
                     LIMIT 1) AS last_message
              FROM Conversations c
              JOIN ConversationMembers cm ON cm.conversation_id = c.conversation_id
             WHERE cm.user_id = ?
             ORDER BY c.conversation_id""";

    private static final String SQL_MESSAGES_IN_CONVERSATION = """
            SELECT message_id, sender_id, message_content, sent_at
              FROM Messages
             WHERE conversation_id = ?
             ORDER BY sent_at, message_id""";

    // ---------- Users ----------

    // Adds a user and returns the new user_id, or -1 if it failed (for example, the username is taken). 

    public static int createUser(String username, String passwordHash, String displayName) {
        try (Connection c = Database.connect()) {
            if (c == null) return -1;
            try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                ps.setString(3, displayName);
                ps.executeUpdate();
                return firstKey(ps);
            }
        } catch (SQLException e) {
            System.out.println("createUser failed: " + e.getMessage());
            return -1;
        }
    }

    // Returns the user_id for a username, or -1 if there is no such user. 
    public static int findUserId(String username) {
        try (Connection c = Database.connect()) {
            if (c == null) return -1;
            try (PreparedStatement ps = c.prepareStatement(SQL_FIND_USER)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            }
        } catch (SQLException e) {
            System.out.println("findUserId failed: " + e.getMessage());
            return -1;
        }
    }

    // ---------- Conversations ----------

    /*
      Creates a conversation ("individual" or "group") and adds the given users as members.
      Returns the new conversation_id, or -1 if it failed.
     */
    public static int createConversation(String name, String type, int... memberIds) {
        try (Connection c = Database.connect()) {
            if (c == null) return -1;
            c.setAutoCommit(false);
            try {
                int conversationId;
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_CONVERSATION, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, type);
                    ps.executeUpdate();
                    conversationId = firstKey(ps);
                }
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_MEMBER)) {
                    for (int userId : memberIds) {
                        ps.setInt(1, conversationId);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return conversationId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("createConversation failed: " + e.getMessage());
            return -1;
        }
    }

    // Retrieves the conversation list for one user, oldest conversation first. 
    public static List<ConversationRow> getConversations(int userId) {
        List<ConversationRow> rows = new ArrayList<>();
        try (Connection c = Database.connect()) {
            if (c == null) return rows;
            try (PreparedStatement ps = c.prepareStatement(SQL_CONVERSATIONS_FOR_USER)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new ConversationRow(rs.getInt(1), rs.getString(2), rs.getString(3)));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("getConversations failed: " + e.getMessage());
        }
        return rows;
    }

    // ---------- Messages ----------

    // Saves a message and returns its message_id, or -1 if it failed. 
    public static int addMessage(int conversationId, int senderId, String text) {
        try (Connection c = Database.connect()) {
            if (c == null) return -1;
            try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, conversationId);
                ps.setInt(2, senderId);
                ps.setString(3, text);
                ps.executeUpdate();
                return firstKey(ps);
            }
        } catch (SQLException e) {
            System.out.println("addMessage failed: " + e.getMessage());
            return -1;
        }
    }

    // Retrieves every message of a conversation, oldest first.
    public static List<MessageRow> getMessages(int conversationId) {
        List<MessageRow> rows = new ArrayList<>();
        try (Connection c = Database.connect()) {
            if (c == null) return rows;
            try (PreparedStatement ps = c.prepareStatement(SQL_MESSAGES_IN_CONVERSATION)) {
                ps.setInt(1, conversationId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new MessageRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4)));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("getMessages failed: " + e.getMessage());
        }
        return rows;
    }

    // ---------- Helper ----------

    // Reads the id that SQLite generated for the row that was just inserted. 
    private static int firstKey(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            return keys.next() ? keys.getInt(1) : -1;
        }
    }
}