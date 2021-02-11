package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = 1234;

    public static void main(String[] args) throws IOException
    {
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
                dos.writeUTF (answer);
                System.out.println ("Enter your desired username: ");
                String username = scn.nextLine ();
                System.out.println ("Enter your desired password: ");
                String password = scn.nextLine ();
                try {
                    dos.writeUTF (username);
                    dos.writeUTF (password);
                    System.out.println ("Welcome to super chat. To message someone, write the message, as shown: \nmessage#user. \nIf you want to write to everyone, omit the '#' and the user. \nTo exit, write \"logout\".");
                } catch (IOException e) {
                    System.out.println ("Connection terminated");
                    return;
                }
                break;
            } else if (answer.equals ("y")) {
                while(true) {
                    dos.writeUTF (answer);
                    System.out.println ("\nLogin: ");
                    System.out.println ("Enter your username: ");
                    String username = scn.nextLine ();
                    System.out.println ("Enter your password: ");
                    String password = scn.nextLine ();
                    try {
                        dos.writeUTF (username);
                        dos.writeUTF (password);
                        boolean loginSuccess = dis.readBoolean ();
                        if(loginSuccess){
                            System.out.println ("Welcome to super chat. To message someone, write the message, as shown: \nmessage#user. \nIf you want to write to everyone, omit the '#' and the user. \nTo exit, write \"logout\".");
                            break;
                        }else{
                            System.out.println ("Wrong username or password");
                        }
                    } catch (IOException e) {
                        System.out.println ("Connection terminated");
                        return;
                    }
                }
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
                        System.exit (0);
                    }
                } catch (IOException e) {
                    System.out.println ("Connection terminated");
                    return;
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(() -> {

            while (true) {
                try {
                    // read the message sent to this client
                    String msg = dis.readUTF();
                    System.out.println(msg);
                }catch (IOException ioe){
                    System.out.println ("Connection terminated");
                    return;
                }
            }
        });
        sendMessage.start();
        readMessage.start();

    }
}
