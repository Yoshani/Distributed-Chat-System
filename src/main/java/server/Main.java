package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //TODO : Load configs
        System.out.println("------server started------");

        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter server ID : ");

        String serverID = scanner.nextLine();  // Read user input

        ServerState.getInstance().initializeWithConfigs(serverID,5000);//TODO : change to auto fetch from config


        try {
            ServerSocket serverSocket = new ServerSocket(ServerState.getInstance().getServerPort());
            System.out.println(serverSocket.getLocalSocketAddress());
            System.out.println("LOG  : TCP Server Waiting for clients on port 5000"); //client should use 5000 as port
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket);
                //starting the tread
                ServerState.getInstance().getClientHandlerThreadList().add(clientHandlerThread);
                clientHandlerThread.start();
            }

        } catch (Exception e) {
            System.out.println("ERROR : occurred in main " + Arrays.toString(e.getStackTrace()));
        }
    }
}
