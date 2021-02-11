package com.company;
import java.io.*;
import java.net.Socket;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private String name = "";
    final DataInputStream dis;
    final DataOutputStream dos;
    ClientHandler client = ClientHandler.this;
    Socket socket;
    boolean isloggedin;
    // constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.socket = s;
        this.isloggedin = false;
    }

    @Override
    public void run() {

        try {
            login ();
        } catch (IOException ioe) {
            Server.logFileWrite ("Socket error");
        }finally {
            System.out.println ("Login successful of the new client");
        }
        String received;
        try {
            while (true) {
                received = dis.readUTF ();
                if (logout (received)){
                    break;
                }
                sendMessage (received);
            }
        } catch (NoSuchElementException nsee) {
            System.out.println ("No such user");
        } catch (IOException e) {
           Server.logFileWrite ("Connection error with message");
        }finally{
            System.out.println ("Message sent successfully");
        }
        try{
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            Server.logFileWrite ("Failure closing datastreams");
        }finally{
            System.out.println ("Logout complete");
        }
}
    public void login() throws IOException{

        while (true) {
            String test = dis.readUTF ();
            if (test.equals ("y")) {
                String username = dis.readUTF ();
                String password = dis.readUTF ();
                if(loginUser (username, password)){
                    break;
                }else{
                    client.dos.writeBoolean (false);
                }
            }else if (test.equals ("n")) {
               if(loginNewUser ()){
                   client.dos.writeBoolean (true);
                   break;
               }
            } else {
                System.out.println ("User failed to login correctly");
            }
        }
    }
    public void printAllActiveUsers() throws IOException{
        client.dos.writeUTF ("List of active users: ");
        for(ClientHandler users : Server.activeUsers){
            client.dos.writeUTF (users.name);
        }
    }
     synchronized private void writeToFile(String username, String password) throws IOException{
        try {
            FileWriter writer = new FileWriter ("users.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter (writer);
            bufferedWriter.write (username + "," + password);
            bufferedWriter.newLine ();
            bufferedWriter.close ();
            writer.close ();
        }catch (IOException ioe){
            sendMessageToUser (name, "Failure to write your credentials in the file" );
        }finally{
            sendMessageToUser (name, "Profile successfully created");
        }
    }

    private void sendMessageToUser(String name, String message) throws  IOException{
        for (ClientHandler user : Server.activeUsers) {
            if (user.name.equals (name)) {
                user.dos.writeUTF (message);
                break;
            }
        }
    }
    private boolean loginNewUser() throws IOException{
        String username = dis.readUTF ();
        String password = dis.readUTF ();
        name = username;
        Server.activeUsers.add (client);
        System.out.println ("Creating a new handler for this client...");
        System.out.println ("Adding this client to active client list");
        writeToFile (name,password);
        printAllActiveUsers ();
        alertForNewUser ();
        isloggedin = true;
        return isloggedin;
    }
    private boolean loginUser(String username, String password) throws IOException {
        try{
        File users = new File ("users.txt");
        Scanner scan = new Scanner (users);
        while (scan.hasNextLine ()) {
            if (scan.nextLine ().equals (username + "," + password)) {
                client.dos.writeBoolean (true);
                this.name = username;
                System.out.println ("Creating a new handler for this client...");
                System.out.println ("Adding this client to active client list");
                System.out.println ("User " + name + " has logged in");
                Server.activeUsers.add (client);
                printAllActiveUsers ();
                alertForNewUser ();
                isloggedin = true;
                return true;
            }else{
                isloggedin = false;
            }
        }

    }catch (FileNotFoundException fileNotFoundException){
            Server.logFileWrite ("Could not find users file");
        }catch (IOException ioe){
            Server.logFileWrite ("Error connecting to user");
        }finally {
            sendMessageToUser (name, "Login successful");
        }
        return isloggedin;
    }
    private boolean logout(String received) throws IOException{
        if (received.equals ("logout")) {
            this.isloggedin = false;
            this.socket.close ();
            Server.activeUsers.remove (this.client);
            alertUserGoingOffline ();
            return true;
        }else{
            return false;
        }
    }
    private void sendMessage(String received) throws IOException{
        Pattern pattern = Pattern.compile ("#");
        Matcher match = pattern.matcher (received);
        if(match.find ()){
            StringTokenizer st = new StringTokenizer (received, "#");
            String MsgToSend = st.nextToken ();
            String recipient = st.nextToken ();
            boolean exists = false;
            for (ClientHandler mc : Server.activeUsers) {
                if (mc.name.equals (recipient) && mc.isloggedin) {
                    mc.dos.writeUTF (this.name + " : " + MsgToSend);
                    exists = true;
                    break;
                }
            }
            if(!exists){
                client.dos.writeUTF ("User " + recipient + " is currently offline or does not exist.");
            }
        }else{
            client.dos.writeUTF ("This will be sent to EVERYONE on the server ");
            for(ClientHandler user : Server.activeUsers){
                if( Server.activeUsers.size () == 1){
                    user.dos.writeUTF ("You are the only one on this server.");
                    break;
                }
                if(user.name.equals (name)){
                    continue;
                }
                user.dos.writeUTF (this.name + ": " + received);
            }
        }
    }
    private void alertForNewUser() throws  IOException{
        for (ClientHandler i : Server.activeUsers) {
            if (i.isloggedin) {
                i.dos.writeUTF (this.name + " is online");
            }
        }
    }
    private void alertUserGoingOffline() throws  IOException{
        for (ClientHandler i : Server.activeUsers) {
            if (i.isloggedin) {
                i.dos.writeUTF (this.name + " is offline");
            }
        }
    }
}
