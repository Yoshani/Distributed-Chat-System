import messaging.MessageThread;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.*;

// Server class
class ChatServer {
    public static void main(String[] args)
    {
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(4444);
            server.setReuseAddress(true);

            boolean debug = false;

            // running infinite loop for getting client request
            while (true) {

                // socket object to receive incoming client requests
                Socket client = server.accept();

                // Displaying that new client is connected to server
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                // start client thread
                Thread clientThread = new Thread(new MessageThread(client, debug));
                clientThread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}