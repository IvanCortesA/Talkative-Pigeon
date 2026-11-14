/*  
    Authors: Andres Lopez, Ivan Cortes 
    Last updated 11/11/26
    
    The database class creates the connection to the sql lite database

*/

package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database{

    private static String url(){
        return System.getProperty("pigeon.db.url", "jdbc:sqlite:talkative_pigeon.db");
    }

    public static Connection connect(){

        try {
            Connection connection = DriverManager.getConnection(url()); //establishes database connection

            try(Statement statement = connection.createStatement()){
                statement.execute("PRAGMA foreign_keys = ON");
            }
            System.out.println("Database connected successfully!");

            return connection;
        } catch (SQLException e) { //error handler

            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());

            return null;
        }
    }

    public static void initializeDatabase(){ //create realtionsip tables
        
        String users = """
            CREATE TABLE IF NOT EXISTS Users 
            ( user_id INTEGER PRIMARY KEY AUTOINCREMENT, 
             username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, 
             display_name TEXT, profile_picture TEXT,created_at TEXT DEFAULT CURRENT_TIMESTAMP);""";

        String conversations = """ 
                CREATE TABLE IF NOT EXISTS Conversations 
                ( conversation_id INTEGER PRIMARY KEY AUTOINCREMENT, 
                 conversation_name TEXT, conversation_type TEXT NOT NULL CHECK
                (conversation_type IN ('individual', 'group')), 
                created_at TEXT DEFAULT CURRENT_TIMESTAMP);""";

        String members = """ 
                CREATE TABLE IF NOT EXISTS ConversationMembers ( 
                    conversation_id INTEGER NOT NULL, 
                    user_id INTEGER NOT NULL, 
                    joined_at TEXT DEFAULT CURRENT_TIMESTAMP, 

                    PRIMARY KEY (conversation_id, user_id),

                    FOREIGN KEY(conversation_id) 
                        REFERENCES Conversations(conversation_id), 
                    FOREIGN KEY (user_id) 
                        REFERENCES Users(user_id));""";

        String messages = """
                CREATE TABLE IF NOT EXISTS Messages (
                    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    conversation_id INTEGER NOT NULL,
                    sender_id INTEGER NOT NULL,
                    message_content TEXT NOT NULL,
                    sent_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    is_edited INTEGER DEFAULT 0,

                    FOREIGN KEY (conversation_id)
                        REFERENCES Conversations(conversation_id),

                    FOREIGN KEY (sender_id)
                        REFERENCES Users(user_id)
                );
                """;

        String settings = """
                CREATE TABLE IF NOT EXISTS UserSettings (
                    user_id INTEGER PRIMARY KEY,
                    theme TEXT NOT NULL DEFAULT 'light' CHECK (theme IN ('light', 'dark')),
                    tts_enabled INTEGER NOT NULL DEFAULT 0 CHECK (tts_enabled IN (0, 1)),
                    tts_rate REAL NOT NULL DEFAULT 1.0 CHECK (tts_rate > 0),
                    tts_voice TEXT,
                    font_scale REAL NOT NULL DEFAULT 1.0 CHECK (font_scale > 0),
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,

                    FOREIGN KEY (user_id)
                        REFERENCES Users(user_id)
                        ON DELETE CASCADE
                );
                """;

        //friends list (one row per pair, user_low is always the smaller id)
        String friendships = """
                CREATE TABLE IF NOT EXISTS Friendships (
                    user_low INTEGER NOT NULL,
                    user_high INTEGER NOT NULL,
                    requested_by INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted')),
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,

                    PRIMARY KEY (user_low, user_high),
                    CHECK (user_low < user_high),
                    CHECK (requested_by IN (user_low, user_high)),

                    FOREIGN KEY (user_low) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (user_high) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (requested_by) REFERENCES Users(user_id) ON DELETE CASCADE
                );
                """;
        

        try(Connection connection = connect()){

            if (connection == null){
                return;
            }

            try(Statement statement = connection.createStatement()){

                statement.execute(users);
                statement.execute(conversations);
                statement.execute(members);
                statement.execute(messages);
                statement.execute(settings);
                statement.execute(friendships);

                System.out.println("Database tables created successfully!");
            }  
            
        }catch(SQLException e){
                System.out.println("Database initialization failed!");
                System.out.println(e.getMessage());
            
        }
    }
}