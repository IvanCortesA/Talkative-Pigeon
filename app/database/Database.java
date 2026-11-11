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
    
    private static final String URL = "jdbc:sqlite:talkative_pigeon.db";

    public static Connection connect(){

        try {
            Connection connection = DriverManager.getConnection(URL); //establishes database connection

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
            CREATE TABLE IS NOT EXISTS Users 
            ( user_id INTEGER PRIMARY KEY AUTOINCREMENT, 
             username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, 
             display_name TEXT, profile_picture TEXT,created_at TEXT DEFAULT CURRENT_TIMESTAMP);""";

        String conversations = """ 
                CREATE TABLE IF NOT EXISTS Conversations 
                ( conversation_id INTEGER PRIMARY KEY AUTOINCREMENT, 
                 conversation_name TEXT, conversation_type TEXT NOT NULL CHECK
                (conversatino_type IN ('individual', 'group')), 
                created_at TEXT DEFAULT CURRENT_TIMESTAMP);""";

        String members = """ 
                CREATE TABLE IF NOT EXISTS ConversationMembers ( 
                    conversation_id INTEGER NOT NULL, 
                    user_id INTEGER NOT NULL, 
                    joined_at TEXT DEFAULT CURRENT_TIMESTAMP, 

                    PRIMARY KEY (conversation_id, user_id),

                    FOREGIN KEY(conversation_id) 
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
        

        try(Connection connection = connect()){

            if (connection == null){
                return;
            }

            try(Statement statement = connection.createStatement()){

                statement.execute(users);
                statement.execute(conversations);
                statement.execute(members);
                statement.execute(messages);

                System.out.println("Database tables created successfully!");
            }  
            
        }catch(SQLException e){
                System.out.println("Database initialization failed!");
                System.out.println(e.getMessage());
            
        }
    }
}