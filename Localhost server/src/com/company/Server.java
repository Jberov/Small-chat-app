package com.company;

import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
    // Vector to store active clients
    static Vector<ClientHandler> activeUsers = new Vector<> ();
    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket serverSocket = new ServerSocket (1234);
        Socket socket;
        // running infinite loop for getting
        // client request
        while(true) {
            socket = serverSocket.accept ();
            clientHandling(socket);
        }
    }
    public static void logFileWrite(String message){
        BufferedWriter bufferedWriter;
        try {
            FileWriter writer = new FileWriter ("log.txt", true);
            bufferedWriter = new BufferedWriter (writer);
            bufferedWriter.write (message);
            bufferedWriter.newLine ();
            bufferedWriter.close ();
            writer.close ();
        }catch (IOException ioe){
            System.out.println ("Failed to create log file");
        }finally{
            System.out.println ("Log file writing successful");
        }
    }
    private static void clientHandling(Socket socket){
        try {
            System.out.println ("New client request received : " + socket);
            // obtain input and output streams
            DataInputStream dis = new DataInputStream (socket.getInputStream ());
            DataOutputStream dos = new DataOutputStream (socket.getOutputStream ());
            ClientHandler mtch = new ClientHandler (socket, dis, dos);
            Thread t = new Thread (mtch);
            t.start ();
        }catch (SocketTimeoutException i){
            logFileWrite ("Connection took too long");
        }catch(SocketException se){
            logFileWrite ("Failure of socket " + socket);
        }catch (IOException ioe){
            logFileWrite ("Connection error");
        }finally{
            System.out.println ("Client has been successfully added");
        }
    }

}
