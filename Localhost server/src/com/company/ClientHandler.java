package com.company;
import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private String name = "message";
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
            ioe.printStackTrace ();
        }
        String received;
        try {
            while (true) {
                received = dis.readUTF ();
                if (received.equals ("logout")) {
                    this.isloggedin = false;
                    for (ClientHandler i : Server.activeUsers) {
                        if (i.isloggedin) {
                            i.dos.writeUTF (this.name + " is offline");
                        }
                    }
                    this.socket.close ();
                    Server.activeUsers.remove (this.client);
                    break;
                }
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
                    for(ClientHandler user : Server.activeUsers){
                        user.dos.writeUTF ("This will be sent to EVERYONE on the server ");
                        if(user.name.equals (name)){
                            continue;
                        }
                        user.dos.writeUTF (this.name + ": " + received);
                    }
                }
            }
        } catch (NoSuchElementException nsee) {
            System.out.println ("No such user");
        } catch (IOException e) {
            e.printStackTrace ();
        }
        try{
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
}
    public void login() throws IOException{

        while (true) {
            String test = dis.readUTF ();
            if (test.equals ("y")) {
                String username = dis.readUTF ();
                File users = new File ("users.txt");
                Scanner scan = new Scanner (users);
                String password = dis.readUTF ();
                while (scan.hasNextLine ()) {
                    if (scan.nextLine ().equals (username + "," + password)) {
                        client.dos.writeBoolean (true);
                        name = username;
                        System.out.println ("Creating a new handler for this client...");
                        System.out.println ("Adding this client to active client list");
                        System.out.println ("User" + username + "has logged in");
                        for (ClientHandler i : Server.activeUsers) {
                            if (i.isloggedin) {
                                i.dos.writeUTF (this.name + " is online");
                            }
                        }
                        isloggedin = true;
                        Server.activeUsers.add (client);
                        break;
                    }else{
                        isloggedin = false;
                    }
                }
                if(isloggedin){
                    printAllActiveUsers ();
                    break;
                }else{
                    client.dos.writeBoolean (false);
                }
            }else if (test.equals ("n")) {
                String username = dis.readUTF ();
                String password = dis.readUTF ();
                FileWriter writer = new FileWriter ("users.txt", true);
                name = username;
                BufferedWriter bufferedWriter = new BufferedWriter (writer);
                bufferedWriter.write (username + "," + password);
                bufferedWriter.newLine ();
                bufferedWriter.close ();
                writer.close ();
                Server.activeUsers.add (client);
                System.out.println ("Creating a new handler for this client...");
                System.out.println ("Adding this client to active client list");
                printAllActiveUsers ();
                for (ClientHandler i : Server.activeUsers) {
                    if (i.isloggedin) {
                        i.dos.writeUTF (this.name + " is online");
                    }
                }
                isloggedin = true;
                break;
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
}
