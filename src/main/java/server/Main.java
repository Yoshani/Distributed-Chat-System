package server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        System.out.println("LOG  : ------server started------");

        ServerState.getInstance().initializeWithConfigs(args[0], args[1]);
        try {
            // server socket for coordination
            ServerSocket serverCoordinationSocket = new ServerSocket();

            // bind SocketAddress with inetAddress and port
            SocketAddress endPointCoordination = new InetSocketAddress(
                    ServerState.getInstance().getServerAddress(),
                    ServerState.getInstance().getCoordinationPort()
            );
            serverCoordinationSocket.bind(endPointCoordination);
            System.out.println(serverCoordinationSocket.getLocalSocketAddress());
            System.out.println("LOG  : TCP Server Waiting for coordination on port "+
                                       serverCoordinationSocket.getLocalPort()); // port open for coordination

            // server socket for clients
            ServerSocket serverClientsSocket = new ServerSocket();

            // bind SocketAddress with inetAddress and port
            SocketAddress endPointClient = new InetSocketAddress(
                    ServerState.getInstance().getServerAddress(),
                    ServerState.getInstance().getClientsPort()
            );
            serverClientsSocket.bind(endPointClient);
            System.out.println(serverClientsSocket.getLocalSocketAddress());
            System.out.println("LOG  : TCP Server Waiting for clients on port "+
                                       serverClientsSocket.getLocalPort()); // port open for clients
            while (true) {
                Socket clientSocket = serverClientsSocket.accept();
                ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket);
                //starting the tread
                ServerState.getInstance().addClientHandlerThreadToList(clientHandlerThread);
                clientHandlerThread.start();
            }

        } catch (Exception e) {
            System.out.println("ERROR : occurred in main " + Arrays.toString(e.getStackTrace()));
        }
    }
}
