package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main( String[] args ) {
        System.out.println("------server started------");

        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter server ID : ");

        String serverID = scanner.nextLine();  // Read user input

        ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();
        try{
            ServerSocket serverSocket = new ServerSocket(5000);
            //System.out.println(serverSocket.getInetAddress());
            //System.out.println(serverSocket.getLocalPort());
            System.out.println(serverSocket.getLocalSocketAddress());
            System.out.println("LOG: TCP Server Waiting for clients on port 5000"); //client should use 5000 as port
            while(true){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(serverID,clientSocket, clientHandlerList);
                //starting the tread
                clientHandlerList.add(clientHandler);
                clientHandler.start();
            }

        } catch (Exception e){
            System.out.println("Error occured in main "+ e.getStackTrace());
        }
    }
}
