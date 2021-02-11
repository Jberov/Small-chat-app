package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = 1234;
    public static void main(String[] args){
        try{
            Scanner scn = new Scanner(System.in);
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection
            Socket s = new Socket(ip, ServerPort);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            while(true) {
                System.out.println ("Welcome to future chat. Do you have a profile? y/n");
                String answer = scn.nextLine ();
                if (answer.equals ("n")) {
                    registerNewUser (dos, answer, scn);
                    break;
                } else if (answer.equals ("y")) {
                    boolean isLogged;
                    do {
                        isLogged = logUser (dos, dis, answer, scn);
                    } while (!isLogged);
                    break;
                } else {
                    System.out.println ("Enter a valid response");
                }
            }

            // sendMessage thread
            Thread sendMessage = new Thread(() -> {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();

                    try {
                        // write on the output stream
                        dos.writeUTF(msg);
                        if(msg.equals ("logout")){
                            System.out.println ("Goodbye");
                            System.exit (0);
                        }
                    } catch (IOException e) {
                        System.out.println ("Problem with connection. You are disconnected");
                        return;
                    }finally{
                        System.out.println ("Message sent");
                    }
                }
            });

            // readMessage thread
            Thread readMessage = new Thread(() -> {
                while (true) {
                    // read the message sent to this client
                    try {
                        String msg = dis.readUTF ();
                        System.out.println (msg);
                    }catch (IOException e) {
                        System.out.println ("Problem with connection. You are disconnected");
                        System.exit (1);
                    }
                }
            });
            sendMessage.start();
            readMessage.start();
        }catch (IOException ioException){
            System.out.println ("Problem with connection. You are disconnected");
            System.exit (1);
        }catch (InputMismatchException ime){
            System.out.println ("Use valid characters");
        }finally{
            System.out.println ("\n");
        }
    }
    private static void registerNewUser(DataOutputStream dos, String answer, Scanner scn) throws  IOException{
        dos.writeUTF (answer);
        System.out.println ("Enter your desired username: ");
        String username = scn.nextLine ();
        System.out.println ("Enter your desired password: ");
        String password = scn.nextLine ();
        dos.writeUTF (username);
        dos.writeUTF (password);
        System.out.println ("Welcome to super chat. To message someone, write the message, as shown: \nmessage#user. \nIf you want to write to everyone, omit the '#' and the user. \nTo exit, write \"logout\".");
    }
    private static boolean logUser(DataOutputStream dos, DataInputStream dis, String answer, Scanner scn) throws IOException{
        while(true) {
            dos.writeUTF (answer);
            System.out.println ("\nLogin: ");
            System.out.println ("Enter your username: ");
            String username = scn.nextLine ();
            System.out.println ("Enter your password: ");
            String password = scn.nextLine ();
            dos.writeUTF (username);
            dos.writeUTF (password);
            boolean loginSuccess = dis.readBoolean ();
            if(loginSuccess){
                System.out.println ("Welcome to super chat. To message someone, write the message, as shown: \nmessage#user. \nIf you want to write to everyone, omit the '#' and the user. \nTo exit, write \"logout\".");
                return true;
            }else{
                System.out.println ("Wrong username or password");
            }
        }
    }
}
