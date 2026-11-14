package database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/*
    Tests for the tables created by Database.initializeDatabase()
    Every test gets a new database file in a temp folder, so these never
    touch talkative_pigeon.db.

    initializeDatabase() will catch and print its own errors instead of
    throwing, so a broken CREATE TABLE would not fail by itself.
*/
class DatabaseTest
{

    @TempDir
    Path dir;

    @BeforeEach
    void setUp()
    {
        System.setProperty("pigeon.db.url", "jdbc:sqlite:" + dir.resolve("test.db"));
        Database.initializeDatabase();
    }

    @AfterEach
    void tearDown()
    {
        System.clearProperty("pigeon.db.url");
    }

    //These are the helpers
    private static void exec(Statement st, String sql) throws SQLException
    {
        st.execute(sql);
    }

    private static int count(Statement st, String sql) throws SQLException
    {
        try (ResultSet rs = st.executeQuery(sql))
        {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void addUser(Statement st, int id, String name) throws SQLException
    {
        exec(st, "INSERT INTO Users(user_id, username, password_hash) VALUES (" + id + ", '" + name + "', 'x')");
    }

    private static void addConversation(Statement st, int id) throws SQLException
    {
        exec(st, "INSERT INTO Conversations(conversation_id, conversation_type) VALUES (" + id + ", 'individual')");
    }

    //Setup
    @Test
    void connectReturnsAConnection() throws Exception
    {
        try (Connection c = Database.connect())
        {
            assertNotNull(c);
        }
    }

    @Test
    void allSixTablesExist() throws Exception
    {
        List<String> tables = List.of("Users", "Conversations", "ConversationMembers",
                "Messages", "UserSettings", "Friendships");
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            for (String name : tables)
            {
                int found = count(st, "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + name + "'");
                assertTrue(found == 1, "missing table: " + name);
            }
        }
    }

    @Test
    void initializeCanRunTwice() throws Exception
    {
        Database.initializeDatabase(); //The second run must not break anything
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            assertEquals(6, count(st, "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'"));
        }
    }

    // ---------- Users, Conversations, Members, Messages ----------

    @Test
    void usernameMustBeUnique() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            assertThrows(SQLException.class, () -> addUser(st, 2, "alice"));
        }
    }

    @Test
    void conversationTypeMustBeIndividualOrGroup() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            exec(st, "INSERT INTO Conversations(conversation_type) VALUES ('group')");
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Conversations(conversation_type) VALUES ('bogus')"));
        }
    }

    @Test
    void memberRowsAreUniqueAndNeedRealParents() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            addConversation(st, 1);
            exec(st, "INSERT INTO ConversationMembers(conversation_id, user_id) VALUES (1, 1)");
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO ConversationMembers(conversation_id, user_id) VALUES (1, 1)"));   // same member twice
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO ConversationMembers(conversation_id, user_id) VALUES (1, 99)"));  // user does not exist
        }
    }

    @Test
    void foreignKeysAreEnforcedOnMessages() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Messages(conversation_id, sender_id, message_content) VALUES (999, 1, 'hi')"));
        }
    }

    @Test
    void messageGetsDefaults() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            addConversation(st, 1);
            exec(st, "INSERT INTO Messages(conversation_id, sender_id, message_content) VALUES (1, 1, 'hi')");
            try (ResultSet rs = st.executeQuery("SELECT is_edited, sent_at FROM Messages"))
            {
                rs.next();
                assertEquals(0, rs.getInt(1));
                assertNotNull(rs.getString(2));
            }
        }
    }

    @Test
    void deletingAUserWhoSentMessagesIsBlocked() throws Exception
    {
        // Documents the current behavior: Messages has no ON DELETE rule, so the database refuses.
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            addConversation(st, 1);
            exec(st, "INSERT INTO Messages(conversation_id, sender_id, message_content) VALUES (1, 1, 'hi')");
            assertThrows(SQLException.class, () -> exec(st, "DELETE FROM Users WHERE user_id = 1"));
        }
    }

    //This is user setting
    @Test
    void settingsHaveDefaultsAndRejectBadValues() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            exec(st, "INSERT INTO UserSettings(user_id) VALUES (1)"); // all defaults
            try (ResultSet rs = st.executeQuery("SELECT theme, tts_enabled, tts_rate, font_scale FROM UserSettings"))
            {
                rs.next();
                assertEquals("light", rs.getString(1));
                assertEquals(0, rs.getInt(2));
                assertEquals(1.0, rs.getDouble(3));
                assertEquals(1.0, rs.getDouble(4));
            }
            assertThrows(SQLException.class, () -> exec(st, "UPDATE UserSettings SET theme = 'purple'"));
            assertThrows(SQLException.class, () -> exec(st, "UPDATE UserSettings SET tts_enabled = 2"));
            assertThrows(SQLException.class, () -> exec(st, "UPDATE UserSettings SET tts_rate = 0"));
        }
    }

    @Test
    void deletingAUserDeletesTheirSettings() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            exec(st, "INSERT INTO UserSettings(user_id) VALUES (1)");
            exec(st, "DELETE FROM Users WHERE user_id = 1");
            assertEquals(0, count(st, "SELECT COUNT(*) FROM UserSettings"));
        }
    }

    //This is for friendship
    @Test
    void friendshipRulesAreEnforced() throws Exception
    {
        try (Connection c = Database.connect(); Statement st = c.createStatement())
        {
            addUser(st, 1, "alice");
            addUser(st, 2, "bob");
            addUser(st, 3, "carol");

            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Friendships(user_low, user_high, requested_by) VALUES (1, 1, 1)"));  // with yourself
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Friendships(user_low, user_high, requested_by) VALUES (2, 1, 1)"));  // wrong order

            exec(st, "INSERT INTO Friendships(user_low, user_high, requested_by) VALUES (1, 2, 1)");

            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Friendships(user_low, user_high, requested_by) VALUES (1, 2, 2)"));  // same pair again
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Friendships(user_low, user_high, requested_by) VALUES (1, 3, 2)"));  // requester not in the pair
            assertThrows(SQLException.class, () -> exec(st,
                    "INSERT INTO Friendships(user_low, user_high, requested_by, status) VALUES (2, 3, 2, 'bogus')"));
        }
    }
}