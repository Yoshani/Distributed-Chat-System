import messaging.MessageThread;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.*;
import java.net.SocketAddress;

// Server class
class ChatServer {
    public static void main(String[] args)
    {
        ServerSocket clients_socket = null;
        String serverid = null;
        String server_address = null;
        ServerSocket coordination_socket = null;
        String server_conf = null;

        try {
            serverid = args[0];
            server_conf = args[1];
            System.out.println(server_conf);

            Server server = Server.getInstance( serverid, server_conf );

            // server is listening on port for coordination
            coordination_socket = new ServerSocket();
            // binding the SocketAddress with inetAddress and port
            SocketAddress endPoint1 = new InetSocketAddress(server.getServer_address(), server.getCoordination_port());
            coordination_socket.bind(endPoint1);

            System.out.println("SERVER" + " " + coordination_socket.getLocalSocketAddress() +
                                       " IS CONNECTED FOR COORDINATION");

            // server is listening on port for clients
            clients_socket = new ServerSocket();
            SocketAddress endPoint2 = new InetSocketAddress(server.getServer_address(), server.getClients_port());
            clients_socket.bind(endPoint2);
            System.out.println("SERVER" + " " + clients_socket.getLocalSocketAddress() +
                                       " IS CONNECTED FOR CLIENTS");

            boolean debug = false;

            // running infinite loop for getting client request
            while (true) {

                // socket object to receive incoming client requests
                Socket client = clients_socket.accept();

                // Displaying that new client is connected to clients_socket
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
            if (clients_socket != null) {
                try {
                    clients_socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}