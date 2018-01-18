package praktikum5;

import java.io.IOException;
import java.net.*;
import java.io.*;
public class Server{
    public static final int PORT = 4938;
    
    public void serveRequests(DataStore dataStore){
        
    }
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Waiting to accept user...");
        Socket socketOfServer = server.accept();
        InputStream is = new BufferedReader(InputStream is = new BufferedReader());;
    }
}