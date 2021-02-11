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
            System.out.println ("New client request received : " + socket);
            // obtain input and output streams
            DataInputStream dis = new DataInputStream (socket.getInputStream ());
            DataOutputStream dos = new DataOutputStream (socket.getOutputStream ());
            ClientHandler mtch = new ClientHandler (socket, dis, dos);
            // Create a new Thread with this object.
            Thread t = new Thread (mtch);
            // start the thread.
            t.start ();
        }
    }

}
