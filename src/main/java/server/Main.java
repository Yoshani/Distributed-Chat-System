package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main( String[] args ) {
        System.out.println("------server started------");

        Scanner serverID = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter serverID");

        String serverid = serverID.nextLine();  // Read user input

        ArrayList<ClientHanlder> threadList = new ArrayList<>();
        try{
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println(serverSocket.getInetAddress());
            System.out.println(serverSocket.getLocalSocketAddress());
            System.out.println(serverSocket.getLocalPort());
            System.out.println("TCPServer Waiting for client on port 5000"); //client should use 5000 as port
            while(true){
                Socket socket = serverSocket.accept();
                ClientHanlder clientHanlder = new ClientHanlder(serverid,socket,threadList);
                //starting the tread
                threadList.add(clientHanlder);
                clientHanlder.start();
            }

        } catch (Exception e){
            System.out.println("Error occured in main "+ e.getStackTrace());
        }
    }
}
